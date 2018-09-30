package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.core.DispatchEventContainer;

/**
 * Created by Administrator on 2018-01-23.
 */
public class PeresistDataEventListenerWrap extends EventListenerAdapter{

    private PeresistDataIncrEventListener peresistDataIncrEventListener;

    private PeresistDataVeriEventListener peresistDataVeriEventListener;

    /**
     * Instantiates a new Event listener adapter.
     *
     * @param dispatchEventContainer the dispatch event container
     */
    public PeresistDataEventListenerWrap(DispatchEventContainer dispatchEventContainer) {
        super(dispatchEventContainer);
        this.peresistDataIncrEventListener = new PeresistDataIncrEventListener(dispatchEventContainer);
        this.peresistDataVeriEventListener = new PeresistDataVeriEventListener(dispatchEventContainer);
    }


    public void peresist(EventSourceBean e){
        ConfTaskBean confTaskBean = e.getConfTaskBean();

        if (confTaskBean.getTaskType() == ConfTaskBean.TaskType.ALL.value
                ||confTaskBean.getTaskType() == ConfTaskBean.TaskType.INC.value ){
            this.peresistDataIncrEventListener.peresist(e);
        }else if(confTaskBean.getTaskType() == ConfTaskBean.TaskType.VERI.value){
            this.peresistDataVeriEventListener.peresist(e);
        }
    }

}
