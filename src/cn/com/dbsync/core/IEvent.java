package cn.com.dbsync.core;

/**
 * Created by cxi on 2016/5/11.
 *
 * @param <T> the type parameter
 */
public interface IEvent<T> {

    /**
     * Gets event id.
     *
     * @return the event id
     */
    public T getEventId();
}
