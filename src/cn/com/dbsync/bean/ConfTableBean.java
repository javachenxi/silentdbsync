package cn.com.dbsync.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxi on 2016/5/6.
 */
public class ConfTableBean {

    private final static int INCER_TOKEN = 1;
    private final static int PKEY_TOKEN = 1;

    private long taskId ;

    private String sourceTable;
    private String targetTable;
    private String sourceDbName;
    private String targetDbName;

    private String dependTable;
    private String relateColumn;

    private List<ConfColumn> columnList;
    private List<ConfColumn> pkeycols;

    private List<ConfTableBean> childList;

    //…˙≥…SQL”Ôæ‰ Ù–‘
    private String selectSql ;
    private String shortSelectSql ;
    private String srcSelectByOrderSql;

    private String tagSelectByOrderSql;

    private String deleteSql ;
    private String deleteSqlById ;
    private String insertSql ;
    private String updateSql ;
    private String mergInsertSql ;

    private String incerColumn;


    /**
     * Instantiates a new Conf table bean.
     */
    public ConfTableBean(){
        pkeycols = new ArrayList<ConfColumn>();
        columnList = new ArrayList<ConfColumn>();
    }

    /**
     * Instantiates a new Conf table bean.
     *
     * @param taskId       the task id
     * @param sourceTable  the source table
     * @param sourceDbName the source db name
     * @param targetDbName the target db name
     * @param dependTable  the depend table
     * @param relateColumn the relate column
     */
    public ConfTableBean(long taskId,String sourceTable,String sourceDbName,String targetDbName,
                         String dependTable,String relateColumn){
        this.taskId = taskId;
        this.sourceTable = sourceTable;
        this.sourceDbName = sourceDbName;
        this.targetDbName = targetDbName;
        this.dependTable = dependTable;
        this.relateColumn = relateColumn;
        columnList = new ArrayList<ConfColumn>();
    }

    /**
     * Add conf column.
     *
     * @param sourceColumn
     * @param targetColumn
     * @param incerToken
     * @param pkeyToken
     * @param soucreColType
     * @param tagColType
     * @param columnOrder
     */
    public void addConfColumn(String sourceColumn, String targetColumn, int incerToken, int pkeyToken, String soucreColType,
                              int tagColType, int columnOrder){
        if(INCER_TOKEN == incerToken){
            incerColumn = sourceColumn;
        }

        if(PKEY_TOKEN == pkeyToken){
            pkeycols.add(new ConfColumn(sourceColumn,targetColumn,incerToken,pkeyToken,soucreColType,tagColType,columnOrder));
        }

        columnList.add(new ConfColumn(sourceColumn,targetColumn,incerToken,pkeyToken,soucreColType,tagColType,columnOrder));
    }

    /**
     * Add child table.
     *
     * @param confTableBean the conf table bean
     */
    public void addChildTable(ConfTableBean confTableBean){
        if(childList == null){
            childList = new ArrayList<ConfTableBean>();
        }

        childList.add(confTableBean);
    }

    public String getTagSelectByOrderSql() {
        return tagSelectByOrderSql;
    }

    public void setTagSelectByOrderSql(String tagSelectByOrderSql) {
        this.tagSelectByOrderSql = tagSelectByOrderSql;
    }

    public String getSrcSelectByOrderSql() {
        return srcSelectByOrderSql;
    }

    public void setSrcSelectByOrderSql(String srcSelectByOrderSql) {
        this.srcSelectByOrderSql = srcSelectByOrderSql;
    }

    public String getDeleteSqlById() {
        return deleteSqlById;
    }

    public void setDeleteSqlById(String deleteSqlById) {
        this.deleteSqlById = deleteSqlById;
    }

    public String getUpdateSql() {
        return updateSql;
    }

    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    public String getMergInsertSql() {
        return mergInsertSql;
    }

    public void setMergInsertSql(String mergInsertSql) {
        this.mergInsertSql = mergInsertSql;
    }

    /**
     * Gets short select sql.
     *
     * @return the short select sql
     */
    public String getShortSelectSql() {
        return shortSelectSql;
    }

    /**
     * Sets short select sql.
     *
     * @param shortSelectSql the short select sql
     */
    public void setShortSelectSql(String shortSelectSql) {
        this.shortSelectSql = shortSelectSql;
    }

    /**
     * Gets incer column.
     *
     * @return the incer column
     */
    public String getIncerColumn() {
        return incerColumn;
    }



    /**
     * Gets column list.
     *
     * @return the column list
     */
    public List<ConfColumn> getColumnList() {
        return columnList;
    }

    /**
     *
     * @return
     */
    public List<ConfColumn> getPkeycols(){
        return pkeycols;
    }

    /**
     * Gets child list.
     *
     * @return the child list
     */
    public List<ConfTableBean> getChildList() {
        return childList;
    }

    /**
     * Gets select sql.
     *
     * @return the select sql
     */
    public String getSelectSql() {
        return selectSql;
    }

    /**
     * Sets select sql.
     *
     * @param selectSql the select sql
     */
    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    /**
     * Gets delete sql.
     *
     * @return the delete sql
     */
    public String getDeleteSql() {
        return deleteSql;
    }

    /**
     * Sets delete sql.
     *
     * @param deleteSql the delete sql
     */
    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
    }

    /**
     * Gets insert sql.
     *
     * @return the insert sql
     */
    public String getInsertSql() {
        return insertSql;
    }

    /**
     * Sets insert sql.
     *
     * @param insertSql the insert sql
     */
    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    /**
     * Gets relate column.
     *
     * @return the relate column
     */
    public String getRelateColumn() {
        return relateColumn;
    }

    /**
     * Sets relate column.
     *
     * @param relateColumn the relate column
     */
    public void setRelateColumn(String relateColumn) {
        this.relateColumn = relateColumn;
    }

    /**
     * Gets depend table.
     *
     * @return the depend table
     */
    public String getDependTable() {
        return dependTable;
    }

    /**
     * Sets depend table.
     *
     * @param dependTable the depend table
     */
    public void setDependTable(String dependTable) {
        this.dependTable = dependTable;
    }

    /**
     * Gets target db name.
     *
     * @return the target db name
     */
    public String getTargetDbName() {
        return targetDbName;
    }

    /**
     * Sets target db name.
     *
     * @param targetDbName the target db name
     */
    public void setTargetDbName(String targetDbName) {
        this.targetDbName = targetDbName;
    }

    /**
     * Gets source db name.
     *
     * @return the source db name
     */
    public String getSourceDbName() {
        return sourceDbName;
    }

    /**
     * Sets source db name.
     *
     * @param sourceDbName the source db name
     */
    public void setSourceDbName(String sourceDbName) {
        this.sourceDbName = sourceDbName;
    }

    /**
     * Gets target table.
     *
     * @return the target table
     */
    public String getTargetTable() {
        return targetTable;
    }

    /**
     * Sets target table.
     *
     * @param targetTable the target table
     */
    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    /**
     * Gets source table.
     *
     * @return the source table
     */
    public String getSourceTable() {
        return sourceTable;
    }

    /**
     * Sets source table.
     *
     * @param sourceTable the source table
     */
    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
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
     * The type Conf column.
     */
    public class ConfColumn {
        private String sourceColumn;
        private String targetColumn;
        private String sourceColType;
        private int incerToken;
        private int pkeyToken;
        private int tagColType;
        private int columnOrder;

        /**
         * Instantiates a new Conf column.
         *
         * @param sourceColumn
         * @param targetColumn
         * @param incerToken
         * @param pkeyToken
         * @param sourceColType
         * @param tagColType
         * @param columnOrder
         */
        public ConfColumn(String sourceColumn,String targetColumn,int incerToken,int pkeyToken,
                          String sourceColType,int tagColType, int columnOrder){
            this.sourceColumn = sourceColumn;
            this.targetColumn = targetColumn;
            this.incerToken = incerToken;
            this.pkeyToken = pkeyToken;
            this.sourceColType = sourceColType;
            this.tagColType = tagColType;
            this.columnOrder = columnOrder;
        }

        public int getTagColType() {
            return tagColType;
        }

        public void setTagColType(int tagColType) {
            this.tagColType = tagColType;
        }

        public int getColumnOrder() {
            return columnOrder;
        }

        public void setColumnOrder(int columnOrder) {
            this.columnOrder = columnOrder;
        }

        /**
         * Gets source col type.
         *
         * @return the source col type
         */
        public String getSourceColType() {
            return sourceColType;
        }

        /**
         * Sets source col type.
         *
         * @param sourceColType the source col type
         */
        public void setSourceColType(String sourceColType) {
            this.sourceColType = sourceColType;
        }

        /**
         * Is incer token boolean.
         *
         * @return the boolean
         */
        public boolean isIncerToken(){
            return INCER_TOKEN == this.incerToken;
        }

        /**
         * Is pkey token boolean.
         *
         * @return the boolean
         */
        public boolean isPkeyToken(){
            return PKEY_TOKEN == this.pkeyToken;
        }

        /**
         * Gets source column.
         *
         * @return the source column
         */
        public String getSourceColumn() {
            return sourceColumn;
        }

        /**
         * Sets source column.
         *
         * @param sourceColumn the source column
         */
        public void setSourceColumn(String sourceColumn) {
            this.sourceColumn = sourceColumn;
        }

        /**
         * Gets target column.
         *
         * @return the target column
         */
        public String getTargetColumn() {
            return targetColumn;
        }

        /**
         * Sets target column.
         *
         * @param targetColumn the target column
         */
        public void setTargetColumn(String targetColumn) {
            this.targetColumn = targetColumn;
        }

        /**
         * Gets incer token.
         *
         * @return the incer token
         */
        public int getIncerToken() {
            return incerToken;
        }

        /**
         * Sets incer token.
         *
         * @param incerToken the incer token
         */
        public void setIncerToken(int incerToken) {
            this.incerToken = incerToken;
        }

        /**
         * Gets pkey token.
         *
         * @return the pkey token
         */
        public int getPkeyToken() {
            return pkeyToken;
        }

        /**
         * Sets pkey token.
         *
         * @param pkeyToken the pkey token
         */
        public void setPkeyToken(int pkeyToken) {
            this.pkeyToken = pkeyToken;
        }
    }
}
