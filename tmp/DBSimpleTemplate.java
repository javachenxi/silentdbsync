package cn.com.dbsync.dao;


import cn.com.dbsync.util.DBUtil;
import cn.com.dbsync.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by cxi on 2016/5/11.
 */
public class DBSimpleTemplate {

    private static final Log log = LogFactory.getLog(DBSimpleTemplate.class.getName());

    private static final String INST_CURRDATE_QUERY = "queryInstanceCurrdate";

    private static final String TASK_TYPE_SELECT = "SELECT";
    private static final String TASK_TYPE_INSERT = "INSERT";
    private static final String TASK_TYPE_UPDATE = "UPDATE";
    private static final String TASK_TYPE_DELETE = "DELETE";

    /**
     * The Orig dao.
     */
    protected IDAO origDao;
    private DAOEnv daoEnv;

    /**
     * Instantiates a new Db simple template.
     */
    public DBSimpleTemplate(){
        origDao = DAOFactory.getInstance().getDao();
        daoEnv = DAOEnv.getInstance();
    }

    /**
     * Select once dao result.
     *
     * @param taskName the task name
     * @param params   the params
     * @return the dao result
     */
    public DAOResult selectOnce(String taskName, List params) {
        return exeSQLTask(taskName, params,true);
    }

    /**
     * Select dao result.
     *
     * @param taskName the task name
     * @param params   the params
     * @return the dao result
     */
    public DAOResult select(String taskName, List params) {
        return exeSQLTask(taskName, params,false);
    }

    /**
     * Insert dao result.
     *
     * @param taskName the task name
     * @param params   the params
     * @return the dao result
     */
    public DAOResult insert(String taskName, List params) {
        return exeSQLTask(taskName, params,false);
    }


    /**
     * Update dao result.
     *
     * @param taskName the task name
     * @param params   the params
     * @return the dao result
     */
    public DAOResult update(String taskName, List params) {
        return exeSQLTask(taskName, params,false);
    }

    /**
     *
     * @param taskName
     * @param params
     * @param onceSelect - 查询SQL时，true为只查询第一条
     * @return
     */
    private DAOResult exeSQLTask(String taskName, List params,boolean onceSelect) {

        DAOResult daoResult = new DAOResult();
        TaskDomain taskDomain = daoEnv.getTaskDomain(taskName);
        SqlDomain sqlDomain = (SqlDomain)taskDomain.getSqlList().get(0);
        DatabaseDomain dbDomain = taskDomain.getDatabaseDomain();
        Connection conn = null;
        PreparedStatement pStatement = null;

        try {
            conn = origDao.getConnectionByDatabaseName(taskDomain.getDatabaseName());
            pStatement = conn.prepareStatement(formatSQLAppCharsetToDBCharset(taskDomain.getDatabaseName(),sqlDomain.getClause()));

            if(params != null && params.size()>0){
                List types = sqlDomain.getParameterTypeList();
                DBUtil.setPreparedStatementParams(dbDomain.getApp_charset(),dbDomain.getDb_charset(),pStatement, params, types);
            }
            String taskType = taskDomain.getType().toUpperCase();
            ResultSet resultSet = null;

            if(TASK_TYPE_SELECT.equals(taskType)){
                resultSet = pStatement.executeQuery();
                ResultSetMetaData rsmd = resultSet.getMetaData();
                DAOResultMetaData currResultMetaData = DBUtil.transResultMetaData(rsmd);
                daoResult.appendDAOResultMetaData(currResultMetaData);

                int colCount = rsmd.getColumnCount();
                ArrayList resData = new ArrayList();
                ArrayList rowdata = null;

                while (resultSet.next()) {
                    rowdata = new ArrayList(colCount);
                    for (int r = 0; r < colCount; r++) {
                        rowdata.add(DBUtil.castResultSetToJavaType(dbDomain.getDb_charset(),dbDomain.getApp_charset(),
                                resultSet, r+1, rsmd.getColumnType(r+1)));
                    }
                    resData.add(rowdata);

                    if(onceSelect){
                        break;
                    }
                }

                daoResult.appendResult(resData);
                resultSet.close();
            }else if(TASK_TYPE_INSERT.equals(taskType)
                    ||TASK_TYPE_UPDATE.equals(taskType)
                    ||TASK_TYPE_DELETE.equals(taskType)){
                int rel = pStatement.executeUpdate();
                daoResult.setResultValue(rel);
            }

            daoResult.setSuccess(true);
        } catch (SQLException e) {
            daoResult.setSuccess(false);
            daoResult.appendError("执行失败！taskName="+taskName);

            if(e.getMessage().indexOf("ORA-")!=-1){
                log.error("数据库错误信息："+DBUtil.convertStrCharset(dbDomain.getDb_charset(),dbDomain.getApp_charset(),e.getMessage()));
            }
            log.error("执行失败！taskName="+taskName, e);
        }finally {
            if(pStatement != null){
                try {
                    pStatement.close();
                } catch (SQLException e) {

                }
            }
            if(conn != null){
                try {
                    origDao.closeConnectionByDatabaseName(taskDomain.getDatabaseName(), conn);
                } catch (SQLException e) {

                }
            }
        }

        return daoResult;
    }


    /**
     * Format sql app charset to db charset string.
     *
     * @param databaseName the database name
     * @param sql          the sql
     * @return the string
     */
    public String formatSQLAppCharsetToDBCharset(String databaseName,String sql){

        DatabaseDomain domain = daoEnv.getDatabaseDomain(databaseName);

        if(!domain.isFormatLAppCharsetToDBCharset()){
            return sql;
        }

        if (domain == null) {
            log.debug("配置错误，在配置的dao-task中指定的数据库，在Database的配置文件中没有找到");
        }

        if (domain.getApp_charset().equalsIgnoreCase(domain.getDb_charset())) {
            return sql; // 如果两边的字符集相同则不进行转换
        }

        return StringUtil.convertString(sql,domain.getApp_charset(),domain.getDb_charset());
    }

    /**
     * 获取数据库时间
     *
     * @return calendar calendar
     */
    public Calendar getDBCurrDate(){
        DAOResult daoResult = origDao.select(INST_CURRDATE_QUERY,null);
        java.util.Date date = DBUtil.parseStrToDate(daoResult.getFirstSqlResultFirstCell());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }


}
