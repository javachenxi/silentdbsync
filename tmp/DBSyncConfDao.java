package cn.com.dbsync.dao;


import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.bean.TaskInstLogBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cxi on 2016/5/6.
 */
public class DBSyncConfDao extends DBSimpleTemplate{


    private static final String DBSYNC_TABLECONF_QUERYBYID = "queryDBSyncTableConfById";

    private static final String DBSYNC_TASKCONF_QUERY = "queryDBSyncTaskConf";
    private static final String DBSYNC_TASKCONF_QUERYBYID = "queryDBSyncTaskConfById";
    private static final String DBSYNC_TASKCONF_UPDATE = "updateTaskConfWithLast";
    private static final String DBSYNC_TASKCONF_UPDATE_COUNT = "updateTaskConfWithCount";

    private static final String DBSYNC_TASKINSTLOG_INSTID = "queryTaskInstLogByInstId";
    private static final String DBSYNC_TASKINSTLOG_INSERT = "insertTaskInstLog";
    private static final String DBSYNC_TASKINSTLOG_UPDATEBYID = "updateTaskInstLogById";

    /**
     * 按日志ID更新日志信息
     *
     * @param taskInstLogBean the task inst log bean
     * @return boolean boolean
     */
    public boolean updateTaskInstLogById(TaskInstLogBean taskInstLogBean) {
        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(taskInstLogBean.getTaskInstId());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncStatus());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncInfo());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncDataSize());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncLastValue());
        dpg.addFirstSqlParam(taskInstLogBean.getAllSize());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncLogId());

        DAOResult result = this.insert(DBSYNC_TASKINSTLOG_UPDATEBYID, (List)dpg.getDAOParam().get(0));
        return result.isSuccess();
    }

    /**
     * 实例第一次运行记录运行的日志
     *
     * @param taskInstLogBean the task inst log bean
     * @return boolean boolean
     */
    public boolean insertTaskInstLog(TaskInstLogBean taskInstLogBean) {
        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(taskInstLogBean.getTaskInstId());
        dpg.addFirstSqlParam(taskInstLogBean.getTaskId());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncStatus());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncDataSize());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncInfo());
        dpg.addFirstSqlParam(taskInstLogBean.getSyncLastValue());
        dpg.addFirstSqlParam(taskInstLogBean.getAllSize());
        DAOResult result = this.insert(DBSYNC_TASKINSTLOG_INSERT, (List)dpg.getDAOParam().get(0));
        return result.isSuccess();
    }

    /**
     * Update task conf for last boolean.
     *
     * @param jsonSyncVal the json sync val
     * @param taskId      the task id
     * @return the boolean
     */
    public boolean updateTaskConfForLast(String jsonSyncVal, long taskId) {
        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(jsonSyncVal);
        dpg.addFirstSqlParam(taskId);

        DAOResult result = this.update(DBSYNC_TASKCONF_UPDATE, (List)dpg.getDAOParam().get(0));

        return result.isSuccess();
    }

    /**
     * Update task conf for count boolean.
     *
     * @param count  the count
     * @param taskId the task id
     * @return the boolean
     */
    public boolean updateTaskConfForCount(long count, long taskId) {
        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(count);
        dpg.addFirstSqlParam(taskId);

        DAOResult result = this.update(DBSYNC_TASKCONF_UPDATE_COUNT, (List)dpg.getDAOParam().get(0));

        return result.isSuccess();
    }

    /**
     * 依据任务实例ID查询对应的日志
     *
     * @param instId the inst id
     * @return task inst log by inst id
     */
    public TaskInstLogBean getTaskInstLogByInstId(String instId) {

        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(instId);
        DAOResult drs = this.select(DBSYNC_TASKINSTLOG_INSTID, (List)dpg.getDAOParam().get(0));

        if (!drs.isSuccess() || drs.isEmpty(0)) {
            return null;
        }

        DAOResultMetaData metaData = drs.getSqlResultSetMetaData(0);
        ArrayList rowlist = drs.getFirstSqlResultFirstRow();
        TaskInstLogBean retlogbean = new TaskInstLogBean();

        retlogbean.setSyncDataSize((String) rowlist.get(metaData.getColumnIndex("SYNC_DATASIZE")));
        retlogbean.setSyncDate((Date) rowlist.get(metaData.getColumnIndex("SYNC_DATE")));
        retlogbean.setSyncInfo((String) rowlist.get(metaData.getColumnIndex("SYNC_INFO")));
        retlogbean.setSyncLastValue((String) rowlist.get(metaData.getColumnIndex("SYNC_LASTVALUE")));
        retlogbean.setSyncLogId((Long) rowlist.get(metaData.getColumnIndex("SYNCLOG_ID")));
        retlogbean.setSyncStatus(((Long) rowlist.get(metaData.getColumnIndex("SYNC_STATUS"))).intValue());
        retlogbean.setTaskId((Long) rowlist.get(metaData.getColumnIndex("TASK_ID")));
        retlogbean.setTaskInstId((String) rowlist.get(metaData.getColumnIndex("TASKINST_ID")));

        return retlogbean;
    }

    /**
     * 依据taskId返回对应同步任务配置表
     *
     * @param taskId the task id
     * @return conf task bean
     */
    public ConfTaskBean getConfTaskByTaskId(long taskId){
        ConfTaskBean confTaskBean = null;
        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(taskId);
        DAOResult drs = this.selectOnce(DBSYNC_TASKCONF_QUERYBYID, (List)dpg.getDAOParam().get(0));

        if (!drs.isSuccess()) {
            return null;
        }

        DAOResultMetaData metaData = drs.getSqlResultSetMetaData(0);
        ArrayList rowlist = drs.getFirstSqlResultFirstRow();
        confTaskBean = tranRowToConfTaskBean(rowlist, metaData);

        return confTaskBean;
    }

    /**
     * 查询配置任务状态为有效的
     *
     * @return list list
     */
    public List<ConfTaskBean> getConfTaskList(){

        DAOResult drs = this.select(DBSYNC_TASKCONF_QUERY, null);

        if (!drs.isSuccess()) {
            return null;
        }

        DAOResultMetaData metaData = drs.getSqlResultSetMetaData(0);
        ArrayList resultSet = drs.getFirstSqlResultList();
        List<ConfTaskBean> beanList = new ArrayList<ConfTaskBean>(resultSet.size());
        ArrayList rowlist = null;

        for(int i=0; i<resultSet.size(); i++){
            rowlist = (ArrayList)resultSet.get(i);
            beanList.add(tranRowToConfTaskBean(rowlist, metaData));
        }

        return beanList;
    }


    private ConfTaskBean tranRowToConfTaskBean(ArrayList rowlist,DAOResultMetaData metaData){
        ConfTaskBean confTaskBean = new ConfTaskBean();
        confTaskBean.setTaskId((Long) rowlist.get(metaData.getColumnIndex("TASK_ID")));
        confTaskBean.setSyncCycle(((Long) rowlist.get(metaData.getColumnIndex("SYNC_CYCLE"))).intValue());
        confTaskBean.setSyncLasttime((Date)rowlist.get(metaData.getColumnIndex("SYNC_LASTTIME")));
        confTaskBean.setTaskName((String) rowlist.get(metaData.getColumnIndex("TASK_NAME")));
        confTaskBean.setTaskType(((Long) rowlist.get(metaData.getColumnIndex("TASK_TYPE"))).intValue());
        confTaskBean.setLastValue((String) rowlist.get(metaData.getColumnIndex("SYNC_LASTVALUE")));
        confTaskBean.setInstCount((Long) rowlist.get(metaData.getColumnIndex("INST_COUNT")));
        confTaskBean.setTaskCreated((Date) rowlist.get(metaData.getColumnIndex("TASK_CREATED")));
        int rlIndex = metaData.getColumnIndex("ROLL_LASTVALUE");
        Long rlvalue = rlIndex==-1?null:(Long) rowlist.get(rlIndex);
        confTaskBean.setRollLastValue(rlvalue==null?0:rlvalue.intValue());
        return confTaskBean;
    }


    /**
     * 依据taskId返回对应同步任务同步表的配置
     *
     * @param taskId the task id
     * @return conf table by task id
     */
    public List<ConfTableBean> getConfTableByTaskId(long taskId) {
        List<ConfTableBean> rootbeanList = new ArrayList<ConfTableBean>();

        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(taskId);
        DAOResult drs = this.select(DBSYNC_TABLECONF_QUERYBYID, (List)dpg.getDAOParam().get(0));

        if (!drs.isSuccess()) {
            return null;
        }

        DAOResultMetaData metaData = drs.getSqlResultSetMetaData(0);
        ArrayList resultlist = drs.getFirstSqlResultList();

        List<ConfTableBean> confList = new ArrayList<ConfTableBean>();
        ConfTableBean tempbean = null;
        ArrayList dataRow = null;

        for(int i=0; i<resultlist.size(); i++) {
            dataRow = (ArrayList) resultlist.get(i);

            //第一次进入或是源端表不同，判定为下一个表的配置
            if (tempbean == null || !tempbean.getSourceTable().equals(dataRow.get(metaData.getColumnIndex("SOURCE_TABLE")))
                    || !tempbean.getTargetTable().equals(dataRow.get(metaData.getColumnIndex("TARGET_TABLE")))) {
                tempbean = new ConfTableBean();
                tempbean.setTaskId((Long)dataRow.get(metaData.getColumnIndex("TASK_ID")));
                tempbean.setDependTable((String)dataRow.get(metaData.getColumnIndex("DEPEND_TABLE")));
                tempbean.setRelateColumn((String)dataRow.get(metaData.getColumnIndex("RELATE_COLUMN")));
                tempbean.setSourceDbName((String)dataRow.get(metaData.getColumnIndex("SOURCE_DBNAME")));
                tempbean.setSourceTable((String)dataRow.get(metaData.getColumnIndex("SOURCE_TABLE")));
                tempbean.setTargetDbName((String)dataRow.get(metaData.getColumnIndex("TARGET_DBNAME")));
                tempbean.setTargetTable((String)dataRow.get(metaData.getColumnIndex("TARGET_TABLE")));

                if (tempbean.getDependTable() == null) {
                    rootbeanList.add(tempbean);
                } else {
                    confList.add(tempbean);
                }
            }

            tempbean.addConfColumn((String)dataRow.get(metaData.getColumnIndex("SOURCE_COLUMN")),
                    (String)dataRow.get(metaData.getColumnIndex("TARGET_COLUMN")),
                    ((Long)dataRow.get(metaData.getColumnIndex("INCER_TOKEN"))).intValue(),
                    ((Long)dataRow.get(metaData.getColumnIndex("PKEY_TOKEN"))).intValue(), null);
        }

        //解析主表子表的结构
        for (int i = 0; i < confList.size(); i++) {

            for (int r = 0; rootbeanList.size() > r; r++) {
                ConfTableBean rootbean = rootbeanList.get(r);
                if (rootbean.getSourceTable().equalsIgnoreCase(confList.get(i).getDependTable())) {
                    rootbean.addChildTable(confList.get(i));
                    continue;
                }
            }

            //找到父节点就加入
            for (int j = 0; j < confList.size(); j++) {
                if (j != i && confList.get(i).getDependTable().equalsIgnoreCase(confList.get(j).getSourceTable())) {
                    confList.get(j).addChildTable(confList.get(i));
                    break;
                }
            }
        }

        return rootbeanList;
    }

}
