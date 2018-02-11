package cn.com.dbsync.dao;


import cn.com.dbsync.util.CommUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2017-10-25.
 */

public class DBMybatisSimpleTemplate  {

    private static final Log log = LogFactory.getLog(DBMybatisSimpleTemplate.class.getName());

    private static final String INST_CURRDATE_QUERY = "DBSyncConfMapper.queryInstanceCurrdate";

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    public <T> T selectOnce(String taskName, Object params) {

        SqlSession sqlSession = sqlSessionFactory.openSession();
        T rt = null;

        try {

            rt = sqlSession.selectOne(taskName, params);

        }finally {
           sqlSession.close();
        }

        return rt;
    }


    public <T> List<T> select(String taskName, Object params){
        SqlSession sqlSession = sqlSessionFactory.openSession();
        List<T> listrt = null;

        try {
            listrt = sqlSession.selectList(taskName, params);
        }finally {
            sqlSession.close();
        }

        return listrt;
    }


    public int insert(String taskName, Object params) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        int rt = 0;

        try {
            rt = sqlSession.insert(taskName, params);
        } finally {
            sqlSession.close();
        }

        return rt;
    }

    public int update(String taskName, Object params){
        SqlSession sqlSession = sqlSessionFactory.openSession();
        int rt = 0;

        try {
            rt = sqlSession.update(taskName, params);
        } finally {
            sqlSession.close();
        }

        return rt;
    }

    /**
     * 获取数据库时间
     *
     * @return calendar calendar
     */
    public Calendar getDBCurrDate(){
        String datestr = (String) this.selectOnce(INST_CURRDATE_QUERY, null);
        java.util.Date date = CommUtil.parseStrToDate(datestr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

}
