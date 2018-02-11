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
     * �¼����������¼����󣬻�ȡ���ݼ�������Ŀ������ݿ�
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

            //ȫ��ͬ����һ��ͬ������ɾ��ԭ������
            if (peresistevent.isFirst() &&packResultBean!=null&& !confTaskBean.isIncTask()) {
                try {
                    deleteTable(sourConn, confTableBeanList, statementMapHelp);
                } catch (Exception ex1) {
                    sourConn.rollback();
                    LOG.error("ȫ��ͬ��ɾ������ʧ��!", ex1);
                    throw new DBSyncException("ȫ��ͬ��ɾ������ʧ��!", ex1);
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

                    //����һ�����е�����
                    List lastRowList = this.modifDataSet(dbDialect,sourConn,statementMapHelp,confTableBean,daoResult);

                    statiInfoBean.incrStatiInfo(confTableBean.getSourceTable(), reslist.size());

                    //��¼��ǰ����������¼�¼
                    if (confTaskBean.isIncTask() && confTableBean.getDependTable() == null) {

                        DAOResultMetaData resultMetaData = daoResult.getSqlResultSetMetaData(0);
                        ColumnMetaData[] columnMetaDatas = resultMetaData.getColumnMetaDataArray();

                        int index = resultMetaData.getColumnIndex(confTableBean.getIncerColumn());
                        Object lastValue = lastRowList.get(index);

                        if(lastValue == null) {
                            LOG.warn("�����ֶ��п�ֵ Table:"+confTableBean.getSourceTable()+" Column:"+confTableBean.getIncerColumn());

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
            LOG.error("������������ʧ��!", ex);
            peresistevent.setErrorMsg("����ͬ���쳣�˳���ErrorMsg="+ex.getMessage());
            throw new DBSyncException("������������ʧ��!",ex, EventSourceBean.EventType.PERESIST);
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
            //ִ��������SQL
            ps.executeBatch();
        }catch (SQLException e){
            LOG.error("��������ʧ�ܣ�SQL=" + confTableBean.getInsertSql(), e);
            isBatchException = true;
        }

        //���������쳣ʱ���� UPDATE �� INSERT
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
