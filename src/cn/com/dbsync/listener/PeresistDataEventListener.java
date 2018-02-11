package cn.com.dbsync.listener;

import cn.com.dbsync.dao.*;

import cn.com.dbsync.bean.*;
import cn.com.dbsync.core.DBSyncException;
import cn.com.dbsync.core.DispatchEventContainer;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.util.DBSyncConstant;
import cn.com.dbsync.util.SpringManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 处理 PeresistDataEvent 事件依据配置保存数据固化到数据库
 * <p>
 * Created by cxi on 2016/5/7.
 */
public class PeresistDataEventListener extends EventListenerAdapter {
    private final static Log LOG = LogFactory.getLog(PeresistDataEventListener.class.getName());

    protected static ThreadLocal<StatementMapHelp> threadContextStatement = new ThreadLocal<StatementMapHelp>();
    protected static ThreadLocal<StatiInfoBean> threadContextStatiInfo = new ThreadLocal<StatiInfoBean>();
    protected static ThreadLocal<Connection> threadContextConnetion = new ThreadLocal<Connection>();

    protected DBPooledManager pooledManager;

    /**
     * Instantiates a new Peresist data event listener.
     *
     * @param dispatchEventContainer the dispatch event container
     */
    public PeresistDataEventListener(DispatchEventContainer dispatchEventContainer){
        super(dispatchEventContainer);
        pooledManager = DBPooledManager.getInstance();
    }

    protected Connection getTheadConection(String dbname) throws SQLException {
        Connection connection = threadContextConnetion.get();

        if(connection == null){
            connection =  pooledManager.getConnectionByPoolName(dbname);
            threadContextConnetion.set(connection);
        }

        return  connection;
    }

    protected StatementMapHelp getThreadStatementParam(){
        StatementMapHelp statementMapHelp = threadContextStatement.get();
        //存入线程上下文
        if(statementMapHelp == null){
            statementMapHelp = new StatementMapHelp();
            threadContextStatement.set(statementMapHelp);
        }
        return statementMapHelp;
    }

    protected StatiInfoBean getThreadStatiInfoParam(ConfTaskBean confTaskBean){
        StatiInfoBean statiInfoBean = threadContextStatiInfo.get();

        if(statiInfoBean == null){
            statiInfoBean = new StatiInfoBean(confTaskBean);
            threadContextStatiInfo.set(statiInfoBean);
        }

        return statiInfoBean;
    }

    protected void setThreadStatiInfoParam(StatiInfoBean statiInfoBean){
        threadContextStatiInfo.set(statiInfoBean);
    }

    /**
     * 固化事件完成，记录实例的日志、记录最后同步记录的值
     *
     * @param peresistevent
     */
    protected void finishPeresistEvent(PeresistDataEventBean peresistevent){
        List<ConfTableBean> confTableBeanList = peresistevent.getConfTableBeans();
        ConfTaskBean confTaskBean =  peresistevent.getConfTaskBean();

        try {
            StatementMapHelp statementMapHelp = getThreadStatementParam();
            statementMapHelp.closeAll();
        }catch (Exception e1){
            LOG.warn("释放 StatementMapHelp 对象异常", e1);
        }

        try{
            Connection conn = threadContextConnetion.get();

            if(conn != null) {

                if(!conn.getAutoCommit()){
                    conn.setAutoCommit(true);
                }

                pooledManager.closeConnectionByPoolName(confTableBeanList.get(0).getTargetDbName(), conn);
            }
        }catch (Exception e1){
            LOG.warn("释放 Connection 对象异常", e1);
        }

        try{
            DBSyncConfService confService = SpringManager.getInstance().getBeanByType(DBSyncConfService.class);
            StatiInfoBean statiInfoBean = this.getThreadStatiInfoParam(confTaskBean);
            TaskInstLogBean instLogBean = new TaskInstLogBean();

            //更新配置表的最后更新值以及最后更新的日期
            if(statiInfoBean.getSyncLastValJson() != null) {
                confService.updateTaskConfForLast(statiInfoBean.getSyncLastValJson(), confTaskBean.getTaskId());
            }else{
                confService.updateTaskConfForLast(confTaskBean.getLastValue(),confTaskBean.getTaskId());
            }

            instLogBean.setTaskInstId(peresistevent.getTaskInstId());
            instLogBean.setTaskId(peresistevent.getTaskId());
            instLogBean.setSyncStatus(TaskInstLogBean.SYNC_STATUS_SUCC);
            instLogBean.setSyncLogId(confTaskBean.getSyncInstLogId());
            instLogBean.setAllSize(statiInfoBean.getAllSize());
            instLogBean.setSyncDataSize(statiInfoBean.getStatInfoJson());
            instLogBean.setSyncLastValue(statiInfoBean.getSyncLastValJson());

            if(peresistevent.getErrorMsg()!=null){
                instLogBean.setSyncInfo(peresistevent.getErrorMsg());
                instLogBean.setSyncStatus(TaskInstLogBean.SYNC_STATUS_FAIL);
            }

            if(confTaskBean.getSyncInstLogId()>0){
                confService.updateTaskInstLogById(instLogBean);
            }else {
                confService.insertTaskInstLog(instLogBean);
            }
        }catch (Exception e1){
            LOG.warn("记录任务实例日志对象异常", e1);
        }

        try{
            threadContextStatement.remove();
            threadContextConnetion.remove();
            threadContextStatiInfo.remove();
        }catch (Exception e1){
            LOG.warn("释放线程变量异常", e1);
        }

    }

    /**
     * 按配置清除表数据
     * @param sourConn
     * @param confTableBeanList
     * @param statementMapHelp
     * @throws SQLException
     */
    protected void deleteTable(Connection sourConn, List<ConfTableBean> confTableBeanList,StatementMapHelp statementMapHelp)
            throws SQLException {

        for(int i=0; i < confTableBeanList.size(); i++){
            ConfTableBean confTableBean = confTableBeanList.get(i);
            PreparedStatement prepareStatement = sourConn.prepareStatement(confTableBean.getDeleteSql());
            prepareStatement.executeUpdate();
            statementMapHelp.put(confTableBean, prepareStatement);

            if(LOG.isDebugEnabled()){
                LOG.debug("执行SQL:" + confTableBean.getDeleteSql());
            }

            if(confTableBean.getChildList() != null) {
                deleteTable(sourConn, confTableBean.getChildList(),statementMapHelp);
            }
        }
    }


}
