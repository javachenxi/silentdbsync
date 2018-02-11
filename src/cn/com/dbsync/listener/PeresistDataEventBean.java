package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.PackResultBean;

import java.util.List;

/**
 * Created by cxi on 2016/5/8.
 */
public class PeresistDataEventBean extends EventSourceBean {

    private boolean isLast;
    private int seqNumber;
    private PackResultBean packResultBean;
    private List<ConfTableBean> confTableBeans ;

    /**
     * Instantiates a new Peresist data event bean.
     */
    public PeresistDataEventBean() {
        super(EventType.PERESIST);
    }

    /**
     * Instantiates a new Peresist data event bean.
     *
     * @param e the e
     */
    public PeresistDataEventBean(EventSourceBean e){
        super(e, EventType.PERESIST);
    }

    public PeresistDataEventBean(EventSourceBean e, EventType eventType){
        super(e, eventType);
    }

    public PeresistDataEventBean(EventType eventType){
        super(eventType);
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

    /**
     * Gets pack result bean.
     *
     * @return the pack result bean
     */
    public PackResultBean getPackResultBean() {
        return packResultBean;
    }

    /**
     * Sets pack result bean.
     *
     * @param packResultBean the pack result bean
     */
    public void setPackResultBean(PackResultBean packResultBean) {
        this.packResultBean = packResultBean;
    }

    /**
     * Is first boolean.
     *
     * @return the boolean
     */
    public boolean isFirst(){
        return seqNumber == 1;
    }

    /**
     * Is last boolean.
     *
     * @return the boolean
     */
    public boolean isLast() {
        return isLast;
    }

    /**
     * Sets last.
     *
     * @param last the last
     */
    public void setLast(boolean last) {
        isLast = last;
    }

    /**
     * Gets seq number.
     *
     * @return the seq number
     */
    public int getSeqNumber() {
        return seqNumber;
    }

    /**
     * Sets seq number.
     *
     * @param seqNumber the seq number
     */
    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }
}
