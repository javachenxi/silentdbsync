package cn.com.dbsync.dao;

import cn.com.dbsync.util.CommUtil;
import cn.com.dbsync.util.DBSyncConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Title: PostgreDBDialect
 * </p>
 * <p>
 * Description:对PG数据的支持
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
 * @created 2016 /10/12
 */
public class PostgreDBDialect extends DBDialect {

    private static final Log log = LogFactory.getLog(PostgreDBDialect.class);
    private static final Map<String, Integer> TypeToSqlTypeMap = new HashMap<String, Integer>();
    /**
     * The constant SqlTypeToDDLTypeMap.
     */
    protected static final Map<Integer, String> SqlTypeToDDLTypeMap = new HashMap<Integer, String>();
    private static final BigDecimal LONG_MAX = new BigDecimal(Long.MAX_VALUE);
    private static final BigDecimal LONG_MIN = new BigDecimal(Long.MIN_VALUE);
    private static final BigDecimal DOUBLE_MAX = new BigDecimal(Double.MAX_VALUE);
    private static final BigDecimal DOUBLE_MIN = new BigDecimal(Double.MIN_VALUE);
    private static final BigDecimal INTEGER_MAX = new BigDecimal(Integer.MAX_VALUE);
    private static final BigDecimal INTEGER_MIN = new BigDecimal(Integer.MIN_VALUE);
    private static final int CLOB_STR_MAX = 1300;
    private static final int BLOB_STR_MAX = 4000;

    static {
        SqlTypeToDDLTypeMap.put(Types.VARCHAR, "VARCHAR(500)");
        SqlTypeToDDLTypeMap.put(Types.DOUBLE, "NUMERIC(19,6)");
        SqlTypeToDDLTypeMap.put(Types.FLOAT, "NUMERIC(19,2)");
        SqlTypeToDDLTypeMap.put(Types.INTEGER, "NUMERIC(12)");
        SqlTypeToDDLTypeMap.put(Types.NUMERIC, "NUMERIC(20)");
        SqlTypeToDDLTypeMap.put(Types.TIMESTAMP, "DATE");
        SqlTypeToDDLTypeMap.put(Types.BOOLEAN, "NUMERIC(1)");
        SqlTypeToDDLTypeMap.put(Types.CLOB, "TEXT");
        SqlTypeToDDLTypeMap.put(Types.BLOB, "BYTEA");
        SqlTypeToDDLTypeMap.put(Types.CHAR, "CHAR(1)");

        TypeToSqlTypeMap.put("String", Types.VARCHAR);
        TypeToSqlTypeMap.put("Long", Types.NUMERIC);
        TypeToSqlTypeMap.put("long", Types.NUMERIC);
        TypeToSqlTypeMap.put("Integer", Types.INTEGER);
        TypeToSqlTypeMap.put("int", Types.INTEGER);
        TypeToSqlTypeMap.put("Byte", Types.INTEGER);
        TypeToSqlTypeMap.put("byte", Types.INTEGER);
        TypeToSqlTypeMap.put("Float", Types.FLOAT);
        TypeToSqlTypeMap.put("float", Types.FLOAT);
        TypeToSqlTypeMap.put("Double", Types.DOUBLE);
        TypeToSqlTypeMap.put("double", Types.DOUBLE);
        TypeToSqlTypeMap.put("Date", Types.TIMESTAMP);
        TypeToSqlTypeMap.put("Clob", Types.CLOB);
        TypeToSqlTypeMap.put("Blob", Types.BLOB);
    }

    /**
     * Instantiates a new Postgre db dialect.
     */
    public PostgreDBDialect() {
        super();
    }

    /**
     * Instantiates a new Postgre db dialect.
     *
     * @param appCharset the app charset
     * @param dbCharset  the db charset
     */
//带应用字符编码及数据库字符编码参数的构造器
    public PostgreDBDialect(String appCharset, String dbCharset) {
        super(appCharset, dbCharset);
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
                } else {
                    retobj = valObj;
                }
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
                if (valObj instanceof Timestamp) {
                    retobj = valObj;
                    break;
                }
                Date ret = null;
                if (valObj instanceof String) {
                    ret = CommUtil.parseStrToDate((String) valObj);
                } else if (valObj instanceof Long) {
                    ret = new Date((Long) valObj);
                } else {
                    ret = (Date) valObj;
                }
                retobj = new Timestamp(ret.getTime());

                break;
            case Types.NUMERIC:
                if (valObj instanceof BigDecimal) {
                    retobj = valObj;
                    break;
                }

                if (valObj instanceof Double) {
                    Double tmpd = (Double) valObj;
                    if (Double.isNaN(tmpd) || Double.isInfinite(tmpd)) {
                        retobj = null;
                    } else {
                        retobj = new BigDecimal(tmpd);
                    }
                } else if (valObj instanceof Float) {
                    Float tmpf = (Float) valObj;
                    if (Float.isNaN(tmpf) || Float.isInfinite(tmpf)) {
                        retobj = null;
                    } else {
                        retobj = new BigDecimal(tmpf);
                    }
                } else if (valObj instanceof String) {
                    retobj = new BigDecimal((String) valObj);
                } else if (valObj instanceof Integer) {
                    retobj = new BigDecimal((Integer) valObj);
                } else if (valObj instanceof Long) {
                    retobj = new BigDecimal((Long) valObj);
                } else {
                    retobj = new BigDecimal(valObj.toString());
                }
                break;
            case Types.CLOB:
                if (valObj instanceof String) {
                    retobj = (String) convertStrCharset(appCharset, dbCharset, valObj);
                } else {
                    retobj = valObj;
                }
                break;
            case Types.VARCHAR:
            case Types.CHAR:
                if (valObj instanceof String) {
                    retobj = (String) convertStrCharset(appCharset, dbCharset, valObj);
                } else {
                    retobj = convertStrCharset(appCharset, dbCharset, String.valueOf(valObj));
                }
                break;
            case Types.DOUBLE:
                if (valObj instanceof Double) {
                    retobj = valObj;
                    break;
                }

                if (valObj instanceof String) {
                    retobj = Double.parseDouble((String) valObj);
                } else if (valObj instanceof Integer) {
                    retobj = ((Integer) valObj).doubleValue();
                } else if (valObj instanceof Long) {
                    retobj = ((Long) valObj).doubleValue();
                } else if (valObj instanceof Float) {
                    retobj = ((Float) valObj).doubleValue();
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
                } else if (valObj instanceof Byte) {
                    retobj = ((Byte) valObj).intValue();
                } else {
                    retobj = valObj;
                }
                break;
            case Types.BLOB:
                if (valObj instanceof String) {
                    retobj = ((String) retobj).getBytes();
                } else {
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
            case Types.NUMERIC:
                BigDecimal valnum = resultSet.getBigDecimal(index);

                if (valnum == null) {
                    break;
                }

                if (valnum.scale() > 0) {
                    if (DOUBLE_MAX.compareTo(valnum) > -1 && DOUBLE_MIN.compareTo(valnum) < 1) {
                        retobj = valnum.doubleValue();
                    }
                } else {
                    if (INTEGER_MAX.compareTo(valnum) > -1 && INTEGER_MIN.compareTo(valnum) < 1) {
                        retobj = valnum.intValue();
                    } else if (LONG_MAX.compareTo(valnum) > -1 && LONG_MIN.compareTo(valnum) < 1) {
                        retobj = valnum.longValue();
                    }
                }

                break;
            case Types.BLOB:
                Blob blob = resultSet.getBlob(index);

                if (blob == null) {
                    break;
                }

                InputStream in = null;

                try {
                    in = blob.getBinaryStream();
                    int length = (int) blob.length();
                    if (length > 1024 * 1024) {
                        retobj = CommUtil.storeTempFile(in);
                    } else {
                        retobj = CommUtil.readInputStream(in, length);
                    }
                } catch (Exception e) {

                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                    }
                }

                break;
            case Types.CLOB:
                Clob clob = resultSet.getClob(index);
                if (clob == null) {
                    break;
                }
                Reader reader = null;
                try {
                    reader = clob.getCharacterStream();
                    int length = (int) clob.length();
                    retobj = CommUtil.readCharacterStream(reader, length);
                    retobj = convertStrCharset(dbCharset, appCharset, retobj);
                } catch (Exception e) {

                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e) {
                        }
                    }
                }
                break;
            case Types.VARCHAR:
            case Types.CHAR:
                retobj = convertStrCharset(dbCharset, appCharset, resultSet.getString(index));

                break;
            case Types.DATE:
            case Types.TIMESTAMP:
            default:
                Object valObj = resultSet.getObject(index);
                if (valObj instanceof String) {
                    retobj = convertStrCharset(dbCharset, appCharset, valObj);
                } else if (valObj instanceof BigDecimal) {
                    retobj = ((BigDecimal) valObj).longValue();
                } else {
                    retobj = valObj;
                }
        }
        //todo 正确的返回值
        return "";
    }


    @Override
    public String transTypeSqlToDDL(int sqltype) {
        return SqlTypeToDDLTypeMap.get(sqltype);
    }

    @Override
    public void setPreparedStatementNotNullParam(PreparedStatement prepareStatement, int pIndex, Object vobj, int sqlType) throws SQLException {
        Object paramVal = this.castJavaToSqlType(sqlType, vobj);

        switch (sqlType) {
            case Types.BOOLEAN:
                prepareStatement.setInt(pIndex, (Integer) paramVal);
                break;
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
            case Types.NUMERIC:
                if (paramVal == null) {
                    prepareStatement.setNull(pIndex, Types.NUMERIC);
                } else {
                    prepareStatement.setBigDecimal(pIndex, (BigDecimal) paramVal);
                }
                break;
            default:
                prepareStatement.setObject(pIndex, paramVal);
        }

    }

    public DBSyncConstant.DBType getDBType(){
        return DBSyncConstant.DBType.POSTGRESQLDB;
    }
}
