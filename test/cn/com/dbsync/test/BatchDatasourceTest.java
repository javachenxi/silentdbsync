package cn.com.dbsync.test;


import cn.com.dbsync.dao.DAOResultMetaData;
import cn.com.dbsync.dao.DBDialect;
import cn.com.dbsync.dao.DBDialectFactory;
import cn.com.dbsync.util.DBSyncConstant;
import org.junit.Test;
import ru.yandex.clickhouse.ClickHouseConnectionImpl;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import java.sql.*;


/**
 * Created by Administrator on 2018/9/18/018.
 */
public class BatchDatasourceTest {

    @Test
    public void selectDatas(){
        ClickHouseProperties clickHouseProperties = new ClickHouseProperties();
        clickHouseProperties.setUser("chendb");
        clickHouseProperties.setPassword("12345678");
        ClickHouseDataSource dataSource = new ClickHouseDataSource("jdbc:clickhouse://192.168.6.130:8123/chendb", clickHouseProperties);
        String psql = "select * from tag_testtab";
        ClickHouseConnectionImpl connection = null;

        try {
            connection = (ClickHouseConnectionImpl) dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(psql);
            preparedStatement.setFetchSize(10);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            DBDialect dbDialect = DBDialectFactory.createDBDialect(DBSyncConstant.DBType.CLICKHOUSE, "", "");
            DAOResultMetaData daoResultMetaData = dbDialect.transResultMetaData(resultSetMetaData);
            StringBuilder stringBuilder = new StringBuilder();
            System.out.println(daoResultMetaData.toString());

            while(resultSet.next()) {
                stringBuilder.append( resultSet.getString(1)).append("  ");
                stringBuilder.append( resultSet.getString(2)).append("  ");
                stringBuilder.append( resultSet.getString(3)).append("  ");
                stringBuilder.append( resultSet.getString(4)).append("  ");

                System.out.println(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
            }

            resultSet.close();

        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Test
    public void addBatchDatas(){
        ClickHouseProperties clickHouseProperties = new ClickHouseProperties();
        clickHouseProperties.setUser("chendb");
        clickHouseProperties.setPassword("12345678");
        ClickHouseDataSource dataSource = new ClickHouseDataSource("jdbc:clickhouse://192.168.6.130:8123/chendb", clickHouseProperties);
        String psql = "insert into testMTree(rdate, id, name, point) values(?, ?, ?, ?)";
        ClickHouseConnectionImpl connection = null;

        try {
            connection = (ClickHouseConnectionImpl) dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(psql);

            for(int i=0; i< 10; i++){
                preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
                preparedStatement.setInt(2, i);
                preparedStatement.setString(3, "test"+i);
                preparedStatement.setInt(4, i+1);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();

        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
