package cn.com.dbsync.core;

import cn.com.dbsync.bean.ConfPoolBean;
import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.util.DBSyncConstant;
import cn.com.dbsync.util.SpringManager;

import java.util.List;


/**
 * Created by cxi on 2016/5/8.
 */
public class IGeneratorSqlFactory {

    /**
     * Get generator sql generator sql.
     *
     * @param confTableBeanList the conf table bean list
     * @param confTaskBean      the conf task bean
     * @return the generator sql
     */
    public static IGeneratorSql getGeneratorSql(List<ConfTableBean> confTableBeanList, ConfTaskBean confTaskBean){

        DBSyncConfService dbSyncConfService = SpringManager.getInstance().getBeanByType(DBSyncConfService.class);
        ConfPoolBean confPoolBean = dbSyncConfService.getConfPoolById(confTableBeanList.get(0).getTargetDbName());
        DBSyncConstant.DBType dbtype = Enum.valueOf(DBSyncConstant.DBType.class, confPoolBean.getDbType());
        DefaultGeneratorSql generatorSql = null;

        switch (dbtype){
            case MYSQL:
            case CLICKHOUSE:
                generatorSql = new MysqlGeneratorSql(confTableBeanList, confTaskBean);
                break;
            default:
                generatorSql = new DefaultGeneratorSql(confTableBeanList, confTaskBean);
        }

        return generatorSql;
    }
}
