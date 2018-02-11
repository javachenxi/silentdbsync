package cn.com.dbsync.core;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.bean.TaskInstLogBean;
import cn.com.dbsync.listener.*;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.util.SpringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据同步组件调用入口
 * <p>
 * Created by cxi on 2016/5/6.
 */
public class DBSyncContext {
    private final static Log LOG = LogFactory.getLog(DBSyncContext.class.getName());
    private static DispatchEventContainer dispatchEvent = null;
    private static ReentrantLock reenLock = new ReentrantLock();
    private static boolean isStarted = false;

    /**
     * 初始化事件分发容器
     *
     * @return
     */
    private static DispatchEventContainer startDEventContainer() {
        if (dispatchEvent == null) {
            try {
                reenLock.lock();
                if (dispatchEvent == null) {
                    isStarted = true;
                    //使用 临时变量 防止多线发布事件，在DispatchEventContainer没有初始化完成时处理事件
                    DispatchEventContainer tmpdispatchEvent = new DispatchEventContainer(new DispatchPolicy());
                    tmpdispatchEvent.registListener(EventSourceBean.EventType.STOP, new EventListenerAdapter(tmpdispatchEvent));
                    tmpdispatchEvent.registListener(EventSourceBean.EventType.START, new EventListenerAdapter(tmpdispatchEvent));
                    tmpdispatchEvent.registListener(EventSourceBean.EventType.LOADED, new LoadDataEventListenerWrap(tmpdispatchEvent));
                    tmpdispatchEvent.registListener(EventSourceBean.EventType.PERESIST, new PeresistDataEventListenerWrap(tmpdispatchEvent));
                    dispatchEvent = tmpdispatchEvent;
                }
            } finally {
                reenLock.unlock();
            }
        }
        return dispatchEvent;
    }

    /**
     * Public event.
     *
     * @param eventSourceBean the event source bean
     */
    public static void publicEvent(EventSourceBean eventSourceBean) {
        startDEventContainer().publicEvent(eventSourceBean);
    }

    /**
     * 发布数据同步停止事件
     *
     * @param taskInstId 任务实例ID
     * @param taskId     任务配置ID
     */
    public static void publicStopEvent(String taskInstId, long taskId){
        StopEventBean stopEventBean = new StopEventBean();
        stopEventBean.setTaskId(taskId);
        stopEventBean.setTaskInstId(taskInstId);
        stopEventBean.setLaunchType(StopEventBean.LaunchType.MANUL);

        publicEvent(stopEventBean);
    }

    /**
     * 发布数据同步启动事件
     *
     * @param taskInstId 任务实例ID
     * @param taskId     任务配置ID
     */
    public static void publicStartEvent(String taskInstId, long taskId){
        publicStartEvent(taskInstId,taskId,null);
    }

    /**
     * 发布数据同步启动事件
     *
     * @param taskInstId  任务实例ID
     * @param taskId      任务配置ID
     * @param eventAdvise 监听事件处理接口
     */
    public static void publicStartEvent(String taskInstId, long taskId,IEventHandleAdvise eventAdvise){

        if(taskInstId == null){
            throw new DBSyncException("任务启动初始化失败！taskInstId is null!");
        }

        DBSyncConfService confService = null;
        ConfTaskBean confTaskBean = null;

        try {
            confService = SpringManager.getInstance().getBeanByType(DBSyncConfService.class);
            confTaskBean = confService.getConfTaskByTaskId(taskId);
            //confTaskBean.setSyncInstLogId(0);
            List<ConfTableBean> confTableBeans = confService.getConfTableByTaskId(taskId);

            if(confTableBeans == null || confTableBeans.isEmpty()){
                throw new DBSyncException("同步任务未配置同步的表");
            }

            StartEventBean eventSourceBean = new StartEventBean(confTaskBean);
            eventSourceBean.setConfTableBeans(confTableBeans);
            eventSourceBean.setTaskInstId(taskInstId);
            //判断任务是否为增量同步
            if(confTaskBean.isIncTask()){
                TaskInstLogBean instLogBean = confService.getTaskInstLogByInstId(taskInstId);
                if(instLogBean != null){
                    confTaskBean.setSyncInstLogId(instLogBean.getSyncLogId());
                    confTaskBean.setLastValue(instLogBean.getSyncLastValue());
                }
            }

            eventSourceBean.setHandleAdvise(eventAdvise);
            IGeneratorSql generatorSql = IGeneratorSqlFactory.getGeneratorSql(confTableBeans, confTaskBean);
            //解析配置表生成SQL
            generatorSql.buildsql();

            publicEvent(eventSourceBean);
        }catch (Exception e){
            LOG.error("任务启动初始化失败！taskId="+taskId,e);
            if(confTaskBean != null) {
                //更新最后执行的时间
                confService.updateTaskConfForLast(confTaskBean.getLastValue(), confTaskBean.getTaskId());
            }
            throw new DBSyncException("任务启动初始化失败！taskId="+taskId,e);
        }

    }

    /**
     * 执行事件分发策略实现
     */
    private static class DispatchPolicy implements IDispatchPolicy, Runnable {

        private EventSourceBean lastEventSourBean;
        private int[] waittimes = {1000,3000,5000,7000,11000};
        private Object lock = new Object();

        /**
         * 是否为异步处理
         *
         * @return
         */
        @Override
        public boolean isASync() {
            return true;
        }

        /**
         * 进入等待
         * @param timeout
         */
        @Override
        public void waiting(long timeout) throws InterruptedException {
            synchronized (lock) {
                lock.wait(timeout);
            }
        }

        /**
         * 如果是在等待，则唤醒当前线程
         */
        @Override
        public void wakeup() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }


        @Override
        public void run() {
            this.dispatch();
        }

        @Override
        public void dispatch() {
            int counter = 0;

            while (dispatchEvent == null) {
                LOG.info("等待事件处理容器启动！");
                try {
                    this.waiting(waittimes[counter % waittimes.length]);
                } catch (InterruptedException e) {
                   // e.printStackTrace();
                }
            }

            while (DBSyncContext.isStarted) {
                try {

                    EventSourceBean tmpEventSourBean = dispatchEvent.dispatchEvent();

                    if(dispatchEvent.getIsThreadPoolFill()){
                        LOG.warn("循环处理同一事件,处理事件线程满 QueueSize=" + dispatchEvent.getQueueSize());
                        this.waiting(waittimes[counter%waittimes.length]);
                        counter++;
                    }else if(tmpEventSourBean == null) {
                        this.waiting(waittimes[waittimes.length - 1]);
                        counter = 0;
                    }

                } catch (Exception e) {
                    LOG.error("分发事件异常 ", e);
                }
            }

        }
    }


}
