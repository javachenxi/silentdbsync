package cn.com.dbsync.dao;

import cn.com.dbsync.bean.ConfPoolBean;
import cn.com.dbsync.core.DBSyncException;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.util.SpringManager;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017-10-23.
 */
public class DBPooledManager {

    private static final Log log = LogFactory.getLog(DBPooledManager.class.getName());

    private final HashMap<String, AtomicInteger> POOLNAME_COUNTER = new HashMap<String, AtomicInteger>();

    private final HashMap<String, ConfBeanAndDBPooledPair> POOLNAME_POOLEDPAIR = new HashMap<String, ConfBeanAndDBPooledPair>();

    private final ReentrantLock pooledPairLock = new ReentrantLock();

    private final ReentrantLock counterLock = new ReentrantLock();

    private final static ReentrantLock pooledMngLock = new ReentrantLock();

    private DBSyncConfService dbSyncConfService;

    private static DBPooledManager pooledManager ;

    private DBPooledManager(){
        dbSyncConfService = SpringManager.getInstance().getBeanByType(DBSyncConfService.class);
    }


    public static DBPooledManager getInstance(){

        if(pooledManager == null){
            try {
                pooledMngLock.lock();

                if (pooledManager == null) {
                    pooledManager = new DBPooledManager();
                }
            }catch (Exception e){
                log.error( "DBPooledManager初始化异常", e);
            }finally {
                pooledMngLock.unlock();
            }
        }

        return pooledManager;
    }

    private void initDBPool(String poolName, boolean isInitPool){
        try {
            ConfPoolBean confPoolBean = dbSyncConfService.getConfPoolById(poolName);

            if(confPoolBean == null){
              throw new DBSyncException("配置信息不存在！poolName=" + poolName);
            }

            ComboPooledDataSource comboPooledDataSource = null;

            if(isInitPool) {
                comboPooledDataSource = new ComboPooledDataSource();
                comboPooledDataSource.setJdbcUrl(confPoolBean.getJdbcUrl());
                comboPooledDataSource.setUser(confPoolBean.getDbUser());
                comboPooledDataSource.setPassword(confPoolBean.getDbPwd());
                comboPooledDataSource.setAcquireIncrement(confPoolBean.getAcquireIncrement());
                comboPooledDataSource.setDriverClass(confPoolBean.getDriverClass());
                comboPooledDataSource.setMaxIdleTime(confPoolBean.getMaxIdletime());
                comboPooledDataSource.setInitialPoolSize(confPoolBean.getInitialPoolSize());
                comboPooledDataSource.setMaxPoolSize(confPoolBean.getMaxPoolSize());
                comboPooledDataSource.setMinPoolSize(confPoolBean.getMinPoolSize());
                comboPooledDataSource.setMaxStatements(confPoolBean.getMaxStatements());
                comboPooledDataSource.setMaxStatementsPerConnection(confPoolBean.getMaxStatementsPerConnection());
            }

            POOLNAME_POOLEDPAIR.put(poolName, new ConfBeanAndDBPooledPair(confPoolBean, comboPooledDataSource));

        }catch (Exception e){
            throw new DBSyncException("初始化链接池失败！poolName="+poolName, e);
        }
    }

    public ConfPoolBean getConfPoolBean(String poolName){
        ConfBeanAndDBPooledPair pooledPair = getPairThreadsafe(poolName, false);
        return  pooledPair==null? null : pooledPair.getConfPoolBean();
    }

    public Connection getConnectionByPoolName(String poolName)throws SQLException{

        Connection connection = null;
        ConfBeanAndDBPooledPair pooledPair = getPairThreadsafe(poolName, true);

        if(pooledPair!=null){
            AtomicInteger atomicInteger = getCounterThreadsafe(poolName);
            int num = atomicInteger.incrementAndGet();
            connection = pooledPair.getPooledDataSource().getConnection();

            if(log.isDebugEnabled()){
                log.debug("线程池：" + poolName +" 数量：" + num);
            }
        }

        return connection;
    }

    public boolean closeConnectionByPoolName(String poolName, Connection connection){

        boolean rebool = true;

        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                rebool = false;
            }
        }

        AtomicInteger atomicInteger = getCounterThreadsafe(poolName, false);

        if(atomicInteger != null){

            int num = atomicInteger.decrementAndGet();

            if(log.isDebugEnabled()){
                log.debug("线程池：" + poolName +" 数量：" + num);
            }
        }else{
            if(log.isDebugEnabled()){
                log.debug("线程池：" + poolName +" 不存在计数器");
            }
        }

        return rebool;
    }

    private AtomicInteger getCounterThreadsafe(String poolName){
        return this.getCounterThreadsafe(poolName, true);
    }

    private AtomicInteger getCounterThreadsafe(String poolName, boolean isCreated){
        AtomicInteger atomicInteger = POOLNAME_COUNTER.get(poolName);

        if(atomicInteger == null){
            try{
                counterLock.lock();
                atomicInteger = POOLNAME_COUNTER.get(poolName);

                if(atomicInteger == null && isCreated){
                    atomicInteger = new AtomicInteger(0);
                    POOLNAME_COUNTER.put(poolName, atomicInteger);
                }
            }finally {
                counterLock.unlock();
            }
        }

        return atomicInteger;
    }

    private ConfBeanAndDBPooledPair getPairThreadsafe(String poolName, boolean isInitPool){
        ConfBeanAndDBPooledPair pooledPair = POOLNAME_POOLEDPAIR.get(poolName);

        if(pooledPair == null || (isInitPool && pooledPair.getPooledDataSource() == null)){
            try {
                pooledPairLock.lock();
                pooledPair = POOLNAME_POOLEDPAIR.get(poolName);

                if(pooledPair == null||(isInitPool && pooledPair.getPooledDataSource() == null)){
                    this.initDBPool(poolName, isInitPool);
                    pooledPair = POOLNAME_POOLEDPAIR.get(poolName);
                }
            }finally {
                pooledPairLock.unlock();
            }
        }

        return pooledPair;
    }

    public class ConfBeanAndDBPooledPair{

        private ConfPoolBean confPoolBean;

        private ComboPooledDataSource pooledDataSource;

        public ConfBeanAndDBPooledPair(ConfPoolBean confPoolBean, ComboPooledDataSource pooledDataSource){
            this.confPoolBean = confPoolBean;
            this.pooledDataSource = pooledDataSource;
        }

        public ConfPoolBean getConfPoolBean() {
            return confPoolBean;
        }

        public void setConfPoolBean(ConfPoolBean confPoolBean) {
            this.confPoolBean = confPoolBean;
        }

        public ComboPooledDataSource getPooledDataSource() {
            return pooledDataSource;
        }

        public void setPooledDataSource(ComboPooledDataSource pooledDataSource) {
            this.pooledDataSource = pooledDataSource;
        }
    }

}













