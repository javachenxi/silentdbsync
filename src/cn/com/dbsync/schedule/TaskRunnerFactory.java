package cn.com.dbsync.schedule;

import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.bean.TaskInstBean;

/**
 * 任务执行构造工厂
 *
 * Created by Administrator on 2017-11-01.
 */
public class TaskRunnerFactory {

    /**
     *
     * @param taskInstBean
     * @param scheduleContainer
     * @return
     */
    public static TaskRunner createTaskRunner(TaskInstBean taskInstBean, ScheduleContainer scheduleContainer){
        TaskRunner taskRunner = null;

        if(ConfTaskBean.TaskType.GENERATEDBSYNC.value == taskInstBean.getTaskType()){
            taskRunner = new GenerateDBSyncTaskRunner(taskInstBean, scheduleContainer);
        }else {
            taskRunner = new DBSyncTaskRunner(taskInstBean, scheduleContainer);
        }

        return taskRunner;
    }


}



