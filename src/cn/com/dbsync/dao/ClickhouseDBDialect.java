package cn.com.dbsync.dao;

import cn.com.dbsync.util.CommUtil;
import cn.com.dbsync.util.DBSyncConstant;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Created by Administrator on 2018/9/27/027.
 */
public class ClickhouseDBDialect extends DBDialect{

    @Override
    public int transTasktypeToSQLType(String tasktype) {
        return 0;
    }

    @Override
    public Object castJavaToSqlType(int sqltype, Object valObj) {
        Object retobj = null;

        if (valObj == null) {
            return retobj;
        }

        switch (sqltype) {
            case Types.DATE:
                if(valObj instanceof Date){
                    retobj = valObj;
                    break;
                }

                java.util.Date ret1 = null;
                if (valObj instanceof String) {
                    ret1 = CommUtil.parseStrToDate((String) valObj);
                } else if (valObj instanceof Long) {
                    ret1 = new java.util.Date((Long) valObj);
                }
                retobj = valObj;
                break;
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
                if (valObj instanceof String) {
                    retobj = valObj;
                } else {
                    retobj = String.valueOf(valObj);
                }
                break;
            case Types.BIGINT:
                if(valObj instanceof BigDecimal){
                    retobj = valObj;
                    break;
                }

                retobj = new BigDecimal(valObj.toString());

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
            case Types.INTEGER:
                if (valObj instanceof String) {
                    retobj = Integer.parseInt((String) valObj);
                }else if (valObj instanceof Byte) {
                    retobj = ((Byte)valObj).intValue();
                }else {
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
            case Types.VARCHAR:
                retobj = resultSet.getString(index);
                break;
            case Types.DOUBLE:
                retobj = resultSet.getDouble(index);
                break;
            case Types.FLOAT:
                retobj = resultSet.getFloat(index);
                break;
            case Types.INTEGER:

                Long tmpv = resultSet.getLong(index);

                if(tmpv != null &&tmpv < Integer.MAX_VALUE && tmpv > Integer.MIN_VALUE){
                    retobj = tmpv.intValue();
                }else{
                    retobj = tmpv;
                }

                break;
            case Types.BIGINT:
                retobj = resultSet.getBigDecimal(index);
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
            case Types.ARRAY:
                retobj = resultSet.getArray(index);
                break;
            default:
                retobj = resultSet.getString(index);
        }

        return retobj;
    }

    @Override
    public DBSyncConstant.DBType getDBType() {
        return DBSyncConstant.DBType.CLICKHOUSE;
    }

    @Override
    public String transTypeSqlToDDL(int sqltype) {
        return null;
    }

    @Override
    public void setPreparedStatementNotNullParam(PreparedStatement prepareStatement, int pIndex, Object vobj, int sqlType) throws SQLException {
        Object paramVal = this.castJavaToSqlType(sqlType, vobj);

        switch (sqlType) {
            case Types.VARCHAR:
                prepareStatement.setString(pIndex, (String) paramVal);
                break;
            case Types.DATE:
                prepareStatement.setDate(pIndex, (Date) paramVal);
                break;
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
                    prepareStatement.setBigDecimal(pIndex, (BigDecimal) paramVal);
                }
                break;
            case Types.BLOB:
                prepareStatement.setBytes(pIndex, (byte[])paramVal);
                break;
            default:
                prepareStatement.setString(pIndex, paramVal.toString());
        }
    }
}
