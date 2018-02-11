package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfPoolBean;
import cn.com.dbsync.dao.*;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.bean.PackResultBean;
import cn.com.dbsync.core.DBSyncException;
import cn.com.dbsync.core.DispatchEventContainer;
import cn.com.dbsync.util.DBSyncConstant;

import cn.com.dbsync.util.StringConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理 LoadDataEvent 事件，按配置载入相应的表的数据
 * <p>
 * Created by cxi on 2016/5/6.
 */
public class LoadDataEventListener extends EventListenerAdapter {

    private final static Log LOG = LogFactory.getLog(LoadDataEventListener.class.getName());
    private static ThreadLocal<StatementMapHelp> threadContext = new ThreadLocal<StatementMapHelp>();

    private static final int COMMIT_RECORD_MAX = 1000;
    private DBPooledManager pooledManager;


    /**
     * Instantiates a new Load data event listener.
     *
     * @param dispatchEventContainer the dispatch event container
     */
    public LoadDataEventListener(DispatchEventContainer dispatchEventContainer){
        super(dispatchEventContainer);
        pooledManager = DBPooledManager.getInstance();
    }

    private StatementMapHelp getThreadParam(){
        StatementMapHelp statementMapHelp = threadContext.get();
        //存入线程上下文
        if(statementMapHelp == null){
            statementMapHelp = new StatementMapHelp();
            threadContext.set(statementMapHelp);
        }
        return statementMapHelp;
    }

    /**
     * 处理载入数据事件,从源端数据库载入数据
     *
     * @param event
     */
    @Override
    public void load(EventSourceBean event) {
        LoadDataEventBean loadevent = (LoadDataEventBean)event;
        List<ConfTableBean> confTableBeanList = loadevent.getConfTableBeans();
        ConfTaskBean confTaskBean = event.getConfTaskBean();
        ConfTableBean tmpConftable = confTableBeanList.get(0);
        StatementMapHelp statementMapHelp = this.getThreadParam();
        PeresistDataEventBean perDateEvent = null;
        ConfPoolBean srcPoolBean = null;
        Connection sourConn = null;
        DBDialect dbDialect = null;

        //发送数据包的序号
        int packSeqNumber = 0;
        boolean isException = false;
        PackResultBean packResultBean = null;

        try {
            sourConn = pooledManager.getConnectionByPoolName(tmpConftable.getSourceDbName());
            srcPoolBean = pooledManager.getConfPoolBean(tmpConftable.getSourceDbName());

            dbDialect = DBDialectFactory.createDBDialect(Enum.valueOf(DBSyncConstant.DBType.class, srcPoolBean.getDbType()),
                    srcPoolBean.getAppCharset(), srcPoolBean.getDbCharset());

            PreparedStatement prepareStatement = null;
            ResultSet resultSet = null;
            DAOResult daoResult = null;
            packResultBean = new PackResultBean();

            for(int i=0; i<confTableBeanList.size(); i++){
                prepareStatement = null;
                daoResult = new DAOResult();
                tmpConftable = confTableBeanList.get(i);
                //区分增量同步与全量同步的预编译Statement
                if(confTaskBean.isIncTask()){
                    ConfTaskBean.SyncLastValue syncvalue = confTaskBean.getSyncLastValue(tmpConftable.getSourceTable(),tmpConftable.getIncerColumn());
                    if(syncvalue != null && syncvalue.getLastVaule() != null) {
                        prepareStatement = sourConn.prepareStatement(tmpConftable.getSelectSql());
                        dbDialect.setPreparedStatementNotNullParam(prepareStatement, 1, syncvalue.getLastVaule(),syncvalue.getColType());
                        statementMapHelp.put(tmpConftable, prepareStatement);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("执行SQL:" + tmpConftable.getSelectSql() + " 参数:" + syncvalue.getLastVaule());
                        }

                    }
                    //增量同步没有增量值，所以执行去掉增量条件的SQL
                    if(prepareStatement == null){
                        prepareStatement = sourConn.prepareStatement(tmpConftable.getShortSelectSql());
                        statementMapHelp.put(tmpConftable, prepareStatement);

                        if(LOG.isDebugEnabled()){
                            LOG.debug("执行SQL:" + tmpConftable.getShortSelectSql());
                        }
                    }
                }else{
                    prepareStatement = sourConn.prepareStatement(tmpConftable.getSelectSql());
                    statementMapHelp.put(tmpConftable, prepareStatement);

                    if(LOG.isDebugEnabled()){
                        LOG.debug("执行SQL:" + tmpConftable.getSelectSql());
                    }
                }
                //执行查询数据
                prepareStatement.setFetchDirection(ResultSet.FETCH_FORWARD);
                resultSet = prepareStatement.executeQuery();

                ResultSetMetaData rsmd = resultSet.getMetaData();
                DAOResultMetaData currResultMetaData = dbDialect.transResultMetaData(rsmd);
                daoResult.appendDAOResultMetaData(currResultMetaData);
                int colCount = rsmd.getColumnCount();
                ArrayList resData = new ArrayList(COMMIT_RECORD_MAX);
                ArrayList rowdata = null;

                //继续查找符合有下一条记录，任务状态是STARTED的
                while (resultSet.next()) {
                    rowdata = new ArrayList(rsmd.getColumnCount());
                    for (int r = 0; r < colCount; r++) {
                        rowdata.add(dbDialect.castResultSetToJavaType(rsmd.getColumnType(r+1),resultSet,r+1));
                    }
                    resData.add(rowdata);

                    //如果有子表的话，则用递归的方式遍历子表树，并查询对应的全部记录
                    if(tmpConftable.getChildList()!= null){
                        ConfTableBean childConfTable = null;

                        for(int n=0;n<tmpConftable.getChildList().size(); n++){
                            childConfTable = tmpConftable.getChildList().get(n);
                            int relateIndex = currResultMetaData.getColumnIndex(childConfTable.getRelateColumn());
                            //关联的查询条件预编译
                            loadChildTableData(sourConn,srcPoolBean,dbDialect,childConfTable,rowdata.get(relateIndex),packResultBean);
                        }
                    }
                    //判断是否达到最大提交记录1000
                    if(packResultBean.getSize()+resData.size()>COMMIT_RECORD_MAX){
                        packSeqNumber ++;
                        daoResult.appendResult(resData);
                        packResultBean.addDataEntity(tmpConftable, daoResult);
                        publicPeresistEvent(packResultBean, packSeqNumber, false, loadevent);
                        resData = new ArrayList(COMMIT_RECORD_MAX);
                        daoResult = new DAOResult();
                        daoResult.appendDAOResultMetaData(currResultMetaData);
                        packResultBean = new PackResultBean();
                    }

                    if(!this.getStatus(loadevent)){
                        DBSyncException stopException = new DBSyncException("强制停止同步任务！");
                        stopException.setEventType(EventSourceBean.EventType.STOP);
                        throw stopException;
                    }
                }

                if(resData.size()>0){
                    daoResult.appendResult(resData);
                    //打包数据集
                    packResultBean.addDataEntity(tmpConftable, daoResult);
                }

                //判断是否达到最大提交记录1000,任务状态是STOPED的
                if(packResultBean.getSize()>COMMIT_RECORD_MAX){
                    packSeqNumber ++;
                    //packResultBean.addDataEntity(tmpConftable, daoResult);
                    publicPeresistEvent(packResultBean, packSeqNumber, false, loadevent);
                    packResultBean = new PackResultBean();
                }

                if(resultSet!= null){
                    resultSet.close();
                    resultSet = null;
                }
            }
        }catch (Exception e) {
            isException = true;
            LOG.error("查询数据异常 [TaskInstId]=" + loadevent.getTaskInstId(), e);
            loadevent.setErrorMsg("数据同步异常退出！ErrorMsg=" + e.getMessage());
            try {
                //如果是处理PeresistEvent异常时，则不再发布PeresistEvent事件
                if (e instanceof DBSyncException) {
                    DBSyncException dbSyncException = (DBSyncException) e;
                    if (!EventSourceBean.EventType.PERESIST.equals(dbSyncException.getEventType())) {
                        packSeqNumber ++;
                        publicPeresistEvent(null, packSeqNumber, true, loadevent);
                    }
                } else {
                    packSeqNumber ++;
                    publicPeresistEvent(null, packSeqNumber, true, loadevent);
                }
            } catch (DBSyncException dbe) {
                LOG.error("发布保存数据最后数据包处理失败 [TaskInstId]=" + loadevent.getTaskInstId(), dbe);
            }

            if(srcPoolBean !=null && e instanceof SQLException){
                LOG.error(StringConverter.convertString(e.getMessage(), srcPoolBean.getDbCharset(), srcPoolBean.getAppCharset()));
            }
        }finally{

            threadContext.remove();

            if(statementMapHelp != null){
                statementMapHelp.closeAll();
            }

            if(sourConn != null){
                try {
                    pooledManager.closeConnectionByPoolName(tmpConftable.getSourceDbName(), sourConn);
                } catch (RuntimeException e) {
                    LOG.error("关闭Connection [TaskInstId]="+loadevent.getTaskInstId(),e);
                }
            }

            if(!isException) {
                try {
                    packSeqNumber ++;
                    publicPeresistEvent(packResultBean, packSeqNumber, true, loadevent);
                }catch (DBSyncException dbe){
                    LOG.error("发布保存数据最后数据包处理失败 [TaskInstId]="+loadevent.getTaskInstId(),dbe);
                }
            }

            publicStopEvent(event);

        }
    }

    private void publicStopEvent(EventSourceBean event){
        StopEventBean stopEvent = new StopEventBean(event);
        stopEvent.setSync(true);

        //外部发起的停止事件，将事件状态置成 人工类型
        if(!this.getStatus(event)){
            stopEvent.setLaunchType(EventSourceBean.LaunchType.MANUL);
        }

        dispatchEventContainer.publicEvent(stopEvent);
    }

    private void publicPeresistEvent(PackResultBean packResultBean,int index, boolean isLast,LoadDataEventBean event){
        PeresistDataEventBean perDateEvent = new PeresistDataEventBean(event);
        perDateEvent.setSync(true);
        perDateEvent.setPackResultBean(packResultBean);
        perDateEvent.setConfTableBeans(event.getConfTableBeans());
        perDateEvent.setSeqNumber(index);
        perDateEvent.setLast(isLast);
        perDateEvent.setErrorMsg(event.getErrorMsg());
        //直接分发事件并直处理数据保存
        dispatchEventContainer.dispatchEvent(perDateEvent);
    }

    private void loadChildTableData(Connection sourConn,ConfPoolBean srcPoolBean ,DBDialect dbDialect ,ConfTableBean conftable,Object relateObj,PackResultBean
            packResultBean){

        ResultSet resultSet = null;

        try {
            //从线程变量中获取PreparedStatement，不存在时重新创建并放入线程容器中
            PreparedStatement prepareStatement = this.getThreadParam().get(conftable);
            if(prepareStatement == null){
                prepareStatement = sourConn.prepareStatement(conftable.getSelectSql());
                this.getThreadParam().put(conftable, prepareStatement);
            }else {
                //清除上次执行过的参数值
                prepareStatement.clearParameters();
            }
            //关联的查询条件预编译
            prepareStatement.setObject(1,relateObj);

            if(LOG.isDebugEnabled()){
                LOG.debug("执行SQL:" + conftable.getSelectSql() +" 参数："+ relateObj);
            }

            resultSet = prepareStatement.executeQuery();
            ResultSetMetaData rsmd = resultSet.getMetaData();
            DAOResult daoResult = new DAOResult();
            DAOResultMetaData currResultMetaData = dbDialect.transResultMetaData(rsmd);
            daoResult.appendDAOResultMetaData(currResultMetaData);
            int colCount = rsmd.getColumnCount();

            ArrayList resData = new ArrayList(COMMIT_RECORD_MAX);
            ArrayList rowdata = null;

            while (resultSet.next()) {
                rowdata = new ArrayList(colCount);

                for (int r = 0; r < colCount; r++) {
                    dbDialect.castResultSetToJavaType(rsmd.getColumnType(r+1),resultSet, r+1);
                }

                resData.add(rowdata);

                if(conftable.getChildList()!= null){
                    ConfTableBean childConfTable = null;
                    for(int n=0;n<conftable.getChildList().size(); n++){
                        childConfTable = conftable.getChildList().get(n);
                        int relateIndex = currResultMetaData.getColumnIndex(childConfTable.getRelateColumn());
                        //关联的查询条件预编译
                        loadChildTableData(sourConn,srcPoolBean,dbDialect,conftable.getChildList().get(n),rowdata.get(relateIndex),packResultBean);
                    }
                }
            }

            if(resData.size()>0){
                daoResult.appendResult(resData);
                //打包数据集
                packResultBean.addDataEntity(conftable, daoResult);
            }

        }catch (Exception e){
            LOG.error("查询数据异常 [tablename]="+conftable.getSourceTable(),e);
            throw new DBSyncException(e.getMessage());
        }finally{
            if(resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException e) {

                }
            }
        }
    }

}





