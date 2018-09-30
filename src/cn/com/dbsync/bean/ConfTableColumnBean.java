package cn.com.dbsync.bean;

/**
 * Created by Administrator on 2018/9/25/025.
 */
public class ConfTableColumnBean {

    private Integer taskId;
    private String sourceTable;
    private String targetTable;
    private String sourceDbName;
    private String targetDbName;
    private String sourceColumn;
    private String targetColumn;
    private String dependTable;
    private String relateColumn;
    private Integer incerToken;
    private Integer pkeyToken;
    private Integer targetColtype;
    private Integer columnOrder;

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getSourceDbName() {
        return sourceDbName;
    }

    public void setSourceDbName(String sourceDbName) {
        this.sourceDbName = sourceDbName;
    }

    public String getTargetDbName() {
        return targetDbName;
    }

    public void setTargetDbName(String targetDbName) {
        this.targetDbName = targetDbName;
    }

    public String getDependTable() {
        return dependTable;
    }

    public void setDependTable(String dependTable) {
        this.dependTable = dependTable;
    }

    public String getRelateColumn() {
        return relateColumn;
    }

    public void setRelateColumn(String relateColumn) {
        this.relateColumn = relateColumn;
    }

    public Integer getIncerToken() {
        return incerToken;
    }

    public void setIncerToken(Integer incerToken) {
        this.incerToken = incerToken;
    }

    public Integer getPkeyToken() {
        return pkeyToken;
    }

    public void setPkeyToken(Integer pkeyToken) {
        this.pkeyToken = pkeyToken;
    }

    public Integer getTargetColtype() {
        return targetColtype;
    }

    public void setTargetColtype(Integer targetColtype) {
        this.targetColtype = targetColtype;
    }

    public Integer getColumnOrder() {
        return columnOrder;
    }

    public void setColumnOrder(Integer columnOrder) {
        this.columnOrder = columnOrder;
    }
}
