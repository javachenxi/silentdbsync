package cn.com.dbsync.service;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.bean.TaskInstBean;
import cn.com.dbsync.dao.DBMybatisSimpleTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-10-31.
 */
@Service
public class DBSyncInstService extends DBMybatisSimpleTemplate {

    private static final Log log = LogFactory.getLog(DBSyncInstService.class.getName());

    private static final String DBSYNC_TASKINST_INSERT = "DBSyncInstMapper.insertTaskInstBean";
    private static final String DBSYNC_TASKINST_QUERYBYSTATUS = "DBSyncInstMapper.queryTaskInstListByStatus";
    private static final String DBSYNC_TASKINST_UPDATETOSTATUS = "DBSyncInstMapper.updateTaskInstToStatusById";
    private static final String DBSYNC_TASKINST_UPDATEDYNABYID = "DBSyncInstMapper.updateDynaTaskInstById";
    private static final String DBSYNC_TASKINST_QUERYDYNA = "DBSyncInstMapper.queryDynaTaskInstList";
    private static final String DBSYNC_TASKINST_ISHAVEINST = "DBSyncInstMapper.isHaveTaskInst";



    private static final int RESULT_FAIL = 0;


    public boolean updateDynaTaskInstById(TaskInstBean taskInstBean){
        int result = 0;

        try {
            result = this.update(DBSYNC_TASKINST_UPDATEDYNABYID, taskInstBean);
        }catch (Exception e) {
            log.error("跟新任务实例失败！TaskInstId=" + taskInstBean.getTaskInstId(), e);
        }

        return !(RESULT_FAIL == result);
    }

    public boolean updateTaskInstToStatusById(long taskInst, int orgStatus, int toStatus){
        int result = 0;
        try {
            Map<String, Object> paraMap = new HashMap<String, Object>();
            paraMap.put("taskInst", taskInst);
            paraMap.put("orgStatus", orgStatus);
            paraMap.put("toStatus", toStatus);
            result = this.insert(DBSYNC_TASKINST_UPDATETOSTATUS, paraMap);
        }catch (Exception e) {
            log.error("更新任务实例失败！taskInst=" + taskInst, e);
        }

        return !(RESULT_FAIL == result);
    }


    /**
     * 新增任务实例
     *
     * @param taskInstBean
     * @return
     */
    public boolean insertIaskInstBean(TaskInstBean taskInstBean) {
        int result = 0;

        try {
            result = this.insert(DBSYNC_TASKINST_INSERT, taskInstBean);
        }catch (Exception e) {
            log.error("新增任务实例失败！TaskID=" + taskInstBean.getTaskId(), e);
        }

        return !(RESULT_FAIL == result);
    }

    /**
     *
     * @param status
     * @return
     */
    public List<TaskInstBean> queryTaskInstListByStatus(int status){

        List<TaskInstBean> taskInstBeans = this.select(DBSYNC_TASKINST_QUERYBYSTATUS, status);

        return  taskInstBeans;
    }

    /**
     *
     * @param taskInstBean
     * @return
     */
    public List<TaskInstBean> queryTaskInstListDyna(TaskInstBean taskInstBean){
        List<TaskInstBean> taskInstBeans = this.select(DBSYNC_TASKINST_QUERYDYNA, taskInstBean);
        return  taskInstBeans;
    }


    public boolean isHaveTaskInst(long taskId, int[] status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", taskId);
        params.put("statusArray", status);
        int num = (Integer) this.selectOnce(DBSYNC_TASKINST_ISHAVEINST, params);
        return num > 0;
    }

    /**
     *
     * @param taskInstBean
     * @return
     */
    public List<TaskInstBean> queryTaskInstDyna(TaskInstBean taskInstBean){

        List<TaskInstBean> taskInstBeans = this.select(DBSYNC_TASKINST_QUERYDYNA, taskInstBean);

        return taskInstBeans;
    }

    /**
     *
     * @return
     */
    public TaskInstBean insertGenDBSyncTaskInst(){

        TaskInstBean taskInstBean = new TaskInstBean();
        taskInstBean.setTaskType(ConfTaskBean.TaskType.GENERATEDBSYNC.value);

        List<TaskInstBean>  tmpTaskInstList = this.queryTaskInstDyna(taskInstBean);

        if(tmpTaskInstList == null || tmpTaskInstList.isEmpty()){
            taskInstBean.setStatus(TaskInstBean.Status.UNHANDLE.value);
            taskInstBean.setPlanTime(this.getDBCurrDate().getTime());
            taskInstBean.setTaskName("生成DBSycn同步任务");

            if(!insertIaskInstBean(taskInstBean)){
                log.warn("生成DBSycn同步任务实例保存失败");
            }
        }

        return  tmpTaskInstList == null||tmpTaskInstList.isEmpty()?taskInstBean:tmpTaskInstList.get(0);

    }

}
