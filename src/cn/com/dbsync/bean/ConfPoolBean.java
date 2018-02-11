package cn.com.dbsync.bean;

/**
 * Created by Administrator on 2017-10-25.
 */
public class ConfPoolBean {

    private String poolName;

    private String jdbcUrl;

    private String driverClass;

    private String dbUser;

    private String dbPwd;

    private int initialPoolSize;

    private int minPoolSize;

    private int maxPoolSize;

    private int acquireIncrement;   //�����ӳ��е����Ӻľ���ʱ��c3p0һ��ͬʱ��ȡ��������

    private int maxStatements;      //��������Դ�ڼ��ص�PreparedStatements����

    private int maxStatementsPerConnection; //���ӳ��ڵ���������ӵ�е���󻺴�statements��

    private int maxIdletime;        //������ʱ��

    private String  dbCharset;      //���ݿ����

    private String  appCharset;     //app����

    private String dbType;

    public String getAppCharset() {
        return appCharset;
    }

    public void setAppCharset(String appCharset) {
        this.appCharset = appCharset;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPwd() {
        return dbPwd;
    }

    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getAcquireIncrement() {
        return acquireIncrement;
    }

    public void setAcquireIncrement(int acquireIncrement) {
        this.acquireIncrement = acquireIncrement;
    }

    public int getMaxStatements() {
        return maxStatements;
    }

    public void setMaxStatements(int maxStatements) {
        this.maxStatements = maxStatements;
    }

    public int getMaxStatementsPerConnection() {
        return maxStatementsPerConnection;
    }

    public void setMaxStatementsPerConnection(int maxStatementsPerConnection) {
        this.maxStatementsPerConnection = maxStatementsPerConnection;
    }

    public int getMaxIdletime() {
        return maxIdletime;
    }

    public void setMaxIdletime(int maxIdletime) {
        this.maxIdletime = maxIdletime;
    }

    public String getDbCharset() {
        return dbCharset;
    }

    public void setDbCharset(String dbCharset) {
        this.dbCharset = dbCharset;
    }
}
