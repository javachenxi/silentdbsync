package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.core.DispatchEventContainer;
import cn.com.dbsync.util.DBSyncConstant;

/**
 * Created by Administrator on 2018-01-23.
 */
public class LoadDataEventListenerWrap extends EventListenerAdapter{

    private LoadDataEventListener loadDataEventListener;

    private LoadDataEventVeriListener loadDataEventVeriListener;

    /**
     * Instantiates a new Event listener adapter.
     *
     * @param dispatchEventContainer the dispatch event container
     */
    public LoadDataEventListenerWrap(DispatchEventContainer dispatchEventContainer) {
        super(dispatchEventContainer);
        this.loadDataEventListener = new LoadDataEventListener(dispatchEventContainer);
        this.loadDataEventVeriListener = new LoadDataEventVeriListener(dispatchEventContainer);
    }


    public void load(EventSourceBean e){
        ConfTaskBean confTaskBean = e.getConfTaskBean();

        if (confTaskBean.getTaskType() == ConfTaskBean.TaskType.ALL.value
                ||confTaskBean.getTaskType() == ConfTaskBean.TaskType.INC.value ){
             this.loadDataEventListener.load(e);
        }else if(confTaskBean.getTaskType() == ConfTaskBean.TaskType.VERI.value){
             this.loadDataEventVeriListener.load(e);
        }else if(confTaskBean.getTaskType() == ConfTaskBean.TaskType.INCVERI.value){
            this.loadDataEventVeriListener.load(e);
            this.loadDataEventListener.load(e);
        }
    }


}
