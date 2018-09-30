package cn.com.dbsync.core;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created by Administrator on 2018/9/19/019.
 */
public class MyBatisDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceSwitch.getDataSourceType();
    }
}
