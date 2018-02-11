package cn.com.dbsync.listener;

import cn.com.dbsync.core.*;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.util.SpringManager;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by cxi on 2016/5/6.
 */
public class EventListenerAdapter implements EventListener<EventSourceBean>,Service<EventSourceBean> {

    private final static Log LOG = LogFactory.getLog(EventListenerAdapter.class.getName());

    /**
     * The Dispatch event container.
     */
    protected DispatchEventContainer dispatchEventContainer;
    private static Map<String, AtomicBoolean> statusMap = new HashedMap();
    private DBSyncConfService dbSyncConfService;

    /**
     * Instantiates a new Event listener adapter.
     *
     * @param dispatchEventContainer the dispatch event container
     */
    public EventListenerAdapter(DispatchEventContainer dispatchEventContainer) {
        this.dispatchEventContainer = dispatchEventContainer;
        dbSyncConfService = SpringManager.getInstance().getBeanByType(DBSyncConfService.class);
    }

    public void handle(EventSourceBean e) {

        LOG.info("处理事件："+ e.toShortString());

        if(e.getHandleAdvise() != null){
            try{
                e.getHandleAdvise().beforeHandle(e);
            }catch (Exception ex){
                LOG.warn("事件前置处理异常！taskInstId="+e.getTaskInstId(), ex);
            }
        }

        try {
            switch (e.getStype()) {
                case LOADED:
                    this.load(e);
                    break;
                case STOP:
                    this.stop(e);
                    break;
                case START:
                    this.start(e);
                    break;
                case PERESIST:
                    this.peresist(e);
                    break;
                default:
                    LOG.warn("无效事件对象类型! stype=" + e.getStype());
            }
        }catch (Exception ex){
            LOG.error("事件处理异常！taskInstId="+e.getTaskInstId(), ex);
        }finally {
            if(e.getHandleAdvise() != null){
                try{
                    e.getHandleAdvise().finishHandle(e);
                }catch (Exception ex){
                    LOG.warn("事件后置处理异常！taskInstId="+e.getTaskInstId(), ex);
                }
            }
        }

    }

    public void stop(EventSourceBean e){
        AtomicBoolean status = statusMap.get(e.getTaskInstId());

        if(status != null){
            synchronized (statusMap) {
                statusMap.remove(status);
            }
            dispatchEventContainer.clearEventBeanByEventId(e.getEventId());
        }

    }

    public void start(EventSourceBean event){
        StartEventBean startEventBean = (StartEventBean)event;
        AtomicBoolean status = statusMap.get(event.getTaskInstId());

        if(status == null){
            synchronized (statusMap){
                status = statusMap.get(event.getTaskInstId());
                if(status == null){
                    statusMap.put(event.getTaskInstId(), new AtomicBoolean(true));
                }
            }
        }else {
            status.compareAndSet(false, true);
        }

        LoadDataEventBean loadDataEventBean = new LoadDataEventBean(event);
        loadDataEventBean.setConfTableBeans(startEventBean.getConfTableBeans());
        loadDataEventBean.setSync(false);

        dispatchEventContainer.publicEvent(loadDataEventBean);
    }

    /**
     * Get status boolean.
     *
     * @param e the e
     * @return the boolean
     */
    public boolean getStatus(EventSourceBean e){
        AtomicBoolean status = statusMap.get(e.getTaskInstId());
        return status == null? false:status.get();
    }

    public void pause(EventSourceBean e){
        LOG.warn("暂停方法未实现！");
    }

    public void resume(EventSourceBean e){
        LOG.warn("恢复方法未实现！");
    }

    public void load(EventSourceBean e){

    }

    public void peresist(EventSourceBean e){

    }

}
