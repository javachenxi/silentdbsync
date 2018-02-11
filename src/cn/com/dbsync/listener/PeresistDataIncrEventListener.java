package cn.com.dbsync.listener;

import cn.com.dbsync.bean.*;
import cn.com.dbsync.core.DBSyncException;
import cn.com.dbsync.core.DispatchEventContainer;
import cn.com.dbsync.dao.*;
import cn.com.dbsync.util.DBSyncConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 2018-01-24.
 */
public class PeresistDataIncrEventListener extends PeresistDataEventListener{

    private final static Log LOG = LogFactory.getLog(PeresistDataIncrEventListener.class.getName());

    /**
     * Instantiates a new Peresist data event listener.
     *
     * @param dispatchEventContainer the dispatch event container
     */
    public PeresistDataIncrEventListener(DispatchEventContainer dispatchEventContainer) {
        super(dispatchEventContainer);
    }

    /**
     * 事件处理：接收事件对象，获取数据集并插入目标端数据库
     *
     * @param event
     */
    public void peresist(EventSourceBean event){
        PeresistDataEventBean peresistevent = (PeresistDataEventBean)event;
        List<ConfTableBean> confTableBeanList = peresistevent.getConfTableBeans();
        ConfTaskBean confTaskBean = peresistevent.getConfTaskBean();
        PackResultBean packResultBean = peresistevent.getPackResultBean();
        StatementMapHelp statementMapHelp = getThreadStatementParam();
        StatiInfoBean statiInfoBean = this.getThreadStatiInfoParam(confTaskBean).clone();
        ConfPoolBean tagPoolBean = null;
        boolean isException = false;
        Connection sourConn = null;
        DBDialect dbDialect = null;
        int[] reBatchs = null;

        try {
            sourConn = this.getTheadConection(confTableBeanList.get(0).getTargetDbName());
            sourConn.setAutoCommit(false);
            tagPoolBean = pooledManager.getConfPoolBean(confTableBeanList.get(0).getTargetDbName());
            dbDialect = DBDialectFactory.createDBDialect(Enum.valueOf(DBSyncConstant.DBType.class, tagPoolBean.getDbType()),
                    tagPoolBean.getAppCharset(), tagPoolBean.getDbCharset());

            //全量同步第一个同步包先删除原先数据
            if (peresistevent.isFirst() &&packResultBean!=null&& !confTaskBean.isIncTask()) {
                try {
                    deleteTable(sourConn, confTableBeanList, statementMapHelp);
                } catch (Exception ex1) {
                    sourConn.rollback();
                    LOG.error("全量同步删除数据失败!", ex1);
                    throw new DBSyncException("全量同步删除数据失败!", ex1);
                } finally {
                    sourConn.commit();
                    statementMapHelp.closeAll();
                    sourConn.setAutoCommit(false);
                }
            }

            if (packResultBean != null && packResultBean.getDataEntityList() != null) {

                List<PackResultBean.DataEntity> dataEntityList = packResultBean.getDataEntityList();

                for (int i = 0; i < dataEntityList.size(); i++) {
                    PackResultBean.DataEntity dataEntity = dataEntityList.get(i);
                    DAOResult daoResult = dataEntity.getResult();
                    ConfTableBean confTableBean = dataEntity.getConfTableBean();

                    List reslist = daoResult.getFirstSqlResultList();

                    if (reslist == null || reslist.size() == 0) {
                        continue;
                    }

                    //更新一个表中的数据
                    List lastRowList = this.modifDataSet(dbDialect,sourConn,statementMapHelp,confTableBean,daoResult);

                    statiInfoBean.incrStatiInfo(confTableBean.getSourceTable(), reslist.size());

                    //记录当前主表的最后更新记录
                    if (confTaskBean.isIncTask() && confTableBean.getDependTable() == null) {

                        DAOResultMetaData resultMetaData = daoResult.getSqlResultSetMetaData(0);
                        ColumnMetaData[] columnMetaDatas = resultMetaData.getColumnMetaDataArray();

                        int index = resultMetaData.getColumnIndex(confTableBean.getIncerColumn());
                        Object lastValue = lastRowList.get(index);

                        if(lastValue == null) {
                            LOG.warn("增长字段有空值 Table:"+confTableBean.getSourceTable()+" Column:"+confTableBean.getIncerColumn());

                            for(int v=reslist.size()-1; v>0;v--){

                                lastValue = ((List)reslist.get(v)).get(index);

                                if(lastValue != null){
                                    break;
                                }
                            }
                        }

                        statiInfoBean.addSyncLastValue(confTableBean.getSourceTable(), columnMetaDatas[index].getName(),
                                columnMetaDatas[index].getType(), lastValue);

                    }
                }

                //更新线程统计对象
                this.setThreadStatiInfoParam(statiInfoBean);
            }
        }catch (Exception ex){
            isException = true;
            if(sourConn != null){
                try {
                    sourConn.rollback();
                } catch (SQLException e) {

                }
            }
            LOG.error("批量新增数据失败!", ex);
            peresistevent.setErrorMsg("数据同步异常退出！ErrorMsg="+ex.getMessage());
            throw new DBSyncException("批量新增数据失败!",ex, EventSourceBean.EventType.PERESIST);
        }finally {
            if(sourConn != null && !isException) {
                try {
                    sourConn.commit();
                } catch (SQLException e) {

                }
            }
            //判断为最后一个包或异常退出时，记录日志，释放资源
            if(peresistevent.isLast() || isException){
                finishPeresistEvent(peresistevent );
            }
        }
    }

    private List modifDataSet(DBDialect dbDialect, Connection sourConn, StatementMapHelp statementMapHelp,
                              ConfTableBean confTableBean, DAOResult daoResult) throws SQLException{

        PreparedStatement ps = dbDialect.getInsertPreparedStatement(sourConn, statementMapHelp, confTableBean);
        DAOResultMetaData resultMetaData = daoResult.getSqlResultSetMetaData(0);
        ColumnMetaData[] columnMetaDatas = resultMetaData.getColumnMetaDataArray();

        List reslist = daoResult.getFirstSqlResultList();
        List rowList = null;

        for (int r = 0; r < reslist.size(); r++) {
            rowList = (List) reslist.get(r);
            dbDialect.setPreparedStatementParams(ps, rowList, columnMetaDatas);
            ps.addBatch();
        }

        boolean isBatchException = false;

        try {
            //执行批处理SQL
            ps.executeBatch();
        }catch (SQLException e){
            LOG.error("批量插入失败！SQL=" + confTableBean.getInsertSql(), e);
            isBatchException = true;
        }

        //批量插入异常时，先 UPDATE 再 INSERT
        if(isBatchException){
            PreparedStatement ups = dbDialect.getUpdatePreparedStatement(sourConn, statementMapHelp, confTableBean);
            int updateRel = 0;
            boolean isBatch = false;

            for (int u = 0; u < reslist.size(); u++) {
                rowList = (List) reslist.get(u);
                dbDialect.setPreparedStatementParams(ups, rowList, columnMetaDatas);
                updateRel = ups.executeUpdate();

                if(updateRel == 0){
                    dbDialect.setPreparedStatementParams(ps, rowList, columnMetaDatas);
                    ps.addBatch();
                    isBatch = true;
                }
            }

            if(isBatch){
                ps.executeBatch();
            }
        }

        return rowList;
    }
}
