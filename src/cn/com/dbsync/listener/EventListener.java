package cn.com.dbsync.listener;

/**
 * Created by cxi on 2016/5/4.
 *
 * @param <T> the type parameter
 */
public interface EventListener<T> {

    /**
     * Handle.
     *
     * @param e the e
     */
    public void handle(T e);

}
