package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfTableBean;

import java.util.List;

/**
 * Created by cxi on 2016/5/8.
 */
public class LoadDataEventBean extends EventSourceBean {

    private List<ConfTableBean> confTableBeans ;

    /**
     * Instantiates a new Load data event bean.
     */
    public LoadDataEventBean() {
        super(EventType.LOADED);
    }

    public LoadDataEventBean(EventType eventType){
        super(eventType);
    }

    public LoadDataEventBean(EventSourceBean e, EventType eventType){
        super(e, eventType);
    }

    /**
     * Instantiates a new Load data event bean.
     *
     * @param e the e
     */
    public LoadDataEventBean(EventSourceBean e){
       super(e, EventType.LOADED);
    }

    /**
     * Gets conf table beans.
     *
     * @return the conf table beans
     */
    public List<ConfTableBean> getConfTableBeans() {
        return confTableBeans;
    }

    /**
     * Sets conf table beans.
     *
     * @param confTableBeans the conf table beans
     */
    public void setConfTableBeans(List<ConfTableBean> confTableBeans) {
        this.confTableBeans = confTableBeans;
    }
}
