package cn.com .dbsync.dao;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.listener.StatementMapHelp;
import cn.com.dbsync.util.CommUtil;
import cn.com.dbsync.util.DBSyncConstant;
import oracle.jdbc.driver.OraclePreparedStatement;
import oracle.sql.BLOB;
import oracle.sql.CLOB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Title: OracleDBDialect
 * </p>
 * <p>
 * Description:关于Oracle数据库本地化处理
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
public class OracleDBDialect14 extends DBDialect {
    private static final Log log = LogFactory.getLog(OracleDBDialect14.class);
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
    private static final int CLOB_STR_MAX=1300;
    private static final int BLOB_STR_MAX=4000;

    static {
        SqlTypeToDDLTypeMap.put(Types.VARCHAR, "VARCHAR(500)");
        SqlTypeToDDLTypeMap.put(Types.DOUBLE, "NUMBER(19,6)");
        SqlTypeToDDLTypeMap.put(Types.FLOAT, "NUMBER(19,2)");
        SqlTypeToDDLTypeMap.put(Types.INTEGER, "NUMBER(12)");
        SqlTypeToDDLTypeMap.put(Types.NUMERIC, "NUMBER(20)");
        SqlTypeToDDLTypeMap.put(Types.TIMESTAMP, "DATE");
        SqlTypeToDDLTypeMap.put(Types.BOOLEAN, "NUMBER(1)");
        SqlTypeToDDLTypeMap.put(Types.CLOB, "CLOB");
        SqlTypeToDDLTypeMap.put(Types.BLOB, "BLOB");
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
     * Instantiates a new Oracle db dialect.
     */
    public OracleDBDialect14() {
        super();
    }

    /**
     * Instantiates a new Oracle db dialect.
     *
     * @param appCharset the app charset
     * @param dbCharset  the db charset
     */
    public OracleDBDialect14(String appCharset, String dbCharset) {
        super(appCharset, dbCharset);
    }

    public int transTasktypeToSQLType(String tasktype) {
        return TypeToSqlTypeMap.get(tasktype);
    }


    /**
     * 查询结果集的字段类型转化为符合JAVA的类型数据，对字符串做转码
     * @param sqltype
     * @param resultSet
     * @param index
     * @return
     */
    public Object castResultSetToJavaType(int sqltype, ResultSet resultSet, int index) throws SQLException {
        Object retobj = null;

        switch (sqltype) {
            case Types.NUMERIC:
                BigDecimal valnum = resultSet.getBigDecimal(index);

                if(valnum == null){
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

                if(blob == null){
                    break;
                }

                InputStream in = null;

                try {
                    in = blob.getBinaryStream();
                    int length = (int) blob.length();
                    if(length > 1024*1024){
                        retobj = CommUtil.storeTempFile(in);
                    }else {
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
                if(clob == null){
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
                /**
                 byte[] valstr = resultSet.getBytes(index);

                 if(valstr == null || valstr.length == 0){
                 break;
                 }

                 try {
                 if (ENCODE_ISO.equals(dbCharset)) {
                 retobj = new String(valstr, ENCODE_GBK);
                 } else {
                 retobj = new String(valstr, dbCharset);
                 }
                 }catch (Exception e){
                 log.warn("转化编码异常 encode="+dbCharset, e);
                 }*/
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

        return retobj;
    }


    /**
     * 依据数据库表字段的类型，强制转化Java的类型
     *
     * @param sqltype
     * @param valObj
     * @return
     */
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
                if(valObj instanceof java.sql.Timestamp){
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
                retobj = new java.sql.Timestamp(ret.getTime());

                break;
            case Types.NUMERIC:
                if(valObj instanceof BigDecimal){
                    retobj = valObj;
                    break;
                }

                if(valObj instanceof Double){
                    Double tmpd = (Double) valObj;
                    if(Double.isNaN(tmpd) ||Double.isInfinite(tmpd) ){
                        retobj = null;
                    }else{
                        retobj = new BigDecimal(tmpd);
                    }
                }else if (valObj instanceof Float) {
                    Float tmpf = (Float) valObj;
                    if(Float.isNaN(tmpf)||Float.isInfinite(tmpf) ){
                        retobj = null;
                    }else{
                        retobj = new BigDecimal(tmpf);
                    }
                }else if (valObj instanceof String) {
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
                }else {
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

    public void setPreparedStatementNullParam( PreparedStatement prepareStatement,
                                               int pIndex, int sqlType) throws SQLException{
        switch (sqlType){
            case Types.VARCHAR:
            case Types.CHAR:
                prepareStatement.setNull(pIndex, Types.VARCHAR);
                break;
            case Types.NUMERIC:
                prepareStatement.setNull(pIndex, Types.NUMERIC);
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
                prepareStatement.setNull(pIndex, Types.DATE);
                break;
            default:
                prepareStatement.setNull(pIndex, sqlType);
        }
    }

    public String transTypeSqlToDDL(int sqltype) {
        return SqlTypeToDDLTypeMap.get(sqltype);
    }

    public void setPreparedStatementNotNullParam(PreparedStatement prepareStatement, int pIndex, Object vobj, int sqlType) throws SQLException {
        Object paramVal = this.castJavaToSqlType(sqlType, vobj);

        switch (sqlType) {
            case Types.BOOLEAN:
                prepareStatement.setInt(pIndex, (Integer) paramVal);
                break;
            case Types.VARCHAR:
                prepareStatement.setString(pIndex, (String) paramVal);
                break;
            case Types.CHAR:
                ((OraclePreparedStatement) prepareStatement).setFixedCHAR(pIndex, (String) paramVal);
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
                prepareStatement.setInt(pIndex, (Integer) paramVal);
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
            case Types.BLOB:
                BLOB blob = null;
                if(paramVal instanceof byte[]){
                    byte[] tmpbytes = (byte[])paramVal;
                    //prepareStatement.setBytes(pIndex, tmpbytes);
                    if(tmpbytes.length < BLOB_STR_MAX ){
                        prepareStatement.setBytes(pIndex, tmpbytes);
                    }else {
                        blob = BLOB.createTemporary(prepareStatement.getConnection(), true, BLOB.DURATION_SESSION);
                        writeBlob(blob,tmpbytes, 1);
                        prepareStatement.setBlob(pIndex, blob);
                    }
                }else if(paramVal instanceof File){
                    File tmpFile = (File) paramVal;
                    FileInputStream inputStream = null;

                    try {
                        blob = BLOB.createTemporary(prepareStatement.getConnection(), true, BLOB.DURATION_SESSION);
                        inputStream = new FileInputStream(tmpFile);
                        //按BLOB进行分包保存
                        if(tmpFile.length()>BLOB.MAX_CHUNK_SIZE){
                            long pos = 1;

                            while (tmpFile.length()>pos){
                                pos = writeBlob(blob, CommUtil.readInputStream(inputStream, BLOB.MAX_CHUNK_SIZE), pos);
                            }

                        }else{
                            writeBlob(blob, CommUtil.readInputStream(inputStream, (int)tmpFile.length()), 1);
                        }
                    }catch (Exception e){

                    }finally {
                        if(inputStream != null){
                            try {
                                inputStream.close();
                            } catch (IOException e) {

                            }
                        }
                    }
                    prepareStatement.setBlob(pIndex, blob);
                }

                break;
            case Types.CLOB:
                CLOB clob = null;
                if(paramVal instanceof String){
                    String clobstr = (String) paramVal;
                    // prepareStatement.setString(pIndex, clobstr);
                    if(clobstr.length() < CLOB_STR_MAX){
                        prepareStatement.setString(pIndex, clobstr);
                    }else {
                        clob = CLOB.createTemporary(prepareStatement.getConnection(), true, CLOB.DURATION_SESSION);
                        writeClob(clob, clobstr, 1);
                        prepareStatement.setClob(pIndex, clob);
                    }
                }else if(paramVal instanceof File){
                    File tmpFile = (File) paramVal;
                    FileReader fileReader = null;

                    try {
                        clob = CLOB.createTemporary(prepareStatement.getConnection(), true, CLOB.DURATION_SESSION);
                        fileReader = new FileReader(tmpFile);
                        String tmpStr = CommUtil.readCharacterStream(fileReader, CLOB.MAX_CHUNK_SIZE);

                        if(tmpStr !=null &&tmpStr.length()==BLOB.MAX_CHUNK_SIZE){
                            long pos = 1;
                            while (tmpStr !=null && tmpStr.length() == BLOB.MAX_CHUNK_SIZE){
                                pos = writeClob(clob, tmpStr, pos);
                                tmpStr = CommUtil.readCharacterStream(fileReader, CLOB.MAX_CHUNK_SIZE);
                            }
                        }else{
                            writeClob(clob, tmpStr, 1);
                        }
                    }catch (Exception e){

                    }finally {
                        if(fileReader != null){
                            try {
                                fileReader.close();
                            } catch (IOException e) {

                            }
                        }
                    }
                    prepareStatement.setClob(pIndex, clob);
                }

                break;
            default:
                prepareStatement.setObject(pIndex, paramVal);
        }
    }

    public PreparedStatement getInsertPreparedStatement(Connection sourConn, StatementMapHelp statementMapHelp, ConfTableBean confTableBean)
            throws SQLException{
        PreparedStatement ps = statementMapHelp.get(confTableBean.getMergInsertSql());

        if(ps == null){
            ps = sourConn.prepareStatement(confTableBean.getMergInsertSql());
            statementMapHelp.put(confTableBean.getMergInsertSql(), ps);

            if(log.isDebugEnabled()){
                log.debug("执行SQL:" + confTableBean.getMergInsertSql());
            }
        }

        return ps;
    }

    private long writeClob(CLOB clob,String clobstr,long pos) throws SQLException{
        int putlen = clob.putString(pos, clobstr);
        int needlen = clobstr.length() - putlen;
        String tmp = null;

        while(needlen > 0){
            tmp = clobstr.substring(clobstr.length()-needlen);
            putlen = clob.putString(pos, tmp);
            needlen = needlen - putlen;
        }

        return pos+clobstr.length();
    }

    private long writeBlob(BLOB blob, byte[] bytes, long pos) throws SQLException {
        int putlen = blob.putBytes(pos, bytes);
        int needlen = bytes.length - putlen;
        byte[] tmp = null;

        while(needlen > 0){
            tmp = new byte[needlen];
            System.arraycopy(bytes, bytes.length-needlen, tmp, 0, tmp.length);
            putlen = blob.putBytes(pos, tmp);
            needlen = needlen - putlen;
        }

        return pos+bytes.length;
    }

    public DBSyncConstant.DBType getDBType(){
        return DBSyncConstant.DBType.ORACLEDB14;
    }


}
