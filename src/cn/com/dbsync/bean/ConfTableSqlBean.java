package cn.com.dbsync.bean;

/**
 * ÷–º‰Ã¨SQL”Ôæ‰
 *
 * Created by Administrator on 2017-10-30.
 */
public class ConfTableSqlBean {

    private long taskId ;
    private String sourceTable;
    private String targetTable;
    private String sourceDbName;
    private String targetDbName;

    private String srcSelectSql;
    private String srcSelectOrderBySql;
    private String tagSelectOrderBySql;
    private String srcShortSelectSql;
    private String tagDeleteByIdSql;
    private String tagDeleteSql;
    private String tagInsertSql;
    private String tagMinsertSql;
    private String tagUpdateSql;

    public ConfTableSqlBean(){

    }

    public ConfTableSqlBean(long taskId,String sourceTable,
                            String targetTable,String sourceDbName,String targetDbName){
        this.taskId = taskId;
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.sourceDbName = sourceDbName;
        this.targetDbName = targetDbName;
    }

    public String getTagSelectOrderBySql() {
        return tagSelectOrderBySql;
    }

    public void setTagSelectOrderBySql(String tagSelectOrderBySql) {
        this.tagSelectOrderBySql = tagSelectOrderBySql;
    }

    public String getTagDeleteSql() {
        return tagDeleteSql;
    }

    public void setTagDeleteSql(String tagDeleteSql) {
        this.tagDeleteSql = tagDeleteSql;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
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

    public String getSrcSelectSql() {
        return srcSelectSql;
    }

    public void setSrcSelectSql(String srcSelectSql) {
        this.srcSelectSql = srcSelectSql;
    }

    public String getSrcSelectOrderBySql() {
        return srcSelectOrderBySql;
    }

    public void setSrcSelectOrderBySql(String srcSelectOrderBySql) {
        this.srcSelectOrderBySql = srcSelectOrderBySql;
    }

    public String getSrcShortSelectSql() {
        return srcShortSelectSql;
    }

    public void setSrcShortSelectSql(String srcShortSelectSql) {
        this.srcShortSelectSql = srcShortSelectSql;
    }

    public String getTagDeleteByIdSql() {
        return tagDeleteByIdSql;
    }

    public void setTagDeleteByIdSql(String tagDeleteByIdSql) {
        this.tagDeleteByIdSql = tagDeleteByIdSql;
    }

    public String getTagInsertSql() {
        return tagInsertSql;
    }

    public void setTagInsertSql(String tagInsertSql) {
        this.tagInsertSql = tagInsertSql;
    }

    public String getTagMinsertSql() {
        return tagMinsertSql;
    }

    public void setTagMinsertSql(String tagMinsertSql) {
        this.tagMinsertSql = tagMinsertSql;
    }

    public String getTagUpdateSql() {
        return tagUpdateSql;
    }

    public void setTagUpdateSql(String tagUpdateSql) {
        this.tagUpdateSql = tagUpdateSql;
    }
}
