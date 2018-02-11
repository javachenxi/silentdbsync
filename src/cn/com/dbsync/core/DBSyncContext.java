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
 * ����ͬ������������
 * <p>
 * Created by cxi on 2016/5/6.
 */
public class DBSyncContext {
    private final static Log LOG = LogFactory.getLog(DBSyncContext.class.getName());
    private static DispatchEventContainer dispatchEvent = null;
    private static ReentrantLock reenLock = new ReentrantLock();
    private static boolean isStarted = false;

    /**
     * ��ʼ���¼��ַ�����
     *
     * @return
     */
    private static DispatchEventContainer startDEventContainer() {
        if (dispatchEvent == null) {
            try {
                reenLock.lock();
                if (dispatchEvent == null) {
                    isStarted = true;
                    //ʹ�� ��ʱ���� ��ֹ���߷����¼�����DispatchEventContainerû�г�ʼ�����ʱ�����¼�
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
     * ��������ͬ��ֹͣ�¼�
     *
     * @param taskInstId ����ʵ��ID
     * @param taskId     ��������ID
     */
    public static void publicStopEvent(String taskInstId, long taskId){
        StopEventBean stopEventBean = new StopEventBean();
        stopEventBean.setTaskId(taskId);
        stopEventBean.setTaskInstId(taskInstId);
        stopEventBean.setLaunchType(StopEventBean.LaunchType.MANUL);

        publicEvent(stopEventBean);
    }

    /**
     * ��������ͬ�������¼�
     *
     * @param taskInstId ����ʵ��ID
     * @param taskId     ��������ID
     */
    public static void publicStartEvent(String taskInstId, long taskId){
        publicStartEvent(taskInstId,taskId,null);
    }

    /**
     * ��������ͬ�������¼�
     *
     * @param taskInstId  ����ʵ��ID
     * @param taskId      ��������ID
     * @param eventAdvise �����¼�����ӿ�
     */
    public static void publicStartEvent(String taskInstId, long taskId,IEventHandleAdvise eventAdvise){

        if(taskInstId == null){
            throw new DBSyncException("����������ʼ��ʧ�ܣ�taskInstId is null!");
        }

        DBSyncConfService confService = null;
        ConfTaskBean confTaskBean = null;

        try {
            confService = SpringManager.getInstance().getBeanByType(DBSyncConfService.class);
            confTaskBean = confService.getConfTaskByTaskId(taskId);
            //confTaskBean.setSyncInstLogId(0);
            List<ConfTableBean> confTableBeans = confService.getConfTableByTaskId(taskId);

            if(confTableBeans == null || confTableBeans.isEmpty()){
                throw new DBSyncException("ͬ������δ����ͬ���ı�");
            }

            StartEventBean eventSourceBean = new StartEventBean(confTaskBean);
            eventSourceBean.setConfTableBeans(confTableBeans);
            eventSourceBean.setTaskInstId(taskInstId);
            //�ж������Ƿ�Ϊ����ͬ��
            if(confTaskBean.isIncTask()){
                TaskInstLogBean instLogBean = confService.getTaskInstLogByInstId(taskInstId);
                if(instLogBean != null){
                    confTaskBean.setSyncInstLogId(instLogBean.getSyncLogId());
                    confTaskBean.setLastValue(instLogBean.getSyncLastValue());
                }
            }

            eventSourceBean.setHandleAdvise(eventAdvise);
            IGeneratorSql generatorSql = IGeneratorSqlFactory.getGeneratorSql(confTableBeans, confTaskBean);
            //�������ñ�����SQL
            generatorSql.buildsql();

            publicEvent(eventSourceBean);
        }catch (Exception e){
            LOG.error("����������ʼ��ʧ�ܣ�taskId="+taskId,e);
            if(confTaskBean != null) {
                //�������ִ�е�ʱ��
                confService.updateTaskConfForLast(confTaskBean.getLastValue(), confTaskBean.getTaskId());
            }
            throw new DBSyncException("����������ʼ��ʧ�ܣ�taskId="+taskId,e);
        }

    }

    /**
     * ִ���¼��ַ�����ʵ��
     */
    private static class DispatchPolicy implements IDispatchPolicy, Runnable {

        private EventSourceBean lastEventSourBean;
        private int[] waittimes = {1000,3000,5000,7000,11000};
        private Object lock = new Object();

        /**
         * �Ƿ�Ϊ�첽����
         *
         * @return
         */
        @Override
        public boolean isASync() {
            return true;
        }

        /**
         * ����ȴ�
         * @param timeout
         */
        @Override
        public void waiting(long timeout) throws InterruptedException {
            synchronized (lock) {
                lock.wait(timeout);
            }
        }

        /**
         * ������ڵȴ������ѵ�ǰ�߳�
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
                LOG.info("�ȴ��¼���������������");
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
                        LOG.warn("ѭ������ͬһ�¼�,�����¼��߳��� QueueSize=" + dispatchEvent.getQueueSize());
                        this.waiting(waittimes[counter%waittimes.length]);
                        counter++;
                    }else if(tmpEventSourBean == null) {
                        this.waiting(waittimes[waittimes.length - 1]);
                        counter = 0;
                    }

                } catch (Exception e) {
                    LOG.error("�ַ��¼��쳣 ", e);
                }
            }

        }
    }


}
