package cn.com.dbsync.startup;

import cn.com.dbsync.core.DBSyncContext;
import cn.com.dbsync.schedule.ScheduleContainer;
import cn.com.dbsync.service.DBSyncInstService;

/**
 * Created by Administrator on 2017-10-26.
 */
public class MainRunner {

    public static void main(String[] args){
        ScheduleContainer scheduleContainer  = ScheduleContainer.getInstance();
        DBSyncInstService dbSyncInstService = scheduleContainer.getDbSyncInstService();
        //初始化生成同步任务线程
        dbSyncInstService.insertGenDBSyncTaskInst();
        //启动任务线程
        scheduleContainer.start();
    }


}
