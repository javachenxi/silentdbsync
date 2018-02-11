package cn.com.dbsync.schedule;

import cn.com.dbsync.bean.TaskInstBean;
import cn.com.dbsync.core.DBSyncContext;
import cn.com.dbsync.core.DBSyncException;
import cn.com.dbsync.core.EventHandleAdviseAdapter;
import cn.com.dbsync.listener.EventSourceBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Administrator on 2017-11-01.
 */
public class DBSyncTaskRunner extends TaskRunner{

    private final static Log LOG = LogFactory.getLog(TaskRunner.class.getName());

    public DBSyncTaskRunner(TaskInstBean taskInstBean, ScheduleContainer scheduleContainer){
        super(taskInstBean, scheduleContainer);
    }

    @Override
    public void proccess() {
        try {

            DBSyncContext.publicStartEvent(taskInstBean.getTaskInstId()+"", taskInstBean.getTaskId(), new StopEventHandleAdvise(this));

        }catch (DBSyncException dbe){
            String errorMsg = "启动同步任务失败！taskInstId="+ taskInstBean.getTaskInstId();
            LOG.error("启动同步任务失败！taskInstId="+ taskInstBean.getTaskInstId(), dbe);
            long totalTime = System.currentTimeMillis()-startTime;
            scheduleContainer.saveFinishInfo(taskInstBean.getTaskInstId(),TaskInstBean.Status.ERROR.value,
                                 totalTime, errorMsg+dbe.getMessage());
        }
    }

    private static class StopEventHandleAdvise extends EventHandleAdviseAdapter {
        private DBSyncTaskRunner taskRunner = null;

        /**
         * Instantiates a new Stop event handle advise.
         *
         * @param taskRunner the inst task executor
         */
        public StopEventHandleAdvise(DBSyncTaskRunner taskRunner){
            this.taskRunner = taskRunner;
        }

        public void finishStop(EventSourceBean event){

            boolean success = true;

            if(event.getLaunchType()==EventSourceBean.LaunchType.AUTO&&event.getErrorMsg()!= null){
                success = false;
            }

            long totalTime = System.currentTimeMillis()-taskRunner.startTime;
            int status = success? TaskInstBean.Status.SUCC.value:TaskInstBean.Status.ERROR.value;
            String errorMsg = "同步任务执行完成：" + (success?" 成功！":" 失败！")+event.toShortString();
            taskRunner.scheduleContainer.saveFinishInfo(taskRunner.taskInstBean.getTaskInstId(),status, totalTime, errorMsg);

            LOG.info(errorMsg);
        }
    }
}
