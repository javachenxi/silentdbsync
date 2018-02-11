package cn.com.dbsync.listener;

import cn.com.dbsync.bean.*;
import cn.com.dbsync.core.DBSyncException;
import cn.com.dbsync.core.DispatchEventContainer;
import cn.com.dbsync.dao.*;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.util.DBSyncConstant;
import cn.com.dbsync.util.SpringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018-01-23.
 */
public class PeresistDataVeriEventListener extends PeresistDataEventListener {

    private final static Log LOG = LogFactory.getLog(PeresistDataVeriEventListener.class.getName());

    /**
     * Instantiates a new Event listener adapter.
     *
     * @param dispatchEventContainer the dispatch event container
     */
    public PeresistDataVeriEventListener(DispatchEventContainer dispatchEventContainer) {
        super(dispatchEventContainer);
    }

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

            if (packResultBean != null && packResultBean.getDataEntityList() != null) {

                List<PackResultBean.DataEntity> dataEntityList = packResultBean.getDataEntityList();

                for (int i = 0; i < dataEntityList.size(); i++) {

                    PackResultBean.DataEntity dataEntity = dataEntityList.get(i);
                    DAOResult daoResult = dataEntity.getResult();
                    ConfTableBean confTableBean = dataEntity.getConfTableBean();
                    int[] optTokens = dataEntity.getOptToken();

                    List reslist = daoResult.getFirstSqlResultList();

                    if (reslist == null || reslist.size() == 0) {
                        continue;
                    }

                    //更新一个表中的数据
                    List lastRowList = this.modifDataSet(dbDialect,sourConn,statementMapHelp,confTableBean,daoResult,optTokens);

                    statiInfoBean.incrStatiInfo(confTableBean.getSourceTable(), reslist.size());
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

            LOG.error("批量校验更新数据失败!", ex);
            peresistevent.setErrorMsg("数据同步异常退出！ErrorMsg="+ex.getMessage());
            throw new DBSyncException("批量校验更新数据失败!",ex, EventSourceBean.EventType.PERESIST);

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
                              ConfTableBean confTableBean, DAOResult daoResult, int[] optTokens) throws SQLException{

        DAOResultMetaData resultMetaData = daoResult.getSqlResultSetMetaData(0);
        ColumnMetaData[] columnMetaDatas = resultMetaData.getColumnMetaDataArray();
        int pksize = confTableBean.getPkeycols().size();
        ColumnMetaData[] pkColumnMetaDatas = Arrays.copyOfRange(columnMetaDatas,
                    columnMetaDatas.length - pksize, columnMetaDatas.length);
        PreparedStatement delPStatement = null, upPStatement = null, inPStatement = null;
        List reslist = daoResult.getFirstSqlResultList();
        List rowList = null;
        int token = 0;

        for (int r = 0; r < reslist.size(); r++) {

            rowList = (List) reslist.get(r);
            token = optTokens[r];

            switch (token){
               case PackResultBean.OPT_TOKEN_D:
                    if(delPStatement == null){
                        delPStatement = dbDialect.getDeleteByIdPreparedStatement(sourConn, statementMapHelp, confTableBean);
                    }

                   dbDialect.setPreparedStatementParams(delPStatement, rowList.subList(rowList.size()-pksize, rowList.size()), pkColumnMetaDatas);
                   delPStatement.addBatch();

                   break;
               case PackResultBean.OPT_TOKEN_I:
                   if(inPStatement == null){
                       inPStatement = dbDialect.getInsertPreparedStatement(sourConn, statementMapHelp, confTableBean);
                   }

                   dbDialect.setPreparedStatementParams(inPStatement, rowList, columnMetaDatas);
                   inPStatement.addBatch();
                   break;
               case PackResultBean.OPT_TOKEN_U:
                   if(upPStatement == null){
                       upPStatement = dbDialect.getUpdatePreparedStatement(sourConn, statementMapHelp, confTableBean);
                   }

                   dbDialect.setPreparedStatementParams(upPStatement, rowList, columnMetaDatas);
                   upPStatement.addBatch();
                   break;
            }
        }

        if(inPStatement != null){
            inPStatement.executeBatch();
        }

        if(upPStatement != null){
            upPStatement.executeBatch();
        }

        if(delPStatement != null){
            delPStatement.executeBatch();
        }

        return rowList;
    }


}
