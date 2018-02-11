package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.core.IEvent;
import cn.com.dbsync.core.IEventHandleAdvise;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Created by cxi on 2016/5/4.
 */
public class EventSourceBean implements IEvent<String> {
    /**
     * The enum Event type.
     */
    public enum EventType{
        /**
         * Pause event type.
         */
        PAUSE,
        /**
         * Stop event type.
         */
        STOP,
        /**
         * Start event type.
         */
        START,
        /**
         * Loaded event type.
         */
        LOADED,
        /**
         * Peresist event type.
         */
        PERESIST,
        /**
         * Reserver event type.
         */
        RESERVER
    }

    /**
     * The enum Launch type.
     */
    public enum LaunchType{
        /**
         * Auto launch type.
         */
        AUTO, /**
         * Manul launch type.
         */
        MANUL
    }

    private LaunchType launchType = LaunchType.AUTO;
    private EventType stype = null;
    private boolean isSync = true;
    private long taskId ;
    private String taskInstId;
    private String errorMsg;
    private IEventHandleAdvise handleAdvise;
    private ConfTaskBean confTaskBean;

    /**
     * Instantiates a new Event source bean.
     *
     * @param stype the stype
     */
    public EventSourceBean(EventType stype){
        this.stype = stype;
    }

    /**
     * Instantiates a new Event source bean.
     *
     * @param confTaskBean the conf task bean
     * @param stype        the stype
     */
    public EventSourceBean(ConfTaskBean confTaskBean,EventType stype){
        this.confTaskBean = confTaskBean;
        this.stype = stype;
        this.taskId = confTaskBean.getTaskId();
    }

    /**
     * Instantiates a new Event source bean.
     *
     * @param event     the event
     * @param eventType the event type
     */
    public EventSourceBean(EventSourceBean event, EventType eventType){
        this.stype = eventType;
        this.confTaskBean = event.getConfTaskBean();
        this.taskId = event.getTaskId();
        this.taskInstId = event.getTaskInstId();
        this.launchType = event.getLaunchType();
        this.errorMsg = event.getErrorMsg();
        this.handleAdvise = event.getHandleAdvise();
    }

    /**
     * Gets handle advise.
     *
     * @return the handle advise
     */
    public IEventHandleAdvise getHandleAdvise() {
        return handleAdvise;
    }

    /**
     * Sets handle advise.
     *
     * @param handleAdvise the handle advise
     */
    public void setHandleAdvise(IEventHandleAdvise handleAdvise) {
        this.handleAdvise = handleAdvise;
    }

    /**
     * Gets error msg.
     *
     * @return the error msg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Sets error msg.
     *
     * @param errorMsg the error msg
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * Gets launch type.
     *
     * @return the launch type
     */
    public LaunchType getLaunchType() {
        return launchType;
    }

    /**
     * Sets launch type.
     *
     * @param launchType the launch type
     */
    public void setLaunchType(LaunchType launchType) {
        this.launchType = launchType;
    }

    /**
     * Gets conf task bean.
     *
     * @return the conf task bean
     */
    public ConfTaskBean getConfTaskBean() {
        return confTaskBean;
    }

    /**
     * Sets conf task bean.
     *
     * @param confTaskBean the conf task bean
     */
    public void setConfTaskBean(ConfTaskBean confTaskBean) {
        this.confTaskBean = confTaskBean;
    }

    /**
     * Gets task id.
     *
     * @return the task id
     */
    public long getTaskId() {
        return taskId;
    }

    /**
     * Sets task id.
     *
     * @param taskId the task id
     */
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    /**
     * Gets task inst id.
     *
     * @return the task inst id
     */
    public String getTaskInstId() {
        return taskInstId;
    }

    /**
     * Sets task inst id.
     *
     * @param taskInstId the task inst id
     */
    public void setTaskInstId(String taskInstId) {
        this.taskInstId = taskInstId;
    }


    /**
     * Set stype.
     *
     * @param stype the stype
     */
    public void setStype(EventType stype){
        this.stype = stype;
    }

    /**
     * Get stype event source bean . event type.
     *
     * @return the event source bean . event type
     */
    public EventSourceBean.EventType getStype(){
        return this.stype;
    }

    /**
     * Is sync boolean.
     *
     * @return the boolean
     */
    public boolean isSync() {
        return isSync;
    }

    /**
     * Sets sync.
     *
     * @param sync the sync
     */
    public void setSync(boolean sync) {
        isSync = sync;
    }

    @Override
    public String getEventId() {
        return this.taskInstId;
    }

    public String toString(){
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * To short string string.
     *
     * @return the string
     */
    public String toShortString(){
        StringBuilder sb = new StringBuilder(256);
        sb.append('{').append("launchType").append(":").append(this.launchType).append(',');
        sb.append("stype").append(":").append(this.stype).append(',');
        sb.append("isSync").append(":").append(this.isSync).append(',');
        sb.append("taskId").append(":").append(this.taskId).append(',');
        sb.append("taskInstId").append(":").append(this.taskInstId).append(',');
        sb.append("errorMsg").append(":").append(this.errorMsg).append('}');

        return sb.toString();
    }
}
