package cn.com.dbsync.core;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.test.DBSyncTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Administrator on 2017-10-30.
 */
public class IGeneratorSqlTest extends DBSyncTest {

    @Autowired
    private DBSyncConfService dbSyncConfService ;


    @Test
    public void buildSQL() {
        ConfTaskBean confTaskBean = dbSyncConfService.getConfTaskByTaskId(999);
        List<ConfTableBean> confTableList = dbSyncConfService.getConfTableByTaskId(999);

        IGeneratorSql generatorSql = IGeneratorSqlFactory.getGeneratorSql(confTableList, confTaskBean);
        //解析配置表生成SQL
        generatorSql.buildsql();

        for(ConfTableBean confTable:confTableList){
           LOG.info(confTable.getUpdateSql());
        }

    }

}
