package cn.com.dbsync.schedule;

import cn.com.dbsync.bean.TaskInstBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Administrator on 2017-11-01.
 */
public abstract class TaskRunner implements Runnable{

    private final static Log LOG = LogFactory.getLog(TaskRunner.class.getName());

    protected TaskInstBean taskInstBean;

    protected ScheduleContainer scheduleContainer;

    protected long startTime ;

    protected boolean stopped;

    public TaskRunner(TaskInstBean taskInstBean, ScheduleContainer scheduleContainer){
        this.taskInstBean = taskInstBean;
        this.scheduleContainer = scheduleContainer;
        this.stopped = false;
    }

    public void stop(){
        this.stopped = true;
    }


    public void run(){
        try {
            preProccess();
            proccess();
        }catch (Exception e){
            LOG.error("执行任务失败！", e);
        }finally {
            postProccess();
        }
    }

    public void preProccess(){
        startTime = System.currentTimeMillis();
        scheduleContainer.saveStartInfo(taskInstBean.getTaskInstId(), "启动任务成功");
    }

    public void postProccess(){

    }

    /**
     * 执行具体任务
     */
    public abstract void proccess();

    public TaskInstBean getTaskInstBean() {
        return taskInstBean;
    }

    public void setTaskInstBean(TaskInstBean taskInstBean) {
        this.taskInstBean = taskInstBean;
    }

    public ScheduleContainer getScheduleContainer() {
        return scheduleContainer;
    }

    public void setScheduleContainer(ScheduleContainer scheduleContainer) {
        this.scheduleContainer = scheduleContainer;
    }
}
