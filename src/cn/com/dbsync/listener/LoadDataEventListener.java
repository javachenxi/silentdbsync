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
 * ���� LoadDataEvent �¼���������������Ӧ�ı������
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
        //�����߳�������
        if(statementMapHelp == null){
            statementMapHelp = new StatementMapHelp();
            threadContext.set(statementMapHelp);
        }
        return statementMapHelp;
    }

    /**
     * �������������¼�,��Դ�����ݿ���������
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

        //�������ݰ������
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
                //��������ͬ����ȫ��ͬ����Ԥ����Statement
                if(confTaskBean.isIncTask()){
                    ConfTaskBean.SyncLastValue syncvalue = confTaskBean.getSyncLastValue(tmpConftable.getSourceTable(),tmpConftable.getIncerColumn());
                    if(syncvalue != null && syncvalue.getLastVaule() != null) {
                        prepareStatement = sourConn.prepareStatement(tmpConftable.getSelectSql());
                        dbDialect.setPreparedStatementNotNullParam(prepareStatement, 1, syncvalue.getLastVaule(),syncvalue.getColType());
                        statementMapHelp.put(tmpConftable, prepareStatement);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("ִ��SQL:" + tmpConftable.getSelectSql() + " ����:" + syncvalue.getLastVaule());
                        }

                    }
                    //����ͬ��û������ֵ������ִ��ȥ������������SQL
                    if(prepareStatement == null){
                        prepareStatement = sourConn.prepareStatement(tmpConftable.getShortSelectSql());
                        statementMapHelp.put(tmpConftable, prepareStatement);

                        if(LOG.isDebugEnabled()){
                            LOG.debug("ִ��SQL:" + tmpConftable.getShortSelectSql());
                        }
                    }
                }else{
                    prepareStatement = sourConn.prepareStatement(tmpConftable.getSelectSql());
                    statementMapHelp.put(tmpConftable, prepareStatement);

                    if(LOG.isDebugEnabled()){
                        LOG.debug("ִ��SQL:" + tmpConftable.getSelectSql());
                    }
                }
                //ִ�в�ѯ����
                prepareStatement.setFetchDirection(ResultSet.FETCH_FORWARD);
                resultSet = prepareStatement.executeQuery();

                ResultSetMetaData rsmd = resultSet.getMetaData();
                DAOResultMetaData currResultMetaData = dbDialect.transResultMetaData(rsmd);
                daoResult.appendDAOResultMetaData(currResultMetaData);
                int colCount = rsmd.getColumnCount();
                ArrayList resData = new ArrayList(COMMIT_RECORD_MAX);
                ArrayList rowdata = null;

                //�������ҷ�������һ����¼������״̬��STARTED��
                while (resultSet.next()) {
                    rowdata = new ArrayList(rsmd.getColumnCount());
                    for (int r = 0; r < colCount; r++) {
                        rowdata.add(dbDialect.castResultSetToJavaType(rsmd.getColumnType(r+1),resultSet,r+1));
                    }
                    resData.add(rowdata);

                    //������ӱ�Ļ������õݹ�ķ�ʽ�����ӱ���������ѯ��Ӧ��ȫ����¼
                    if(tmpConftable.getChildList()!= null){
                        ConfTableBean childConfTable = null;

                        for(int n=0;n<tmpConftable.getChildList().size(); n++){
                            childConfTable = tmpConftable.getChildList().get(n);
                            int relateIndex = currResultMetaData.getColumnIndex(childConfTable.getRelateColumn());
                            //�����Ĳ�ѯ����Ԥ����
                            loadChildTableData(sourConn,srcPoolBean,dbDialect,childConfTable,rowdata.get(relateIndex),packResultBean);
                        }
                    }
                    //�ж��Ƿ�ﵽ����ύ��¼1000
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
                        DBSyncException stopException = new DBSyncException("ǿ��ֹͣͬ������");
                        stopException.setEventType(EventSourceBean.EventType.STOP);
                        throw stopException;
                    }
                }

                if(resData.size()>0){
                    daoResult.appendResult(resData);
                    //������ݼ�
                    packResultBean.addDataEntity(tmpConftable, daoResult);
                }

                //�ж��Ƿ�ﵽ����ύ��¼1000,����״̬��STOPED��
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
            LOG.error("��ѯ�����쳣 [TaskInstId]=" + loadevent.getTaskInstId(), e);
            loadevent.setErrorMsg("����ͬ���쳣�˳���ErrorMsg=" + e.getMessage());
            try {
                //����Ǵ���PeresistEvent�쳣ʱ�����ٷ���PeresistEvent�¼�
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
                LOG.error("������������������ݰ�����ʧ�� [TaskInstId]=" + loadevent.getTaskInstId(), dbe);
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
                    LOG.error("�ر�Connection [TaskInstId]="+loadevent.getTaskInstId(),e);
                }
            }

            if(!isException) {
                try {
                    packSeqNumber ++;
                    publicPeresistEvent(packResultBean, packSeqNumber, true, loadevent);
                }catch (DBSyncException dbe){
                    LOG.error("������������������ݰ�����ʧ�� [TaskInstId]="+loadevent.getTaskInstId(),dbe);
                }
            }

            publicStopEvent(event);

        }
    }

    private void publicStopEvent(EventSourceBean event){
        StopEventBean stopEvent = new StopEventBean(event);
        stopEvent.setSync(true);

        //�ⲿ�����ֹͣ�¼������¼�״̬�ó� �˹�����
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
        //ֱ�ӷַ��¼���ֱ�������ݱ���
        dispatchEventContainer.dispatchEvent(perDateEvent);
    }

    private void loadChildTableData(Connection sourConn,ConfPoolBean srcPoolBean ,DBDialect dbDialect ,ConfTableBean conftable,Object relateObj,PackResultBean
            packResultBean){

        ResultSet resultSet = null;

        try {
            //���̱߳����л�ȡPreparedStatement��������ʱ���´����������߳�������
            PreparedStatement prepareStatement = this.getThreadParam().get(conftable);
            if(prepareStatement == null){
                prepareStatement = sourConn.prepareStatement(conftable.getSelectSql());
                this.getThreadParam().put(conftable, prepareStatement);
            }else {
                //����ϴ�ִ�й��Ĳ���ֵ
                prepareStatement.clearParameters();
            }
            //�����Ĳ�ѯ����Ԥ����
            prepareStatement.setObject(1,relateObj);

            if(LOG.isDebugEnabled()){
                LOG.debug("ִ��SQL:" + conftable.getSelectSql() +" ������"+ relateObj);
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
                        //�����Ĳ�ѯ����Ԥ����
                        loadChildTableData(sourConn,srcPoolBean,dbDialect,conftable.getChildList().get(n),rowdata.get(relateIndex),packResultBean);
                    }
                }
            }

            if(resData.size()>0){
                daoResult.appendResult(resData);
                //������ݼ�
                packResultBean.addDataEntity(conftable, daoResult);
            }

        }catch (Exception e){
            LOG.error("��ѯ�����쳣 [tablename]="+conftable.getSourceTable(),e);
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





