package cn.com.dbsync.util;

/**
 * <p>
 * Title: DBSyncConstant
 * </p>
 * <p>
 * Description:同步主键的常量
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
 * @created 2016 /5/14
 */
public interface DBSyncConstant {

    /**
     * The constant TASKCONF_EXECUTOR_KEY.
     */
     String TASKCONF_EXECUTOR_KEY="DBSyncConf";

    /**
     * The constant TASKINST_EXECUTOR_KEY.
     */
     String TASKINST_EXECUTOR_KEY="DBSyncInst";

    /**
     * The enum Db type.
     */
    enum DBType{
        /**
         * Oracle db db type.
         */
        ORACLEDB,

        /**
         * Oracle db db type.
         */
        ORACLEDB14,

        /**
         * Postgre sqldb db type.
         */
        POSTGRESQLDB,

        /**
         * DB2 sqldb type.
         */
        DB2,
        /**
         * Mysql 5.7 db
         */
        MYSQL,

        /**
         *  Clickhouse db
         */
        CLICKHOUSE
    }

}
