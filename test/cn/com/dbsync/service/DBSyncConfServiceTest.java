package cn.com.dbsync.service;

import cn.com.dbsync.bean.ConfPoolBean;
import cn.com.dbsync.test.DBSyncTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Administrator on 2017-10-26.
 */
public class DBSyncConfServiceTest extends DBSyncTest {

    @Autowired
    private DBSyncConfService dbSyncConfService ;

    @Before
    public void setUp(){

    }

    @Test
    public void getConfPoolByIdTest(){
        ConfPoolBean confPoolBean = dbSyncConfService.getConfPoolById("TDAPPOOL");
        LOG.info(confPoolBean.getPoolName());
    }
    @Test
    public void updateTaskConfForLast(){
        boolean rebool = dbSyncConfService.updateTaskConfForLast(null, 10000);
        LOG.info(rebool);
    }


}
