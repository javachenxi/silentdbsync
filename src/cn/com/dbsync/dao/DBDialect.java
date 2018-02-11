package cn.com.dbsync.dao;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.listener.StatementMapHelp;
import cn.com.dbsync.util.DBSyncConstant;
import cn.com.dbsync.util.StringConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.List;

/**
 * <p>
 * Title: Dialect
 * </p>
 * <p>
 * Description: 关于数据库本地化处理
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009-2016
 * </p>
 * <p>
 * Company: servyou
 * </p>
 *
 * @author cxi
 * @version 1.0
 * @created 2016 /5/31
 */
public abstract class DBDialect {

    private static final Log log = LogFactory.getLog(DBDialect.class);

    /**
     * The constant ENCODE_UTF8.
     */
    protected static final String ENCODE_UTF8="UTF-8";
    /**
     * The constant ENCODE_GBK.
     */
    protected static final String ENCODE_GBK="GBK";
    /**
     * The constant ENCODE_ISO.
     */
    protected static final String ENCODE_ISO="ISO8859_1";

    /**
     * The App charset.
     */
    protected String appCharset;
    /**
     * The Db charset.
     */
    protected String dbCharset;

    /**
     * Instantiates a new Db dialect.
     */
    public DBDialect(){

    }

    /**
     * Instantiates a new Db dialect.
     *
     * @param appCharset the app charset
     * @param dbCharset  the db charset
     */
    public DBDialect(String appCharset, String dbCharset){
        this.appCharset = appCharset.toUpperCase();
        this.dbCharset = dbCharset.toUpperCase();
    }

    /**
     * Trans tasktype to sql type int.
     *
     * @param tasktype the tasktype
     * @return the int
     */
    public abstract int transTasktypeToSQLType(String tasktype);

    /**
     * Cast java to sql type object.
     *
     * @param sqltype the sqltype
     * @param valObj  the val obj
     * @return the object
     */
    public abstract Object castJavaToSqlType(int sqltype, Object valObj);

    /**
     * Cast result set to java type object.
     *
     * @param sqltype   the sqltype
     * @param resultSet the result set
     * @param index     the index
     * @return the object
     * @throws SQLException the sql exception
     */
    public abstract Object castResultSetToJavaType(int sqltype, ResultSet resultSet, int index)throws SQLException;

    /**
     * Gets db type.
     *
     * @return the db type
     */
    public abstract DBSyncConstant.DBType getDBType();

    /**
     * Trans type sql to ddl string.
     *
     * @param sqltype the sqltype
     * @return the string
     */
    public abstract String transTypeSqlToDDL(int sqltype);

    /**
     * Sets prepared statement not null param.
     *
     * @param prepareStatement the prepare statement
     * @param pIndex           the p index
     * @param vobj             the vobj
     * @param sqlType          the sql type
     * @throws SQLException the sql exception
     */
    public abstract void setPreparedStatementNotNullParam( PreparedStatement prepareStatement,
                                                           int pIndex, Object vobj,int sqlType) throws SQLException;

    /**
     * Sets prepared statement null param.
     *
     * @param prepareStatement the prepare statement
     * @param pIndex           the p index
     * @param sqlType          the sql type
     * @throws SQLException the sql exception
     */
    public void setPreparedStatementNullParam( PreparedStatement prepareStatement,
                                               int pIndex, int sqlType) throws SQLException{
        prepareStatement.setNull(pIndex, sqlType);
    }

    public PreparedStatement getInsertPreparedStatement(Connection sourConn, StatementMapHelp statementMapHelp, ConfTableBean confTableBean)
            throws SQLException{
        return getPreparedStatementBySQL(sourConn, statementMapHelp, confTableBean.getInsertSql());
    }

    public PreparedStatement getUpdatePreparedStatement(Connection sourConn, StatementMapHelp statementMapHelp, ConfTableBean confTableBean)
            throws SQLException{
        return getPreparedStatementBySQL(sourConn, statementMapHelp, confTableBean.getUpdateSql());
    }

    public PreparedStatement getDeleteByIdPreparedStatement(Connection sourConn, StatementMapHelp statementMapHelp, ConfTableBean confTableBean)
            throws SQLException{
        return getPreparedStatementBySQL(sourConn, statementMapHelp, confTableBean.getDeleteSqlById());
    }

    public  PreparedStatement getPreparedStatementBySQL(Connection sourConn, StatementMapHelp statementMapHelp, String sql)
            throws SQLException{
        PreparedStatement ps = statementMapHelp.get(sql);

        if(ps == null){
            ps = sourConn.prepareStatement(sql);
            statementMapHelp.put(sql, ps);

            if(log.isDebugEnabled()){
                log.debug("执行SQL:" + sql);
            }
        }

        return ps;
    }

    /**
     * 按照ResultSet的类型设置 PreparedStatement 的参数
     *
     * @param prepareStatement the prepare statement
     * @param rowList          the row list
     * @param columnMetaDatas  the column meta datas
     * @throws SQLException the sql exception
     */
    public void setPreparedStatementParams(PreparedStatement prepareStatement,
                                                  List rowList, ColumnMetaData[] columnMetaDatas) throws SQLException {
        for(int i=0; i<rowList.size(); i++){
            Object vobj = rowList.get(i);

            if(vobj != null){
                setPreparedStatementNotNullParam(prepareStatement, i+1, vobj, columnMetaDatas[i].getType());
            }else{
                setPreparedStatementNullParam(prepareStatement, i+1, columnMetaDatas[i].getType());
            }

        }
    }

    /**
     * 将数据库的结果集元数据转换成DAOResultMetaData
     *
     * @param rsmd the rsmd
     * @return dao result meta data
     */
    public DAOResultMetaData transResultMetaData(ResultSetMetaData rsmd) {
        try {
            DAOResultMetaData daoResultMetaData = new DAOResultMetaData();
            daoResultMetaData.setColumnCount(rsmd.getColumnCount());
            ColumnMetaData[] columnMetaDataArray = new ColumnMetaData[daoResultMetaData.getColumnCount()];
            for (int i = 0; i < columnMetaDataArray.length; i++) {
                ColumnMetaData columnMetaData = new ColumnMetaData();
                columnMetaData.setAutoIncrement(rsmd.isAutoIncrement(i + 1));
                columnMetaData.setCatalogName(rsmd.getCatalogName(i + 1));
                columnMetaData.setClassName(rsmd.getColumnClassName(i + 1));
                columnMetaData.setCurrency(rsmd.isCurrency(i + 1));
                columnMetaData.setDefinitelyWritable(rsmd.isDefinitelyWritable(i + 1));
                columnMetaData.setDisplaySize(rsmd.getColumnDisplaySize(i + 1));
                columnMetaData.setLabel(rsmd.getColumnLabel(i + 1));
                columnMetaData.setName(rsmd.getColumnName(i + 1));
                columnMetaData.setNullable(rsmd.isNullable(i + 1));
                columnMetaData.setReadOnly(rsmd.isReadOnly(i + 1));
                columnMetaData.setSchemaName(rsmd.getSchemaName(i + 1));
                columnMetaData.setSearchable(rsmd.isSearchable(i + 1));
                columnMetaData.setSigned(rsmd.isSigned(i + 1));
                columnMetaData.setTableName(rsmd.getTableName(i + 1));
                columnMetaData.setType(rsmd.getColumnType(i + 1));
                columnMetaData.setTypeName(rsmd.getColumnTypeName(i + 1));
                columnMetaData.setWritable(rsmd.isWritable(i + 1));
                columnMetaDataArray[i] = columnMetaData;
            }
            daoResultMetaData.setColumnMetaDataArray(columnMetaDataArray);
            return daoResultMetaData;
        } catch (Exception ex) {
            log.error("转换 ResultSetMetaData 异常！",ex);
            return null;
        }
    }


    /**
     * Convert str charset object.
     *
     * @param soucCharset the souc charset
     * @param tagCharset  the tag charset
     * @param obj         the obj
     * @return the object
     */
    public Object convertStrCharset(String soucCharset, String tagCharset,Object obj) {

        if (soucCharset == null || tagCharset == null || obj == null) {
            return obj;
        }

        if (soucCharset.equalsIgnoreCase(tagCharset)) {
            return obj;
        }

        return StringConverter.convertString((String) obj, soucCharset, tagCharset);
    }

}
