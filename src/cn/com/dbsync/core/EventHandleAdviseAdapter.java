package cn.com.dbsync.core;

import cn.com.dbsync.listener.EventSourceBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by cxi on 2016/5/13.
 */
public class EventHandleAdviseAdapter implements IEventHandleAdvise{

    private final static Log LOG = LogFactory.getLog(EventHandleAdviseAdapter.class.getName());

    /**
     * �¼�ǰ�ô���
     *
     * @param event
     */
    @Override
    public void beforeHandle(EventSourceBean event) {
    }

    /**
     * �¼����ô���
     *
     * @param event
     */
    @Override
    public void finishHandle(EventSourceBean event) {
        switch (event.getStype()) {
            case LOADED:
                this.finishLoad(event);
                break;
            case STOP:
                this.finishStop(event);
                break;
            case START:
                this.finishStart(event);
                break;
            case PERESIST:
                this.finishPeresist(event);
                break;
            default:
                LOG.warn("��Ч�¼���������! stype=" + event.getStype());
        }
    }

    /**
     * Finish load.
     *
     * @param event the event
     */
    public void finishLoad(EventSourceBean event){

    }

    /**
     * Finish stop.
     *
     * @param event the event
     */
    public void finishStop(EventSourceBean event){

    }

    /**
     * Finish start.
     *
     * @param event the event
     */
    public void finishStart(EventSourceBean event){

    }

    /**
     * Finish peresist.
     *
     * @param event the event
     */
    public void finishPeresist(EventSourceBean event){

    }
}
