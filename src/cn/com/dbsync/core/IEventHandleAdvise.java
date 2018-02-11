package cn.com.dbsync.core;

import cn.com.dbsync.listener.EventSourceBean;

/**
 * Created by cxi on 2016/5/13.
 */
public interface IEventHandleAdvise {

    /**
     * 事件前置处理
     *
     * @param event the event
     */
    void beforeHandle(EventSourceBean event);

    /**
     * 事件后置处理
     *
     * @param event the event
     */
    void finishHandle(EventSourceBean event);

}
