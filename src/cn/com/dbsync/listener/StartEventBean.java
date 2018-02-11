package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;

import java.util.List;

/**
 * Created by cxi on 2016/5/12.
 */
public class StartEventBean extends EventSourceBean{

    private List<ConfTableBean> confTableBeans ;

    /**
     * Instantiates a new Start event bean.
     */
    public StartEventBean() {
        super(EventType.START);
    }

    /**
     * Instantiates a new Start event bean.
     *
     * @param confTaskBean the conf task bean
     */
    public StartEventBean(ConfTaskBean confTaskBean){
        super(confTaskBean, EventType.START);
    }

    /**
     * Instantiates a new Start event bean.
     *
     * @param e the e
     */
    public StartEventBean(EventSourceBean e){
        super(e, EventType.START);
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
