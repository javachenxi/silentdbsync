package cn.com.dbsync.core;

/**
 * Created by cxi on 2016/5/11.
 */
public interface IDispatchPolicy {


    /**
     * 分发事件的策略实现
     */
    public void dispatch();

    /**
     * 是否为异步处理
     *
     * @return boolean boolean
     */
    public boolean isASync();


    /**
     * 进入等待
     *
     * @param timeout the timeout
     * @throws InterruptedException the interrupted exception
     */
    public void waiting(long timeout) throws InterruptedException;

    /**
     * 如果是在等待，则唤醒当前线程
     */
    public void wakeup();

}
