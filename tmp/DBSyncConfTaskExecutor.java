package cn.com.dbsync.executor;

import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.dao.DBSyncConfDao;
import cn.com.dbsync.dao.DBSyncManageInstanceDao;
import cn.com.dbsync.util.DBSyncConstant;
import cn.com.dbsync.util.DBUtil;
import cn.com.servyou.components.task.executor.BaseTaskExecutor;
import cn.com.servyou.components.task.object.InstanceBean;
import cn.com.servyou.tdap.common.BeanUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * <p>
 * Title: DBSyncConfTaskExecutor
 * </p>
 * <p>
 * Description: 扫描任务同步配置表，生成具体的同步任务实例
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009-2016
 * </p>
 * <p>
 * Company: servyou
 * </p>
 *
 * @author cxi
 * @version 1.0
 * @created 2016 /5/14
 */
public class DBSyncConfTaskExecutor extends BaseTaskExecutor {

    /**
     * 遍历同步任务模板表 pt_dbsync_conftask
     */
    protected void runTask() {
        DBSyncConfDao confDao = new DBSyncConfDao();
        DBSyncManageInstanceDao mngInstDao = new DBSyncManageInstanceDao();

        List<ConfTaskBean> confTaskBeanList = confDao.getConfTaskList();

        if(confTaskBeanList == null || confTaskBeanList.isEmpty()){
            return;
        }

        InstanceBean instanceBean = null;

        try {
            instanceBean = (InstanceBean)BeanUtils.cloneBean(this.getInsBean());
        } catch (Exception e) {
           log.error("复制实例对象失败！",e);
        }

        ConfTaskBean tmpConfBean = null;
        String dateStr = null;
        Calendar currCalendar = mngInstDao.getDBCurrDate();

        for(int i=0; i<confTaskBeanList.size(); i++){
            tmpConfBean = confTaskBeanList.get(i);
            //查询任务实例成功并且已有实例，则不生成新实例
            if(mngInstDao.getInstanceByModid(tmpConfBean.getTaskId()+"")){
                continue;
            }

            dateStr = this.calcNextplanTime(tmpConfBean, currCalendar);
            instanceBean.setTaskName(tmpConfBean.getTaskName());
            instanceBean.setModId(tmpConfBean.getTaskId()+"");
            instanceBean.setPlanTime(dateStr);
            instanceBean.setTaskProp(DBSyncConstant.TASKINST_EXECUTOR_KEY);
            mngInstDao.insertInstance(instanceBean);
            //任务实例的计数器
            confDao.updateTaskConfForCount(tmpConfBean.getInstCount()+1,tmpConfBean.getTaskId());
        }

    }

    /**
     * 计算下一周期的任务
     *
     * @param tmpConfBean
     * @param currdate
     * @return
     */
    private String calcNextplanTime(ConfTaskBean tmpConfBean, Calendar currdate) {

        Date lastdate = tmpConfBean.getSyncLasttime();
        int lastyear = 0;
        int lastmonth = 0;
        int lastweek = 0;
        int lastday = 0;
        long lasttime = 0;

        if (lastdate != null) {
            Calendar lastCalendar = Calendar.getInstance();
            lastCalendar.setTime(lastdate);
            lastyear = lastCalendar.get(Calendar.YEAR);
            lastmonth = lastCalendar.get(Calendar.MONTH);
            lastweek = lastCalendar.get(Calendar.WEEK_OF_YEAR);
            lastday = lastCalendar.get(Calendar.DAY_OF_YEAR);
            lasttime = lastCalendar.getTimeInMillis();
        }

        Calendar nextdate = null;

        switch (tmpConfBean.getSyncCycle()) {
            case ConfTaskBean.SYNC_CYCLE_YEAR:
                if (lastyear < currdate.get(Calendar.YEAR)) {
                    nextdate = new GregorianCalendar(currdate.get(Calendar.YEAR), 0, 1);
                } else {
                    nextdate = new GregorianCalendar(lastyear + 1, 0, 1);
                }
                break;
            case ConfTaskBean.SYNC_CYCLE_MONTH:
                if (lastyear < currdate.get(Calendar.YEAR) || lastmonth < currdate.get(Calendar.MONTH)) {
                    nextdate = new GregorianCalendar(currdate.get(Calendar.YEAR), currdate.get(Calendar.MONTH), 1);
                } else {
                    nextdate = new GregorianCalendar(currdate.get(Calendar.YEAR), lastmonth + 1, 1);
                }
                break;
            case ConfTaskBean.SYNC_CYCLE_WEEK:
                if (lastyear < currdate.get(Calendar.YEAR) || lastweek < currdate.get(Calendar.WEEK_OF_YEAR)) {
                    nextdate = new GregorianCalendar(currdate.get(Calendar.YEAR), currdate.get(Calendar.MONTH), currdate.get(Calendar.DAY_OF_MONTH));
                } else {
                    nextdate = new GregorianCalendar(currdate.get(Calendar.YEAR), currdate.get(Calendar.MONTH), 1);
                    nextdate.clear(Calendar.MONTH);
                    nextdate.clear(Calendar.DAY_OF_MONTH);
                    nextdate.set(Calendar.WEEK_OF_YEAR, lastweek + 1);
                }
                break;
            case ConfTaskBean.SYNC_CYCLE_DAY:
                if (lastyear < currdate.get(Calendar.YEAR) || lastday < currdate.get(Calendar.DAY_OF_YEAR)) {
                    nextdate = new GregorianCalendar(currdate.get(Calendar.YEAR), currdate.get(Calendar.MONTH), currdate.get(Calendar.DAY_OF_MONTH));
                } else {
                    nextdate = new GregorianCalendar(currdate.get(Calendar.YEAR), currdate.get(Calendar.MONTH), 1);
                    nextdate.clear(Calendar.MONTH);
                    nextdate.clear(Calendar.DAY_OF_MONTH);
                    nextdate.set(Calendar.DAY_OF_YEAR, lastday + 1);
                }
                break;

            default:
                if (tmpConfBean.getSyncCycle() >= 10) {
                    nextdate = new GregorianCalendar();
                    if (lasttime + tmpConfBean.getSyncCycle() * 1000 < currdate.getTimeInMillis()) {
                        nextdate.setTimeInMillis(currdate.getTimeInMillis());
                    } else {
                        nextdate.setTimeInMillis(lasttime + tmpConfBean.getSyncCycle() * 1000);
                    }
                } else {
                    log.warn("未知的任务周期类型 SyncCycle=" + tmpConfBean.getSyncCycle());
                    return null;
                }
        }

        return DBUtil.formDateToNormalStr(nextdate.getTime());
    }

    /**
     *
     * @param error
     * @param success
     * 成功标志
     * @return
     */
    protected String afterExe(String error, boolean success) {
        String relStr = super.afterExe(error, success);
        DBSyncManageInstanceDao mngInstDao = new DBSyncManageInstanceDao();
        mngInstDao.resetRunByInstanceId(this.getTaskId());

        return relStr;
    }


}






