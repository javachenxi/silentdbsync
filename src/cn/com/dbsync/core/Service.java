package cn.com.dbsync.core;

/**
 * Created by cxi on 2016/5/5.
 *
 * @param <T> the type parameter
 */
public interface Service<T> {

    /**
     * Load.
     *
     * @param e the e
     */
    public void load(T e);

    /**
     * Peresist.
     *
     * @param e the e
     */
    public void peresist(T e);

    /**
     * Start.
     *
     * @param e the e
     */
    public void start(T e);

    /**
     * Stop.
     *
     * @param e the e
     */
    public void stop(T e);

    /**
     * Pause.
     *
     * @param e the e
     */
    public void pause(T e);

    /**
     * Resume.
     *
     * @param e the e
     */
    public void resume(T e);

}
