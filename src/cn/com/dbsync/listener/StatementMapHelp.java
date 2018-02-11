package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfTableBean;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cxi on 2016/5/8.
 */
public class StatementMapHelp {

    private Map<Object,PreparedStatement> statementMap;

    /**
     * Instantiates a new Statement map help.
     */
    public StatementMapHelp(){
        statementMap = new HashMap<Object, PreparedStatement>();
    }

    /**
     * Put.
     *
     * @param bean      the bean
     * @param statement the statement
     */
    public void put(Object bean, PreparedStatement statement){
        statementMap.put(bean, statement);
    }

    /**
     * Get prepared statement.
     *
     * @param bean the bean
     * @return the prepared statement
     */
    public PreparedStatement get(Object bean) {
        PreparedStatement ps = statementMap.get(bean);
        return statementMap.get(bean);
    }

    /**
     * Close.
     *
     * @param bean the bean
     */
    public void close(Object bean){
        Statement statement = statementMap.get(bean);
        if(statement != null){
            try {
                statement.close();
            } catch (SQLException e) {

            }finally {
                statementMap.remove(bean);
            }
        }
    }

    /**
     * Close all.
     */
    public void closeAll(){
        Iterator<Object> it = statementMap.keySet().iterator();

        while (it.hasNext()){
            Statement statement = statementMap.get(it.next());
            if(statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
            it.remove();
        }
    }

}
