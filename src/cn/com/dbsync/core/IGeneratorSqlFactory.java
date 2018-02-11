package cn.com.dbsync.core;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;

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
        return new DefaultGeneratorSql(confTableBeanList, confTaskBean);
    }
}
