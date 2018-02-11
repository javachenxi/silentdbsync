package cn.com.dbsync.core;

import cn.com.dbsync.listener.EventSourceBean;

/**
 * Created by cxi on 2016/5/13.
 */
public interface IEventHandleAdvise {

    /**
     * �¼�ǰ�ô���
     *
     * @param event the event
     */
    void beforeHandle(EventSourceBean event);

    /**
     * �¼����ô���
     *
     * @param event the event
     */
    void finishHandle(EventSourceBean event);

}
