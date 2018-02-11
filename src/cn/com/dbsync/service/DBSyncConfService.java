package cn.com.dbsync.service;

import cn.com.dbsync.bean.*;
import cn.com.dbsync.dao.DAOResult;
import cn.com.dbsync.dao.DAOResultMetaData;
import cn.com.dbsync.dao.DBMybatisSimpleTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-10-25.
 */
@Service
public class DBSyncConfService extends DBMybatisSimpleTemplate {

    private static final Log log = LogFactory.getLog(DBSyncConfService.class.getName());

    private static final String DBSYNC_TABLECONF_QUERYBYID = "DBSyncConfMapper.queryDBSyncTableConfById";
    private static final String DBSYNC_TASKCONF_QUERY = "DBSyncConfMapper.queryDBSyncTaskConf";
    private static final String DBSYNC_TASKCONF_QUERYBYID = "DBSyncConfMapper.queryDBSyncTaskConfById";
    private static final String DBSYNC_TASKCONF_UPDATE = "DBSyncConfMapper.updateTaskConfWithLast";
    private static final String DBSYNC_TASKCONF_UPDATE_COUNT = "DBSyncConfMapper.updateTaskConfWithCount";
    private static final String DBSYNC_TASKINSTLOG_INSTID = "DBSyncConfMapper.queryTaskInstLogByInstId";
    private static final String DBSYNC_TASKINSTLOG_INSERT = "DBSyncConfMapper.insertTaskInstLog";
    private static final String DBSYNC_TASKINSTLOG_UPDATEBYID = "DBSyncConfMapper.updateTaskInstLogById";
    private static final String DBSYNC_POOLCONF_QUERYBYID = "DBSyncConfMapper.queryConfPoolById";
    private static final String DBSYNC_TABLESQL_INSERT = "DBSyncConfMapper.insertConfTableSql";
    private static final String DBSYNC_TABLESQL_QUERYBYID = "DBSyncConfMapper.queryConfTableSqlById";

    private static final int RESULT_FAIL = 0;

    /**
     *
     * @param confTableBean
     * @return
     */
   public boolean insertConfTableSql(ConfTableBean confTableBean) {
       int result = 0;

       try {
           ConfTableSqlBean confTableSqlBean = new ConfTableSqlBean(confTableBean.getTaskId(),confTableBean.getSourceTable(),
                   confTableBean.getTargetTable(), confTableBean.getSourceDbName(), confTableBean.getTargetDbName());
           confTableSqlBean.setSrcSelectSql(confTableBean.getSelectSql());
           confTableSqlBean.setSrcShortSelectSql(confTableBean.getShortSelectSql());
           confTableSqlBean.setTagDeleteByIdSql(confTableBean.getDeleteSqlById());
           confTableSqlBean.setTagDeleteSql(confTableBean.getDeleteSql());
           confTableSqlBean.setTagInsertSql(confTableBean.getInsertSql());
           confTableSqlBean.setTagMinsertSql(confTableBean.getMergInsertSql());
           confTableSqlBean.setTagUpdateSql(confTableBean.getUpdateSql());
           confTableSqlBean.setSrcSelectOrderBySql(confTableBean.getSrcSelectByOrderSql());
           confTableSqlBean.setTagSelectOrderBySql(confTableBean.getTagSelectByOrderSql());

           result = this.insert(DBSYNC_TABLESQL_INSERT, confTableSqlBean);
       } catch (Exception e) {
           log.error("����SQL����¼ʧ�ܣ�TaskID=" + confTableBean.getTaskId(), e);
       }

       return !(RESULT_FAIL == result);
   }

    /**
     * �����������Ҷ���
     *
     * @param confTableBean
     * @return
     */
    public ConfTableBean getConfTableSqlBeanById(ConfTableBean confTableBean){

        ConfTableSqlBean rtConfTableSqlBean = this.selectOnce(DBSYNC_TABLESQL_QUERYBYID, confTableBean);

        if(rtConfTableSqlBean == null){
            return null;
        }

        confTableBean.setSelectSql(rtConfTableSqlBean.getSrcSelectSql());
        confTableBean.setShortSelectSql(rtConfTableSqlBean.getSrcShortSelectSql());
        confTableBean.setDeleteSqlById(rtConfTableSqlBean.getTagDeleteByIdSql());
        confTableBean.setDeleteSql(rtConfTableSqlBean.getTagDeleteSql());
        confTableBean.setInsertSql(rtConfTableSqlBean.getTagInsertSql());
        confTableBean.setMergInsertSql(rtConfTableSqlBean.getTagMinsertSql());
        confTableBean.setUpdateSql(rtConfTableSqlBean.getTagUpdateSql());
        confTableBean.setSrcSelectByOrderSql(rtConfTableSqlBean.getSrcSelectOrderBySql());
        confTableBean.setTagSelectByOrderSql(rtConfTableSqlBean.getTagSelectOrderBySql());

        return confTableBean;
    }

    /**
     * ����־ID������־��Ϣ
     *
     * @param taskInstLogBean the task inst log bean
     * @return boolean boolean
     */
    public boolean updateTaskInstLogById(TaskInstLogBean taskInstLogBean) {
        int result = 0;
        try {
            result = this.insert(DBSYNC_TASKINSTLOG_UPDATEBYID, taskInstLogBean);
        }catch (Exception e){
            log.error("������־ʧ�ܣ�ID=" + taskInstLogBean.getSyncLogId(), e);
        }
        return !(RESULT_FAIL == result);
    }

    /**
     * ʵ����һ�����м�¼���е���־
     *
     * @param taskInstLogBean the task inst log bean
     * @return boolean boolean
     */
    public boolean insertTaskInstLog(TaskInstLogBean taskInstLogBean) {
        int result = 0;
        try {
            result = this.update(DBSYNC_TASKINSTLOG_INSERT, taskInstLogBean);
        }catch (Exception e){
            log.error("������־ʧ�ܣ�ID=" + taskInstLogBean.getSyncLogId(), e);
        }
        return !(RESULT_FAIL == result);
    }

    /**
     * Update task conf for last boolean.
     *
     * @param jsonSyncVal the json sync val
     * @param taskId      the task id
     * @return the boolean
     */
    public boolean updateTaskConfForLast(String jsonSyncVal, long taskId) {

        int result = 0;

        try {
            ConfTaskBean confTaskBean = new ConfTaskBean();
            confTaskBean.setTaskId(taskId);
            confTaskBean.setLastValue(jsonSyncVal);
            result = this.update(DBSYNC_TASKCONF_UPDATE, confTaskBean);
        }catch (Exception e){
            log.error("���������������ֵLastValueʧ�ܣ�ID=" + taskId, e);
        }

        return !(RESULT_FAIL == result);
    }

    /**
     * Update task conf for count boolean.
     *
     * @param count  the count
     * @param taskId the task id
     * @return the boolean
     */
    public boolean updateTaskConfForCount(long count, long taskId) {

        int result = 0;

        try {
            ConfTaskBean confTaskBean = new ConfTaskBean();
            confTaskBean.setTaskId(taskId);
            confTaskBean.setInstCount(count);
            result = this.update(DBSYNC_TASKCONF_UPDATE_COUNT, confTaskBean);
        }catch (Exception e){
            log.error("������������ִ�д���countʧ�ܣ�ID=" + taskId, e);
        }

        return !(RESULT_FAIL == result);
    }

    /**
     * ��������ʵ��ID��ѯ��Ӧ����־
     *
     * @param instId the inst id
     * @return task inst log by inst id
     */
    public TaskInstLogBean getTaskInstLogByInstId(String instId) {

        TaskInstLogBean taskInstLogBean = this.selectOnce(DBSYNC_TASKINSTLOG_INSTID, instId);

        return taskInstLogBean;
    }

    /**
     * ����taskId���ض�Ӧͬ���������ñ�
     *
     * @param taskId the task id
     * @return conf task bean
     */
    public ConfTaskBean getConfTaskByTaskId(long taskId){

        ConfTaskBean confTaskBean = this.selectOnce(DBSYNC_TASKCONF_QUERYBYID, taskId);

        return confTaskBean;
    }

    /**
     * ��ѯ��������״̬Ϊ��Ч��
     *
     * @return list list
     */
    public List<ConfTaskBean> getConfTaskList(){
        List<ConfTaskBean> confTaskBeans = this.select(DBSYNC_TASKCONF_QUERY, null);
        return confTaskBeans;
    }

    /**
     * ����taskId���ض�Ӧͬ������ͬ���������
     *
     * @param taskId the task id
     * @return conf table by task id
     */
    public List<ConfTableBean> getConfTableByTaskId(long taskId) {
        List<ConfTableBean> rootbeanList = new ArrayList<ConfTableBean>();
        List<Map> mapList = this.select(DBSYNC_TABLECONF_QUERYBYID, taskId);

        if (mapList == null || mapList.isEmpty()) {
            return null;
        }

        List<ConfTableBean> confList = new ArrayList<ConfTableBean>();
        ConfTableBean tempbean = null;
        Map dataRow = null;

        for(int i=0; i<mapList.size(); i++) {
            dataRow = (Map) mapList.get(i);

            //��һ�ν������Դ�˱�ͬ���ж�Ϊ��һ���������
            if (tempbean == null || !tempbean.getSourceTable().equals(dataRow.get("SOURCE_TABLE"))
                    || !tempbean.getTargetTable().equals(dataRow.get("TARGET_TABLE"))) {
                tempbean = new ConfTableBean();
                tempbean.setTaskId(((BigDecimal)dataRow.get("TASK_ID")).longValue());
                tempbean.setDependTable((String)dataRow.get("DEPEND_TABLE"));
                tempbean.setRelateColumn((String)dataRow.get("RELATE_COLUMN"));
                tempbean.setSourceDbName((String)dataRow.get("SOURCE_DBNAME"));
                tempbean.setSourceTable((String)dataRow.get("SOURCE_TABLE"));
                tempbean.setTargetDbName((String)dataRow.get("TARGET_DBNAME"));
                tempbean.setTargetTable((String)dataRow.get("TARGET_TABLE"));

                if (tempbean.getDependTable() == null) {
                    rootbeanList.add(tempbean);
                } else {
                    confList.add(tempbean);
                }
            }

            tempbean.addConfColumn((String)dataRow.get("SOURCE_COLUMN"),
                    (String)dataRow.get("TARGET_COLUMN"),
                    ((BigDecimal)dataRow.get("INCER_TOKEN")).intValue(),
                    ((BigDecimal)dataRow.get("PKEY_TOKEN")).intValue(),
                    null,
                    ((BigDecimal)dataRow.get("TARGET_COLTYPE")).intValue(),
                    ((BigDecimal)dataRow.get("COLUMN_ORDER")).intValue());
        }

        //���������ӱ�Ľṹ
        for (int i = 0; i < confList.size(); i++) {

            for (int r = 0; rootbeanList.size() > r; r++) {
                ConfTableBean rootbean = rootbeanList.get(r);
                if (rootbean.getSourceTable().equalsIgnoreCase(confList.get(i).getDependTable())) {
                    rootbean.addChildTable(confList.get(i));
                    continue;
                }
            }

            //�ҵ����ڵ�ͼ���
            for (int j = 0; j < confList.size(); j++) {
                if (j != i && confList.get(i).getDependTable().equalsIgnoreCase(confList.get(j).getSourceTable())) {
                    confList.get(j).addChildTable(confList.get(i));
                    break;
                }
            }
        }

        return rootbeanList;
    }

    /**
     * ���� PoolName ��ȡ���ӳ�����
     *
     * @param poolName
     * @return
     */
    public ConfPoolBean getConfPoolById(String poolName){
        ConfPoolBean confPoolBean = this.selectOnce(DBSYNC_POOLCONF_QUERYBYID, poolName);
        return confPoolBean;
    }

}
