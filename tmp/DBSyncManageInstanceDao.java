package cn.com.dbsync.dao;


import cn.com.dbsync.util.DBSyncConstant;
import cn.com.dbsync.util.DBUtil;

import java.util.List;

/**
 * <p>
 * Title: DBSyncInstanceDao
 * </p>
 * <p>
 * Description:任务实例管理的操作
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009-2016
 * </p>
 * <p>
 * Company: servyou
 * </p>
 *
 * @author cxi
 * @version 1.0
 * @created 2016 /5/15
 */
public class DBSyncManageInstanceDao extends DBSimpleTemplate{

    private static final String TASK_INSTANCE_UPDATEBYID = "taskUpdateResetRunByTaskId";
    private static final String TASK_INSTANCE_QUERYBYMODID = "queryInstanceByModId";
    private static final String TASK_INSTANCE_INSERT = "dbsyncTaskInstanceInsert";


    /**
     * 检查模型ID对应的实例是否存在了
     *
     * @param modId the mod id
     * @return boolean boolean
     */
    public boolean getInstanceByModid(String modId){
        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(modId);
        dpg.addFirstSqlParam(DBSyncConstant.TASKINST_EXECUTOR_KEY);

        DAOResult daoResult = this.selectOnce(TASK_INSTANCE_QUERYBYMODID, (List)dpg.getDAOParam().get(0));

        if(daoResult.isSuccess()&&!daoResult.isEmpty(0)){
            return true;
        }

        return false;
    }

    /**
     * 新增任务实例
     *
     * @param instanceBean the instance bean
     * @return boolean boolean
     */
    public boolean insertInstance(InstanceBean instanceBean){

        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(instanceBean.getTaskName());
        dpg.addFirstSqlParam(instanceBean.getTaskProp());
        dpg.addFirstSqlParam(instanceBean.getTaskType());
        dpg.addFirstSqlParam(instanceBean.getExeType());
        dpg.addFirstSqlParam(DBUtil.parseStrToDate(instanceBean.getPlanTime()));
        dpg.addFirstSqlParam(instanceBean.getModId());
        dpg.addFirstSqlParam(DBSyncConstant.TASKINST_EXECUTOR_KEY);

        DAOResult daoResult = this.update(TASK_INSTANCE_INSERT, (List) dpg.getDAOParam().get(0));

        if(daoResult.isSuccess()){
            return true;
        }

        return false;
    }

    /**
     * 重置Taskconf的任务
     *
     * @param instanceId the instance id
     * @return boolean boolean
     */
    public boolean resetRunByInstanceId(String instanceId){

        DAOParamGenerator dpg = new DAOParamGenerator();
        dpg.addFirstSqlParam(instanceId);
        DAOResult daoResult = this.update(TASK_INSTANCE_UPDATEBYID, (List) dpg.getDAOParam().get(0));

        if(daoResult.isSuccess()){
            return true;
        }

        return false;
    }



}
