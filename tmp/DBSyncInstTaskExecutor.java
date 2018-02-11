package cn.com.dbsync.executor;

import cn.com.dbsync.core.DBSyncContext;
import cn.com.dbsync.core.DBSyncException;
import cn.com.dbsync.core.EventHandleAdviseAdapter;
import cn.com.dbsync.listener.EventSourceBean;
import cn.com.servyou.components.task.executor.BaseTaskExecutor;
import cn.com.servyou.components.task.object.InstanceBean;
import cn.com.servyou.components.task.object.TaskHandleResult;

/**
 * <p>
 * Title: DBSyncInstTaskExecutor
 * </p>
 * <p>
 * Description: 同步任务实例的入口
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
 * @created 2016 /5/14
 */
public class DBSyncInstTaskExecutor extends BaseTaskExecutor {

    private long startTime ;

    protected void runTask() {
        try {
            startTime = System.currentTimeMillis();
            InstanceBean instanceBean = this.getInsBean();
            DBSyncContext.publicStartEvent(instanceBean.getTaskId(), Long.parseLong(instanceBean.getModId()), new StopEventHandleAdvise(this));

        }catch (DBSyncException dbe){
            log.error("启动同步任务失败！instanceId="+ this.getInsBean().getTaskId(), dbe);
            long totalTime = System.currentTimeMillis()-startTime;
            insDao.markEnd(getTaskId(),false,dbe.getMessage(),totalTime+"");

        }
    }

    /**
     * 覆盖父类方法不做任何处理
     *
     * @param error
     * @param success
     * 成功标志
     * @return
     */
    protected String afterExe(String error, boolean success) {
        //insDao.markEnd(this.getInsBean().getTaskId(), );
        return "";
    }

    /**
     * 停止任务
     *
     * @param taskId
     * 任务id
     * @return 任务执行结果
     */
    public TaskHandleResult stop(String taskId) {
        InstanceBean instanceBean = this.getInsBean();
        DBSyncContext.publicStopEvent(instanceBean.getTaskId(),Long.parseLong(instanceBean.getModId()));
        return new TaskHandleResult();
    }

    private static class StopEventHandleAdvise extends EventHandleAdviseAdapter {
        private DBSyncInstTaskExecutor instTaskExecutor = null;

        /**
         * Instantiates a new Stop event handle advise.
         *
         * @param instTaskExecutor the inst task executor
         */
        public StopEventHandleAdvise(DBSyncInstTaskExecutor instTaskExecutor){
            this.instTaskExecutor = instTaskExecutor;
        }

        public void finishStop(EventSourceBean event){

            boolean success = true;

            if(event.getLaunchType()==EventSourceBean.LaunchType.AUTO&&event.getErrorMsg()!= null){
                success = false;
            }
            long totalTime = System.currentTimeMillis()-instTaskExecutor.startTime;
            instTaskExecutor.insDao.markEnd(instTaskExecutor.getTaskId(),success,event.getErrorMsg(),totalTime+"");

            instTaskExecutor.log.info("同步任务执行完成：" + (success?" 成功！":" 失败！")+event.toShortString());
        }
    }
}
