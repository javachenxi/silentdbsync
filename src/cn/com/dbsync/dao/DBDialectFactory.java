package cn.com.dbsync.dao;


import cn.com.dbsync.util.DBSyncConstant;

/**
 * <p>
 * Title: DBDialectFactory
 * </p>
 * <p>
 * Description:构建本地化DBDialect实例
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
 * @created 2016 /6/2
 */
public class DBDialectFactory {

    /**
     * Create db dialect db dialect.
     *
     * @return the db dialect
     */
    public static DBDialect createDBDialect(){
        return new OracleDBDialect();
    }

    /**
     * Create db dialect db dialect.
     *
     * @param appCharset the app charset
     * @param dbCharset  the db charset
     * @return the db dialect
     */
    public static DBDialect createDBDialect(String appCharset, String dbCharset){
        return new OracleDBDialect(appCharset, dbCharset);
    }

    /**
     * Create db dialect db dialect.
     *
     * @param dbtype     the dbtype
     * @param appCharset the app charset
     * @param dbCharset  the db charset
     * @return the db dialect
     */
    public static DBDialect createDBDialect(DBSyncConstant.DBType dbtype, String appCharset, String dbCharset){
        DBDialect dialect = null;

        switch (dbtype){
            case POSTGRESQLDB:
                dialect = new PostgreDBDialect();
                break;
            case ORACLEDB14:
                dialect = new OracleDBDialect14(appCharset, dbCharset);
                break;
            default:
                dialect = new OracleDBDialect(appCharset, dbCharset);
        }

        return dialect;
    }

}
