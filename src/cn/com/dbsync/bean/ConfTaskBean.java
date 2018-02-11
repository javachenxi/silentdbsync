package cn.com.dbsync.bean;



import cn.com.dbsync.util.JsonUtil;

import java.math.BigInteger;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * Created by cxi on 2016/5/7.
 */
public class ConfTaskBean {
    //业务类型
   public enum TaskType{

        ALL(1, "全量同步"), INC(2, "增量同步"), VERI(4, "校验同步"), INCVERI(6, "增量校验同步"),
        GENERATEDBSYNC(99 , "生成数据库同步实例");

        public final int value;
        public final String name;

        TaskType(int v, String n){
            this.value = v;
            this.name = n;
        }
    }

    /**
     * The constant SYNC_CYCLE_YEAR.
     */
    //同步业务的同步周期,大于10为秒单位的周期
    public static final int SYNC_CYCLE_YEAR = 1;
    /**
     * The constant SYNC_CYCLE_MONTH.
     */
    public static final int SYNC_CYCLE_MONTH = 2;
    /**
     * The constant SYNC_CYCLE_WEEK.
     */
    public static final int SYNC_CYCLE_WEEK = 3;
    /**
     * The constant SYNC_CYCLE_DAY.
     */
    public static final int SYNC_CYCLE_DAY = 4;
    /**
     * The constant SYNC_CYCLE_OTHER.
     */
    public static final int SYNC_CYCLE_OTHER = 10;

    private long taskId;
    private String taskName;
    private int taskType;
    private int syncCycle;
    private Date taskCreated;
    private Date syncLasttime;
    private long instCount;
    private String yxbz;
    private List<SyncLastValue> lastValueList;
    private String lastValue;
    private int rollLastValue;

    private long syncInstLogId = 0;

    /**
     * Gets sync inst log id.
     *
     * @return the sync inst log id
     */
    public long getSyncInstLogId() {
        return syncInstLogId;
    }

    /**
     * Sets sync inst log id.
     *
     * @param syncInstLogId the sync inst log id
     */
    public void setSyncInstLogId(long syncInstLogId) {
        this.syncInstLogId = syncInstLogId;
    }

    /**
     * Gets last value list.
     *
     * @return the last value list
     */
    public List<SyncLastValue> getLastValueList() {
        return lastValueList;
    }

    /**
     * Get sync last value sync last value.
     *
     * @param tableName the table name
     * @param colName   the col name
     * @return the sync last value
     */
    public SyncLastValue getSyncLastValue(String tableName, String colName){

        if(lastValueList == null){
            return null;
        }

        SyncLastValue temp = null;
        for(int i=0; i<lastValueList.size(); i++){
            temp = lastValueList.get(i);

            if(tableName.equals(temp.getTableName())&&colName.equals(temp.getColName())){

                if(rollLastValue == 0 || temp.getLastVaule() == null){
                    return temp;
                }

                return calcRollLastValue(temp);
            }
        }

        return null;
    }

    /**
     *增量同步时，回退的值才有意义
     * ColType 不同类型不同处理：
     * 1.时间类型，lastVaule 减去 rollLastValue 分钟后的时间
     * 2.数值, lastVaule 减去 rollLastValue 后的起始值,小于零时取0
     * 3.字符串, lastVaule尾部截取rollLastValue个字符，长度不够去1
     *
     * @param temp
     * @return 返回拷贝 SyncLastValue 对象
     */
    private SyncLastValue calcRollLastValue(SyncLastValue temp){
        SyncLastValue relastvalue = temp.clone();

        Object tobj = temp.getLastVaule();

        switch (temp.getColType()){
            case Types.DATE:
            case Types.TIMESTAMP:
                long rollvalue = rollLastValue*60*1000;
                if(tobj instanceof BigInteger){
                    relastvalue.setLastVaule(((BigInteger)tobj).longValue() - rollvalue);
                }else if(tobj instanceof Long){
                    relastvalue.setLastVaule(((Long)tobj).longValue() - rollvalue);
                }
                break;
            case Types.NUMERIC:
                if(tobj instanceof BigInteger){
                    relastvalue.setLastVaule(((BigInteger)tobj).longValue() - rollLastValue);
                }else if(tobj instanceof Long){
                    relastvalue.setLastVaule(((Long)tobj).longValue() - rollLastValue);
                }else if(tobj instanceof Integer){
                    relastvalue.setLastVaule(((Integer)tobj).intValue() - rollLastValue);
                }
                break;
            default:
                if(tobj instanceof String){
                    String str = (String)tobj;
                    str = str.length()>rollLastValue?str.substring(0, str.length()-rollLastValue):str.substring(0,1);
                    relastvalue.setLastVaule(str);
                }

        }

        return relastvalue;
    }

    /**
     * Sets last value list.
     *
     * @param lastValueList the last value list
     */
    public void setLastValueList(List<SyncLastValue> lastValueList) {
        this.lastValueList = lastValueList;
    }

    /**
     * Gets last value.
     *
     * @return the last value
     */
    public String getLastValue() {
        if(lastValueList != null){
            this.lastValue = JsonUtil.getJsonStringForList(lastValueList);
        }
        return lastValue;
    }

    /**
     * Sets last value.
     *
     * @param lastValue the last value
     */
    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;

        if(lastValue != null){
            this.lastValueList = JsonUtil.getListForJson(lastValue, SyncLastValue.class);
        }
    }

    /**
     * Gets task created.
     *
     * @return the task created
     */
    public Date getTaskCreated() {
        return taskCreated;
    }

    /**
     * Sets task created.
     *
     * @param taskCreated the task created
     */
    public void setTaskCreated(Date taskCreated) {
        this.taskCreated = taskCreated;
    }

    /**
     * Gets inst count.
     *
     * @return the inst count
     */
    public long getInstCount() {
        return instCount;
    }

    /**
     * Sets inst count.
     *
     * @param instCount the inst count
     */
    public void setInstCount(long instCount) {
        this.instCount = instCount;
    }

    /**
     * Is inc task boolean.
     *
     * @return the boolean
     */
    public boolean isIncTask(){
        return TaskType.INC.value == this.taskType
                || TaskType.INCVERI.value == this.taskType
                || TaskType.VERI.value == this.taskType;
    }

    /**
     * Gets roll last value.
     *
     * @return the roll last value
     */
    public int getRollLastValue() {
        return rollLastValue;
    }

    /**
     * Sets roll last value.
     *
     * @param rollLastValue the roll last value
     */
    public void setRollLastValue(int rollLastValue) {
        this.rollLastValue = rollLastValue;
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
     * Gets task type.
     *
     * @return the task type
     */
    public int getTaskType() {
        return taskType;
    }

    /**
     * Sets task type.
     *
     * @param taskType the task type
     */
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    /**
     * Gets task name.
     *
     * @return the task name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Sets task name.
     *
     * @param taskName the task name
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Gets sync cycle.
     *
     * @return the sync cycle
     */
    public int getSyncCycle() {
        return syncCycle;
    }

    /**
     * Sets sync cycle.
     *
     * @param syncCycle the sync cycle
     */
    public void setSyncCycle(int syncCycle) {
        this.syncCycle = syncCycle;
    }

    /**
     * Gets sync lasttime.
     *
     * @return the sync lasttime
     */
    public Date getSyncLasttime() {
        return syncLasttime;
    }

    /**
     * Sets sync lasttime.
     *
     * @param syncLasttime the sync lasttime
     */
    public void setSyncLasttime(Date syncLasttime) {
        this.syncLasttime = syncLasttime;
    }

    /**
     * Gets yxbz.
     *
     * @return the yxbz
     */
    public String getYxbz() {
        return yxbz;
    }

    /**
     * Sets yxbz.
     *
     * @param yxbz the yxbz
     */
    public void setYxbz(String yxbz) {
        this.yxbz = yxbz;
    }

    /**
     * The type Sync last value.
     */
    public static class SyncLastValue{
        private String tableName;
        private String colName;
        private int colType;
        private Object lastVaule;

        public SyncLastValue clone(){
            SyncLastValue clone = new SyncLastValue();
            clone.tableName = this.colName;
            clone.colName = this.colName;
            clone.colType = this.colType;
            clone.lastVaule = this.lastVaule;
            return clone;
        }

        /**
         * Gets table name.
         *
         * @return the table name
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * Sets table name.
         *
         * @param tableName the table name
         */
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        /**
         * Gets col name.
         *
         * @return the col name
         */
        public String getColName() {
            return colName;
        }

        /**
         * Sets col name.
         *
         * @param colName the col name
         */
        public void setColName(String colName) {
            this.colName = colName;
        }

        /**
         * Gets col type.
         *
         * @return the col type
         */
        public int getColType() {
            return colType;
        }

        /**
         * Sets col type.
         *
         * @param colType the col type
         */
        public void setColType(int colType) {
            this.colType = colType;
        }

        /**
         * Gets last vaule.
         *
         * @return the last vaule
         */
        public Object getLastVaule() {
            return lastVaule;
        }

        /**
         * Sets last vaule.
         *
         * @param lastVaule the last vaule
         */
        public void setLastVaule(Object lastVaule) {
            this.lastVaule = lastVaule;
        }
    }
}
