package cn.com.dbsync.core;

import cn.com.dbsync.listener.EventSourceBean;

/**
 * Created by cxi on 2016/5/8.
 */
public class DBSyncException extends RuntimeException{

    private EventSourceBean.EventType eventType = EventSourceBean.EventType.RESERVER;

    /**
     * Instantiates a new Db sync exception.
     */
    public DBSyncException() {
        super();
    }

    /**
     * Instantiates a new Db sync exception.
     *
     * @param message the message
     */
    public DBSyncException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Db sync exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public DBSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Db sync exception.
     *
     * @param message   the message
     * @param cause     the cause
     * @param eventType the event type
     */
    public DBSyncException(String message, Throwable cause, EventSourceBean.EventType eventType) {
        super(message, cause);
        this.eventType = eventType;
    }

    /**
     * Instantiates a new Db sync exception.
     *
     * @param cause the cause
     */
    public DBSyncException(Throwable cause) {
        super(cause);
    }

    /**
     * Gets event type.
     *
     * @return the event type
     */
    public EventSourceBean.EventType getEventType() {
        return eventType;
    }

    /**
     * Sets event type.
     *
     * @param eventType the event type
     */
    public void setEventType(EventSourceBean.EventType eventType) {
        this.eventType = eventType;
    }
}
