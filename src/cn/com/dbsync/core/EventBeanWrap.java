package cn.com.dbsync.core;

import cn.com.dbsync.listener.EventSourceBean;

/**
 * Created by cxi on 2016/5/4.
 *
 * @param <T> the type parameter
 */
public class EventBeanWrap<T> implements Comparable<EventBeanWrap<T>> {


    private T esource = null;

    private EventSourceBean.EventType etype = null;

    /**
     * Instantiates a new Event bean wrap.
     */
    public EventBeanWrap(){
        this(null, EventSourceBean.EventType.RESERVER);
    }

    /**
     * Instantiates a new Event bean wrap.
     *
     * @param esource the esource
     */
    public EventBeanWrap(T esource){
        this(esource, EventSourceBean.EventType.RESERVER);
    }

    /**
     * Instantiates a new Event bean wrap.
     *
     * @param esource the esource
     * @param etype   the etype
     */
    public EventBeanWrap(T esource, EventSourceBean.EventType etype) {
        this.esource = esource;
        this.etype = etype;
    }

    /**
     * Gets esource.
     *
     * @return the esource
     */
    public T getEsource() {
        return esource;
    }

    /**
     * Sets esource.
     *
     * @param esource the esource
     */
    public void setEsource(T esource) {
        this.esource = esource;
    }

    /**
     * Gets etype.
     *
     * @return the etype
     */
    public EventSourceBean.EventType getEtype() {
        return etype;
    }

    /**
     * Sets etype.
     *
     * @param etype the etype
     */
    public void setEtype(EventSourceBean.EventType etype) {
        this.etype = etype;
    }

    @Override
    public int compareTo(EventBeanWrap<T> e) {

        if(e == null){
            return 0;
        }

        if(this.etype.ordinal() > e.etype.ordinal()){
            return 1;
        }else if(this.etype.ordinal() < e.etype.ordinal()){
            return -1;
        }

        return 0;
    }

    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }

        EventBeanWrap peerobj = (EventBeanWrap)obj;

        return this.esource != null && this.esource.equals(peerobj.esource)&&
        this.etype !=null && this.etype.equals(peerobj.etype);
    }

}
