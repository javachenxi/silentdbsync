package cn.com.dbsync.service;

import cn.com.dbsync.bean.TaskInstBean;
import cn.com.dbsync.test.DBSyncTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Administrator on 2017-11-05.
 */
public class DBSyncInstServiceTest  extends DBSyncTest {

    @Autowired
    private DBSyncInstService dbSyncInstService ;

    @Test
    public void insertGenDBSyncTaskInst() {

        TaskInstBean taskInstBean = dbSyncInstService.insertGenDBSyncTaskInst();
        LOG.info(taskInstBean.getTaskName());
    }
    @Test
    public void updateTaskInstToStatusByIdTest(){
        boolean rebool = dbSyncInstService.updateTaskInstToStatusById(3,
                TaskInstBean.Status.UNHANDLE.value,
                TaskInstBean.Status.HANDLING.value);
        LOG.info(rebool);
    }

    @Test
    public void isHaveTaskInstTest(){
        int[] statusArray = {TaskInstBean.Status.UNHANDLE.value,TaskInstBean.Status.HANDLING.value};
        boolean rebool = dbSyncInstService.isHaveTaskInst(10000, statusArray);
        LOG.info(rebool);
    }

}
