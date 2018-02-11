package cn.com.dbsync.schedule;

import cn.com.dbsync.bean.TaskInstBean;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.service.DBSyncInstService;
import cn.com.dbsync.util.CommUtil;
import cn.com.dbsync.util.SpringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017-10-31.
 */
public class ScheduleContainer {

    private final static Log LOG = LogFactory.getLog(ScheduleContainer.class.getName());
    private ThreadPoolExecutor threadPoolExecutor = null;
    private DBSyncInstService dbSyncInstService = null;
    private DBSyncConfService dbSyncConfService = null;
    private boolean isThreadPoolFill = false;
    private boolean isFirst = true;
    private boolean isRunning = false;

    private static ReentrantLock reenLock = new ReentrantLock();
    private static ScheduleContainer scheduleContainer = null;

    public static ScheduleContainer getInstance(){

        if(scheduleContainer == null){
            try{
                reenLock.lock();

                if(scheduleContainer == null){
                    scheduleContainer = new ScheduleContainer();
                }
            }finally {
                reenLock.unlock();
            }
        }

        return scheduleContainer;
    }

    public void start(){
        if(!isRunning){
            synchronized (this) {
                if(!isRunning) {
                    isRunning = true;
                    threadPoolExecutor.execute(new ScheduleRunner(this));
                }
            }
        }
    }

    public void stop(){
        isRunning = isRunning? false:isRunning;
    }


    private ScheduleContainer(){
        threadPoolExecutor = new ThreadPoolExecutor(4,8,60, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),
                new ScheduleThreadFactory(), new ScheduleREHendler(this));
        dbSyncInstService = SpringManager.getInstance().getBeanByType(DBSyncInstService.class);
        dbSyncConfService = SpringManager.getInstance().getBeanByType(DBSyncConfService.class);
    }

    public DBSyncInstService getDbSyncInstService() {
        return dbSyncInstService;
    }

    public void setDbSyncInstService(DBSyncInstService dbSyncInstService) {
        this.dbSyncInstService = dbSyncInstService;
    }

    public DBSyncConfService getDbSyncConfService() {
        return dbSyncConfService;
    }

    public void setDbSyncConfService(DBSyncConfService dbSyncConfService) {
        this.dbSyncConfService = dbSyncConfService;
    }

    private class ScheduleRunner implements Runnable {

        private ScheduleContainer scheduleContainer;

        private int[] waittimes = {1000,3000,5000,7000,11000};
        private Object lock = new Object();

        public ScheduleRunner(ScheduleContainer scheduleContainer){
             this.scheduleContainer = scheduleContainer;
        }

        @Override
        public void run(){

            LOG.info("启动任务管理器成功");

            List<TaskInstBean> taskInstBeanList = null;

            TaskInstBean tmpTaskInst = null;
            int counter = 0;
            int index = 0;

            while(isRunning) {
                try {
                    if (!isThreadPoolFill) {
                        if (isFirst) {
                            //第一次执行查询，上次退出时，处于正在处理的任务
                            taskInstBeanList = dbSyncInstService.queryTaskInstListByStatus(TaskInstBean.Status.HANDLING.value);
                            isFirst = false;
                        } else if(taskInstBeanList == null || taskInstBeanList.isEmpty()){
                            //轮询执行查询，未处理的任务
                            taskInstBeanList = dbSyncInstService.queryTaskInstListByStatus(TaskInstBean.Status.UNHANDLE.value);
                        }
                    }

                    if (taskInstBeanList == null || taskInstBeanList.isEmpty() ||isThreadPoolFill) {
                        this.waiting(waittimes[counter % waittimes.length]);
                        counter++;

                        if(isThreadPoolFill) {
                            isThreadPoolFill = false;
                        }

                        continue;
                    }

                    do{

                        tmpTaskInst = taskInstBeanList.get(index);
                        this.scheduleContainer.dispatchTaskRunner(tmpTaskInst);

                    }while(!isThreadPoolFill && taskInstBeanList.size() > ++index);

                    if(taskInstBeanList.size() <= index){
                        taskInstBeanList = null;
                        index = 0;
                        counter = 0;
                    }

                } catch (Exception e) {
                    LOG.error("守护线程异常！", e);
                }
            }

            LOG.info("退出任务管理器成功");
        }

        /**
         * 进入等待
         * @param timeout
         */
        public void waiting(long timeout) throws InterruptedException {
            synchronized (lock) {
                lock.wait(timeout);
            }
        }

    }

    private void dispatchTaskRunner(TaskInstBean taskInstBean){
        TaskRunner taskRunner = TaskRunnerFactory.createTaskRunner(taskInstBean, this);
        threadPoolExecutor.execute(taskRunner);

        if(!this.isThreadPoolFill){
            if(taskInstBean.getStatus() == TaskInstBean.Status.HANDLING.value ){
                return ;
            }
            //切换为正在运行状态
            scheduleContainer.switchToHandling(taskInstBean.getTaskInstId());
        }
    }

    public boolean switchToHandling(long taskInst){
       return switchTaskInstStatus(taskInst, TaskInstBean.Status.UNHANDLE.value,
               TaskInstBean.Status.HANDLING.value);
    }

    public boolean switchTaskInstStatus(long taskInst, int orgStatus, int toStatus){
        return dbSyncInstService.updateTaskInstToStatusById(taskInst, orgStatus, toStatus);
    }

    public boolean saveStartInfo(long taskInst,String currentMsg){
        TaskInstBean tmpTaskInstBean = new TaskInstBean();
        tmpTaskInstBean.setTaskInstId(taskInst);
        tmpTaskInstBean.setBeginTime(dbSyncInstService.getDBCurrDate().getTime());
        tmpTaskInstBean.setCurrentMsg(currentMsg);
        return dbSyncInstService.updateDynaTaskInstById(tmpTaskInstBean);
    }

    public boolean saveFinishInfo(long taskInst, int status,long totalWaste, String errorMsg){
        TaskInstBean tmpTaskInstBean = new TaskInstBean();
        tmpTaskInstBean.setTaskInstId(taskInst);
        tmpTaskInstBean.setStatus(status);
        tmpTaskInstBean.setEndTime(dbSyncInstService.getDBCurrDate().getTime());
        tmpTaskInstBean.setTotalWaste(totalWaste);
        tmpTaskInstBean.setErrorMsg(CommUtil.subFixedStr(errorMsg));
        return dbSyncInstService.updateDynaTaskInstById(tmpTaskInstBean);
    }

    public boolean updateDynaTaskInstById(TaskInstBean taskInstBean){
        return dbSyncInstService.updateDynaTaskInstById(taskInstBean);
    }

    /**
     * 定义线程池的创建工厂
     */
    private class ScheduleThreadFactory implements ThreadFactory {

        private static final String THREAD_NAME_PREFIX = "Schedule-";
        private final AtomicLong THREAD_INDEX = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,THREAD_NAME_PREFIX + THREAD_INDEX.getAndIncrement());
        }
    }

    /**
     * 异步事件处理不能加入线程池时，异常处理
     */
    private class ScheduleREHendler implements RejectedExecutionHandler {

        private ScheduleContainer scheduleContainer;

        /**
         * Instantiates a new Dispathch re hendler.
         *
         * @param scheduleContainer the event container
         */
        public ScheduleREHendler(ScheduleContainer scheduleContainer){
            this.scheduleContainer = scheduleContainer;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            scheduleContainer.isThreadPoolFill=true;
        }

    }

}
