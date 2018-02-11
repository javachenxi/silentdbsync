package cn.com.dbsync.listener;

/**
 * Created by cxi on 2016/5/12.
 */
public class StopEventBean extends EventSourceBean{

    /**
     * Instantiates a new Stop event bean.
     */
    public StopEventBean() {
        super(EventType.STOP);
    }

    /**
     * Instantiates a new Stop event bean.
     *
     * @param e the e
     */
    public StopEventBean(EventSourceBean e){
        super(e, EventType.STOP);
    }
}
