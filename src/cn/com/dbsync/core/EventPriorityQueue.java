package cn.com.dbsync.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by cxi on 2016/5/4.
 *
 * @param <T> the type parameter
 */
public class EventPriorityQueue<T> {

    private final static Log LOG = LogFactory.getLog(EventPriorityQueue.class.getName());

    private static final int DEFAULT_MAX_SIZE = 100;
    private static final int DEFAULT_UNIT_WAIT = 5000;

    private LinkedList<T> eventQueue = null;
    private int maxSize = 0;
    private boolean isFixed = false;

    /**
     * Instantiates a new Event priority queue.
     */
    public EventPriorityQueue(){
        this(0);
    }

    /**
     * Instantiates a new Event priority queue.
     *
     * @param size the size
     */
    public EventPriorityQueue(int size){
        if(size > 0){
            maxSize = size ;
            isFixed = true;
        }else{
            maxSize = DEFAULT_MAX_SIZE;
        }

        eventQueue = new LinkedList<T>();
    }

    /**
     * Add boolean.
     *
     * @param t the t
     * @return the boolean
     */
    public boolean add(T t){
        return add(t, 0);
    }

    /**
     * 新增对象到队列中，队列满时，在timeout时间内不能加入时，返回加入失败
     *
     * @param t       the t
     * @param timeout the timeout
     * @return boolean boolean
     */
    public boolean add(T t, int timeout){

        int temptimeout = DEFAULT_UNIT_WAIT>timeout?DEFAULT_UNIT_WAIT:timeout;
        boolean ret = false;

        synchronized (eventQueue) {

            if (!isFixed||eventQueue.size() < maxSize ) {

                eventQueue.addLast(t);
                ret = true;
            } else {

                while (eventQueue.size() >= maxSize && temptimeout > 0) {
                    try {
                        eventQueue.wait(DEFAULT_UNIT_WAIT);
                    } catch (InterruptedException e) {
                        LOG.warn("线程被唤醒!");
                    }

                    temptimeout -= DEFAULT_UNIT_WAIT;
                }

                if(eventQueue.size() < maxSize){
                    eventQueue.addLast(t);
                    ret = true;
                }

            }
        }

        return ret;
    }

    /**
     * Add first.
     *
     * @param t the t
     */
    public void addFirst(T t){
        synchronized (eventQueue) {
            eventQueue.addFirst(t);
        }
    }

    /**
     * Remove.
     *
     * @param t the t
     */
    public void remove(T t){
        synchronized (eventQueue) {
            eventQueue.remove(t);
        }
    }

    /**
     * Remove all.
     *
     * @param list the list
     */
    public void removeAll(List<T> list){
        synchronized (eventQueue) {
            eventQueue.removeAll(list);
        }
    }

    /**
     * Iterator iterator.
     *
     * @return the iterator
     */
    public Iterator<T> iterator(){

        Iterator<T> it = eventQueue.iterator();

        return it;
    }

    /**
     * 取得队列第一个，并删除该对象
     *
     * @return t t
     */
    public T poll(){

        T event = null;

        synchronized (eventQueue) {
            event = eventQueue.poll();
            eventQueue.notifyAll();
        }

        return event;
    }

    /**
     * 取得队列第一个，但不删除该对象
     *
     * @return t t
     */
    public T peek(){
        return eventQueue.peek();
    }


    /**
     * Size int.
     *
     * @return the int
     */
    public int size(){
        return eventQueue.size();
    }

}
