package cn.com.dbsync.bean;

import cn.com.dbsync.util.CommUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cxi on 2016/5/8.
 */
public class TaskInstBean {

    /**状态(9:未处理 1:处理成功 2:停止 3:暂停 4:处理失败 98:处理成功但是有异常 99:处理中)*/
   public enum Status{
        UNHANDLE(9, "未处理"), SUCC(1, "处理成功"), STOP(2, "停止"),PAUSE(3, "暂停"),
        ERROR(4, "处理失败 "), SUCCPART(98,"处理成功但是有异常"),HANDLING(99, "处理中");

        public final int value;
        public  final String name;

        Status(int v, String n){
            this.value = v;
            this.name = n;
        }

    }

    private long taskInstId;
    private long taskId;
    private String taskName;
    private int taskType;
    private String taskParam;
    private Date generateTime;
    private Date planTime;
    private Date beginTime;
    private Date endTime;
    private long totalWaste;
    private String currentMsg;
    private long finishRate;
    private String errorMsg;
    private int status;
    private String serverFlag;

    public long getTaskInstId() {
        return taskInstId;
    }

    public void setTaskInstId(long taskInstId) {
        this.taskInstId = taskInstId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public String getTaskParam() {
        return taskParam;
    }

    public void setTaskParam(String taskParam) {
        this.taskParam = taskParam;
    }

    public Date getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(Date generateTime) {
        this.generateTime = generateTime;
    }

    public Date getPlanTime() {
        return planTime;
    }

    public void setPlanTime(Date planTime) {
        this.planTime = planTime;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getTotalWaste() {
        return totalWaste;
    }

    public void setTotalWaste(long totalWaste) {
        this.totalWaste = totalWaste;
    }

    public String getCurrentMsg() {
        return currentMsg;
    }

    public void setCurrentMsg(String currentMsg) {
        this.currentMsg = currentMsg;
    }

    public long getFinishRate() {
        return finishRate;
    }

    public void setFinishRate(long finishRate) {
        this.finishRate = finishRate;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getServerFlag() {
        return serverFlag;
    }

    public void setServerFlag(String serverFlag) {
        this.serverFlag = serverFlag;
    }

    public String toString(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("taskInstId", this.taskInstId);
        map.put("taskId", this.taskId);
        map.put("planTime", CommUtil.formatDateToNormalStr(planTime));
        map.put("beginTime", CommUtil.formatDateToNormalStr(beginTime));
        map.put("endTime", CommUtil.formatDateToNormalStr(endTime));
        map.put("generateTime", CommUtil.formatDateToNormalStr(generateTime));
        map.put("currentMsg", this.currentMsg);
        map.put("finishRate", this.finishRate);
        map.put("status", this.status);
        map.put("serverFlag", this.serverFlag);


      return map.toString();
    }
}
