package cn.com.dbsync.core;


import cn.com.dbsync.listener.EventListener;
import cn.com.dbsync.listener.EventSourceBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cxi on 2016/5/4.
 */
public class DispatchEventContainer {

    private final static Log LOG = LogFactory.getLog(DispatchEventContainer.class.getName());

    private EventPriorityQueue<EventBeanWrap<EventSourceBean>> epqueue = null;
    private Map<EventSourceBean.EventType, List<EventListener<EventSourceBean>>> listenerMap = null;
    private ThreadPoolExecutor threadPoolExecutor = null;
    private boolean isThreadPoolFill = false;
    private IDispatchPolicy dispatchPolicy;

    /**
     * Instantiates a new Dispatch event container.
     *
     * @param dispatchPolicy the dispatch policy
     */
    public DispatchEventContainer(IDispatchPolicy dispatchPolicy){
        epqueue = new EventPriorityQueue<EventBeanWrap<EventSourceBean>>();
        listenerMap = new HashMap<EventSourceBean.EventType, List<EventListener<EventSourceBean>>>(16);
        threadPoolExecutor = new ThreadPoolExecutor(4,8,60, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),
                new DispatchThreadFactory(), new DispathchREHendler(this));

        if(dispatchPolicy != null) {
            if (dispatchPolicy.isASync()) {
                threadPoolExecutor.execute((Runnable) dispatchPolicy);
            } else {
                dispatchPolicy.dispatch();
            }
            this.dispatchPolicy = dispatchPolicy;
        }
    }

    /**
     * Instantiates a new Dispatch event container.
     */
    public DispatchEventContainer(){
        this(null);
    }


    /**
     * Public event.
     *
     * @param e the e
     */
    public void publicEvent(EventSourceBean e){
        EventBeanWrap<EventSourceBean> eventBeanWarp = new EventBeanWrap<EventSourceBean>(e, e.getStype());
        epqueue.add(eventBeanWarp);

        if(dispatchPolicy != null && dispatchPolicy.isASync()){
            dispatchPolicy.wakeup();
        }

        LOG.info("�����¼��ɹ���" + e.toShortString());
    }

    /**
     * ���������зַ���ֱ�ӵ��ã�������ͬ���¼�
     *
     * @param e the e
     * @return event source bean
     */
    public EventSourceBean dispatchEvent(EventSourceBean e){
        EventBeanWrap<EventSourceBean> eventBeanWarp = new EventBeanWrap<EventSourceBean>(e, e.getStype());
       return dispatchEvent(eventBeanWarp);
    }

    /**
     * �Ӷ�����ȡ������Ȼ��ַ�����
     *
     * @return event source bean
     */
    public EventSourceBean dispatchEvent(){
        EventBeanWrap<EventSourceBean> eventBeanWarp = epqueue.poll();
        return dispatchEvent(eventBeanWarp);
    }

    /**
     * �ַ��¼�����
     * @param eventBeanWarp
     */
    private EventSourceBean dispatchEvent(EventBeanWrap<EventSourceBean> eventBeanWarp){

        EventSourceBean sourceBean = null;

        if(eventBeanWarp != null){
            sourceBean = (EventSourceBean) eventBeanWarp.getEsource();
            LOG.info("�ַ��¼��ɹ���" + sourceBean.toShortString());

            List<EventListener<EventSourceBean>> list = getListenerList(sourceBean.getStype());

            if(list == null){
                LOG.info("�¼���������Ϣ��" + listenerMap.toString());
            }

            if(!sourceBean.isSync()){
                isThreadPoolFill = false;
                AsyncDispatchEvent asyncDispatchEvent = new AsyncDispatchEvent(list, eventBeanWarp);
                threadPoolExecutor.execute(asyncDispatchEvent);
            } else{
                listenHandle(list, eventBeanWarp);
            }

        }

        return sourceBean;
    }

    /**
     * Get is thread pool fill boolean.
     *
     * @return the boolean
     */
    public boolean getIsThreadPoolFill(){
        return isThreadPoolFill;
    }

    /**
     * Get queue size int.
     *
     * @return the int
     */
    public int getQueueSize(){
        return epqueue.size();
    }

    private void listenHandle(List<EventListener<EventSourceBean>> list, EventBeanWrap<EventSourceBean> eventBeanWarp){
        Iterator<EventListener<EventSourceBean>> it = list.iterator();
        EventListener<EventSourceBean> eventListener = null;

        while(it.hasNext()){

            eventListener = it.next();

            if(eventListener != null){
                eventListener.handle((EventSourceBean) eventBeanWarp.getEsource());
            }
        }
    }

    /**
     * ���¼�����ע������������б�
     *
     * @param stype the stype
     * @param el    the el
     */
    public void registListener(EventSourceBean.EventType stype, EventListener<EventSourceBean> el){

        List<EventListener<EventSourceBean>> list = listenerMap.get(stype);

        if(list == null){
            synchronized (listenerMap) {

                list = listenerMap.get(stype);

                if(list == null) {
                    list =  Collections.synchronizedList(new LinkedList<EventListener<EventSourceBean>>());
                    listenerMap.put(stype, list);
                }
            }
        }

        list.add(el);
    }

    /**
     * ȡ���ض������¾������������
     *
     * @param stype the stype
     * @param el    the el
     */
    public void unregistListener(EventSourceBean.EventType stype, EventListener<EventSourceBean> el){

        List<EventListener<EventSourceBean>> list = listenerMap.get(stype);

        if(list == null){
            return ;
        }

        Iterator<EventListener<EventSourceBean>> it = list.iterator();
        EventListener<EventSourceBean> tempListener = null;

       while(it.hasNext()){
            tempListener = it.next();
            if(tempListener == el){
                it.remove();
                break;
            }
        }

        return;

    }

    /**
     * ֹͣ�¼�����ʱ������������Ѿ�������ͬһ���¼�����
     *
     * @param eventId the event id
     */
    public void clearEventBeanByEventId(Object eventId){
        Iterator<EventBeanWrap<EventSourceBean>> it = epqueue.iterator();
        EventBeanWrap<EventSourceBean> eventBeanWarp = null;
        List<EventBeanWrap<EventSourceBean>> removeList = new ArrayList<EventBeanWrap<EventSourceBean>>();

        while(it.hasNext()){
            eventBeanWarp = it.next();

            if(eventId.equals(eventBeanWarp.getEsource().getEventId())){
                removeList.add(eventBeanWarp);
            }
        }
        if(!removeList.isEmpty()) {
            epqueue.removeAll(removeList);
        }
    }

    private List<EventListener<EventSourceBean>> getListenerList(EventSourceBean.EventType stype){
        List<EventListener<EventSourceBean>> list = listenerMap.get(stype);
        return list;
    }

    /**
     * Gets thread pool executor.
     *
     * @return the thread pool executor
     */
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    /**
     * Sets thread pool executor.
     *
     * @param threadPoolExecutor the thread pool executor
     */
    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * �첽�����¼�
     */
    private class AsyncDispatchEvent implements Runnable {

        private List<EventListener<EventSourceBean>> list = null;
        private EventBeanWrap<EventSourceBean> eventBeanWarp = null;

        /**
         * Instantiates a new Async dispatch event.
         *
         * @param list          the list
         * @param eventBeanWarp the event bean warp
         */
        public AsyncDispatchEvent(List<EventListener<EventSourceBean>> list, EventBeanWrap<EventSourceBean> eventBeanWarp){
            this.list = list;
            this.eventBeanWarp = eventBeanWarp;
        }

        @Override
        public void run() {
            listenHandle(this.list, eventBeanWarp);
        }

        /**
         * Get event bean warp event bean wrap.
         *
         * @return the event bean wrap
         */
        public EventBeanWrap<EventSourceBean> getEventBeanWarp(){
            return eventBeanWarp;
        }

    }

    /**
     * �����̳߳صĴ�������
     */
    private class DispatchThreadFactory implements ThreadFactory {

        private static final String THREAD_NAME_PREFIX = "EventHandle-";
        private final AtomicLong THREAD_INDEX = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,THREAD_NAME_PREFIX + THREAD_INDEX.getAndIncrement());
        }
    }

    /**
     * �첽�¼������ܼ����̳߳�ʱ���쳣����
     */
    private class DispathchREHendler implements RejectedExecutionHandler {

        private DispatchEventContainer eventContainer;

        /**
         * Instantiates a new Dispathch re hendler.
         *
         * @param eventContainer the event container
         */
        public DispathchREHendler(DispatchEventContainer eventContainer){
            this.eventContainer = eventContainer;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            AsyncDispatchEvent asyncEvent = (AsyncDispatchEvent) r;
            eventContainer.epqueue.add(asyncEvent.getEventBeanWarp());
            eventContainer.isThreadPoolFill=true;
        }

    }



}











