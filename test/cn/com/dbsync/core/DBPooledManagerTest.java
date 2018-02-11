package cn.com.dbsync.core;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.dao.DBPooledManager;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.test.DBSyncTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017-10-30.
 */
public class DBPooledManagerTest extends DBSyncTest {
    @Autowired
    private DBSyncConfService dbSyncConfService ;


    @Before
    public void setUp() {

    }

    @Test
    public void execSQLTest(){

        Connection connection = null;
        DBPooledManager dbPooledManager = DBPooledManager.getInstance();
        int[] test = null;

        try {
            ConfTaskBean confTaskBean = dbSyncConfService.getConfTaskByTaskId(10000);
            List<ConfTableBean> confTableList = dbSyncConfService.getConfTableByTaskId(10000);
            IGeneratorSql generatorSql = IGeneratorSqlFactory.getGeneratorSql(confTableList, confTaskBean);
            //解析配置表生成SQL
            generatorSql.buildsql();
            ConfTableBean confTableBean = confTableList.get(0);

            LOG.info("插入:"+confTableBean.getInsertSql() + " 删除:" + confTableBean.getDeleteSqlById());
            connection = dbPooledManager.getConnectionByPoolName("TDAPPOOL");

            connection.setAutoCommit(false);

            PreparedStatement insertPreStatement = connection.prepareStatement(confTableBean.getInsertSql());
            PreparedStatement deletePreStatement = connection.prepareStatement(confTableBean.getDeleteSqlById());
            //PreparedStatement updatePreStatement = connection.prepareStatement(confTableBean.getUpdateSql());
            PreparedStatement updatePreStatement2 = connection.prepareStatement(confTableBean.getUpdateSql());


            for(int i=0; i<2; i++){
                insertPreStatement.setString(1, "88888888"+i);
                insertPreStatement.setString(2, "88888888");
                insertPreStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                insertPreStatement.setBigDecimal(4, new BigDecimal(67+i));
                insertPreStatement.setObject(5, "88888888");

                insertPreStatement.addBatch();
            }

           try {
               test = insertPreStatement.executeBatch();
           }catch (Exception e){
               LOG.error("更新异常！", e);

               if(connection != null){
                   try {
                       connection.commit();
                   } catch (SQLException se) {
                       se.printStackTrace();
                   }
               }
           }

            int reUp = 0;

            for(int i=0; i<2; i++){
                updatePreStatement2.setString(1, "10000000"+i);
                updatePreStatement2.setString(2, "10000000");
                updatePreStatement2.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                updatePreStatement2.setObject(4, "10000000");
                updatePreStatement2.setBigDecimal(5, new BigDecimal(66+i));
                reUp = updatePreStatement2.executeUpdate();

                if(reUp == 0){
                    insertPreStatement.setString(1, "88888888"+i);
                    insertPreStatement.setString(2, "88888888");
                    insertPreStatement.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                    insertPreStatement.setBigDecimal(4, new BigDecimal(66+i));
                    insertPreStatement.setString(5, "88888888");
                    reUp = insertPreStatement.executeUpdate();
                }
            }

            /**
            for(int j=0; j<1; j++){
                deletePreStatement.setBigDecimal(1, new BigDecimal(66+j));
                deletePreStatement.addBatch();
            }

            test = deletePreStatement.executeBatch();
             */

        }catch (Exception e){
            LOG.error("异常", e);
        }finally {
            if(connection != null){
                try {
                    connection.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            dbPooledManager.closeConnectionByPoolName("TDAPPOOL", connection);
        }
    }


}
