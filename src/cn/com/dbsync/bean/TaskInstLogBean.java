package cn.com.dbsync.bean;

import java.util.Date;

/**
 * Created by cxi on 2016/5/8.
 */
public class TaskInstLogBean {

    /**
     * The constant SYNC_STATUS_SUCC.
     */
    public static final int SYNC_STATUS_SUCC = 1;
    /**
     * The constant SYNC_STATUS_FAIL.
     */
    public static final int SYNC_STATUS_FAIL = 2;

    private String taskInstId;
    private long taskId;
    private int syncStatus;
    private String syncDataSize;
    private String syncInfo;
    private Date syncDate;
    private String syncLastValue;
    private long syncLogId;
    private long allSize;

    /**
     * Gets all size.
     *
     * @return the all size
     */
    public long getAllSize() {
        return allSize;
    }

    /**
     * Sets all size.
     *
     * @param allSize the all size
     */
    public void setAllSize(long allSize) {
        this.allSize = allSize;
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
     * Gets sync status.
     *
     * @return the sync status
     */
    public int getSyncStatus() {
        return syncStatus;
    }

    /**
     * Sets sync status.
     *
     * @param syncStatus the sync status
     */
    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    /**
     * Gets sync data size.
     *
     * @return the sync data size
     */
    public String getSyncDataSize() {
        return syncDataSize;
    }

    /**
     * Sets sync data size.
     *
     * @param syncDataSize the sync data size
     */
    public void setSyncDataSize(String syncDataSize) {
        this.syncDataSize = syncDataSize;
    }

    /**
     * Gets sync info.
     *
     * @return the sync info
     */
    public String getSyncInfo() {
        return syncInfo;
    }

    /**
     * Sets sync info.
     *
     * @param syncInfo the sync info
     */
    public void setSyncInfo(String syncInfo) {
        this.syncInfo = syncInfo;
    }

    /**
     * Gets sync date.
     *
     * @return the sync date
     */
    public Date getSyncDate() {
        return syncDate;
    }

    /**
     * Sets sync date.
     *
     * @param syncDate the sync date
     */
    public void setSyncDate(Date syncDate) {
        this.syncDate = syncDate;
    }

    /**
     * Gets sync last value.
     *
     * @return the sync last value
     */
    public String getSyncLastValue() {
        return syncLastValue;
    }

    /**
     * Sets sync last value.
     *
     * @param syncLastValue the sync last value
     */
    public void setSyncLastValue(String syncLastValue) {
        this.syncLastValue = syncLastValue;
    }

    /**
     * Gets sync log id.
     *
     * @return the sync log id
     */
    public long getSyncLogId() {
        return syncLogId;
    }

    /**
     * Sets sync log id.
     *
     * @param syncLogId the sync log id
     */
    public void setSyncLogId(long syncLogId) {
        this.syncLogId = syncLogId;
    }
}
