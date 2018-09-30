package cn.com.dbsync.dao;

import cn.com.dbsync.util.CommUtil;
import cn.com.dbsync.util.DBSyncConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/9/25/025.
 */
public class MysqlDBDialect extends DBDialect{

    private static final Log log = LogFactory.getLog(MysqlDBDialect.class);
    private static final Map<String, Integer> TypeToSqlTypeMap = new HashMap<String, Integer>();
    /**
     * The constant SqlTypeToDDLTypeMap.
     */
    protected static final Map<Integer, String> SqlTypeToDDLTypeMap = new HashMap<Integer, String>();

    static {
        SqlTypeToDDLTypeMap.put(Types.VARCHAR, "VARCHAR(255)");
        SqlTypeToDDLTypeMap.put(Types.DOUBLE, "DOUBLE");
        SqlTypeToDDLTypeMap.put(Types.FLOAT, "FLOAT");
        SqlTypeToDDLTypeMap.put(Types.INTEGER, "INTEGER");
        SqlTypeToDDLTypeMap.put(Types.TIMESTAMP, "DATETIME");
        SqlTypeToDDLTypeMap.put(Types.TINYINT, "TINYINT");
        SqlTypeToDDLTypeMap.put(Types.BLOB, "BLOB");
        SqlTypeToDDLTypeMap.put(Types.CHAR, "CHAR(1)");

        TypeToSqlTypeMap.put("String", Types.VARCHAR);
        TypeToSqlTypeMap.put("Long", Types.BIGINT);
        TypeToSqlTypeMap.put("long", Types.BIGINT);
        TypeToSqlTypeMap.put("Integer", Types.INTEGER);
        TypeToSqlTypeMap.put("int", Types.INTEGER);
        TypeToSqlTypeMap.put("Byte", Types.INTEGER);
        TypeToSqlTypeMap.put("byte", Types.INTEGER);
        TypeToSqlTypeMap.put("Float", Types.FLOAT);
        TypeToSqlTypeMap.put("float", Types.FLOAT);
        TypeToSqlTypeMap.put("Double", Types.DOUBLE);
        TypeToSqlTypeMap.put("double", Types.DOUBLE);
        TypeToSqlTypeMap.put("Date", Types.TIMESTAMP);
        TypeToSqlTypeMap.put("Blob", Types.BLOB);
    }

    @Override
    public int transTasktypeToSQLType(String tasktype) {
        return TypeToSqlTypeMap.get(tasktype);
    }

    @Override
    public Object castJavaToSqlType(int sqltype, Object valObj) {
        Object retobj = null;

        if (valObj == null) {
            return retobj;
        }

        switch (sqltype) {
            case Types.BOOLEAN:
                if (valObj instanceof Boolean) {
                    retobj = ((Boolean) valObj).booleanValue() ? 1 : 0;
                }else{
                    retobj = valObj;
                }
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
                if(valObj instanceof Timestamp){
                    retobj = valObj;
                    break;
                }
                java.util.Date ret = null;
                if (valObj instanceof String) {
                    ret = CommUtil.parseStrToDate((String) valObj);
                } else if (valObj instanceof Long) {
                    ret = new java.util.Date((Long) valObj);
                } else {
                    ret = (java.util.Date) valObj;
                }
                retobj = new Timestamp(ret.getTime());

                break;

            case Types.VARCHAR:
            case Types.CHAR:
                if (valObj instanceof String) {
                    retobj = valObj;
                } else {
                    retobj = String.valueOf(valObj);
                }
                break;
            case Types.BIGINT:
                if(valObj instanceof Long){
                    retobj = valObj;
                    break;
                }

                if(valObj instanceof BigDecimal){
                    retobj = ((BigDecimal)valObj).longValue();
                }else if(valObj instanceof Integer){
                    retobj = ((Integer)valObj).longValue();
                }

                break;
            case Types.DOUBLE:
                if(valObj instanceof Double){
                    retobj = valObj;
                    break;
                }

                if (valObj instanceof String) {
                    retobj = Double.parseDouble((String) valObj);
                } else if(valObj instanceof Integer){
                    retobj = ((Integer)valObj).doubleValue();
                } else if(valObj instanceof Long){
                    retobj = ((Long)valObj).doubleValue();
                }else if(valObj instanceof Float){
                    retobj = ((Float)valObj).doubleValue();
                }
                break;
            case Types.FLOAT:
                if (valObj instanceof String) {
                    retobj = Float.parseFloat((String) valObj);
                } else {
                    retobj = valObj;
                }
                break;
            case Types.TINYINT:
            case Types.INTEGER:
                if (valObj instanceof String) {
                    retobj = Integer.parseInt((String) valObj);
                }else if (valObj instanceof Byte) {
                    retobj = ((Byte)valObj).intValue();
                }else{
                    retobj = valObj;
                }
                break;
            case Types.BLOB:
                if (valObj instanceof String) {
                    retobj = ((String) retobj).getBytes();
                }else {
                    retobj = valObj;
                }
                break;
            default:
                retobj = valObj;
        }

        return retobj;
    }

    @Override
    public Object castResultSetToJavaType(int sqltype, ResultSet resultSet, int index) throws SQLException {

            Object retobj = null;

            switch (sqltype) {
                case Types.LONGVARCHAR:
                case Types.VARCHAR:
                case Types.CHAR:
                    retobj = resultSet.getString(index);
                    break;
                case Types.DOUBLE:
                    retobj = resultSet.getDouble(index);
                    break;
                case Types.FLOAT:
                    retobj = resultSet.getFloat(index);
                    break;
                case Types.INTEGER:
                case Types.TINYINT:
                    retobj = resultSet.getInt(index);
                    break;
                case Types.BIGINT:
                    retobj = resultSet.getLong(index);
                    break;
                case Types.BLOB:
                    retobj = resultSet.getBytes(index);
                    break;
                case Types.DATE:
                    retobj = resultSet.getDate(index);
                    break;
                case Types.TIMESTAMP:
                    retobj = resultSet.getTimestamp(index);
                    break;
                default:
                    retobj = resultSet.getString(index);
            }

            return retobj;

    }

    @Override
    public DBSyncConstant.DBType getDBType() {
        return DBSyncConstant.DBType.MYSQL;
    }

    @Override
    public String transTypeSqlToDDL(int sqltype) {
        return SqlTypeToDDLTypeMap.get(sqltype);
    }

    @Override
    public void setPreparedStatementNotNullParam(PreparedStatement prepareStatement, int pIndex, Object vobj, int sqlType) throws SQLException {
        Object paramVal = this.castJavaToSqlType(sqlType, vobj);

        switch (sqlType) {
            case Types.LONGVARCHAR:
            case Types.CHAR:
            case Types.VARCHAR:
                prepareStatement.setString(pIndex, (String) paramVal);
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
                prepareStatement.setTimestamp(pIndex, (Timestamp) paramVal);
                break;
            case Types.DOUBLE:
                if (Double.isNaN((Double) paramVal) || Double.isInfinite((Double) paramVal)) {
                    prepareStatement.setNull(pIndex, Types.DOUBLE);
                } else {
                    prepareStatement.setDouble(pIndex, (Double) paramVal);
                }
                break;
            case Types.BOOLEAN:
            case Types.TINYINT:
            case Types.INTEGER:
                if(paramVal instanceof Long){
                    prepareStatement.setLong(pIndex, (Long) paramVal);
                }else{
                    prepareStatement.setInt(pIndex, (Integer) paramVal);
                }
                break;
            case Types.FLOAT:
                if (Float.isNaN((Float) paramVal) || Float.isInfinite((Float) paramVal)) {
                    prepareStatement.setNull(pIndex, Types.FLOAT);
                } else {
                    prepareStatement.setFloat(pIndex, (Float) paramVal);
                }
                break;
            case Types.BIGINT:
                if (paramVal == null) {
                    prepareStatement.setNull(pIndex, Types.BIGINT);
                } else {
                    prepareStatement.setLong(pIndex, (Long)paramVal);
                }
                break;
            case Types.BLOB:
                prepareStatement.setBytes(pIndex, (byte[])paramVal);
                break;
            default:
                prepareStatement.setObject(pIndex, paramVal);
        }
    }
}
