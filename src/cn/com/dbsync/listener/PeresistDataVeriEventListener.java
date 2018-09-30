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
import java.util.ArrayList;
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

                    //����һ�����е�����
                    List lastRowList = this.modifDataSet(dbDialect,sourConn,statementMapHelp,confTableBean,daoResult,optTokens);

                    statiInfoBean.incrStatiInfo(confTableBean.getSourceTable(), reslist.size());
                }

                //�����߳�ͳ�ƶ���
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

            LOG.error("����У���������ʧ��!", ex);
            peresistevent.setErrorMsg("����ͬ���쳣�˳���ErrorMsg="+ex.getMessage());
            throw new DBSyncException("����У���������ʧ��!",ex, EventSourceBean.EventType.PERESIST);

        }finally {
            if(sourConn != null && !isException) {
                try {
                    sourConn.commit();
                } catch (SQLException e) {

                }
            }
            //�ж�Ϊ���һ�������쳣�˳�ʱ����¼��־���ͷ���Դ
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
        List<List> insertlist = new ArrayList<List>();
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
                   insertlist.add(rowList);
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

        boolean isInsertExcept = false;

        if(inPStatement != null){
            try {
                inPStatement.executeBatch();
            }catch (SQLException e){
                isInsertExcept = true;
                LOG.error("��������ʧ�ܣ�SQL=" + confTableBean.getInsertSql(), e);
            }
        }

        if(upPStatement != null){
            try {
                upPStatement.executeBatch();
            }catch (SQLException e){
                LOG.error("��������ʧ�ܣ�SQL=" + confTableBean.getUpdateSql(), e);
            }
        }

        if(delPStatement != null){
            try {
                delPStatement.executeBatch();
            }catch (SQLException e){
                LOG.error("����ɾ��ʧ�ܣ�SQL=" + confTableBean.getDeleteSqlById(), e);
            }
        }

        if(isInsertExcept){

            int updateRel = 0;
            boolean  isBatch = false;

            for(List tmplist: insertlist){

                if(tmplist == null || tmplist.isEmpty()){
                    continue;
                }

                dbDialect.setPreparedStatementParams(upPStatement, rowList, columnMetaDatas);
                updateRel = upPStatement.executeUpdate();

                if(updateRel == 0){
                    dbDialect.setPreparedStatementParams(inPStatement, rowList, columnMetaDatas);
                    inPStatement.addBatch();
                    isBatch = true;
                }

                if(isBatch){
                    inPStatement.executeBatch();
                }
            }
        }

        return rowList;
    }


}
