package cn.com.dbsync.schedule;

import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.bean.TaskInstBean;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.service.DBSyncInstService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Administrator on 2017-11-01.
 */
public class GenerateDBSyncTaskRunner extends TaskRunner{

    private final static Log LOG = LogFactory.getLog(GenerateDBSyncTaskRunner.class.getName());

    private static final int IDLE_TIME_MAX = 10;

    private DBSyncConfService dbSyncConfService = null;
    private DBSyncInstService dbSyncInstService = null;

    private int[] waittimes = {1000,3000,5000,7000,11000};
    private Object lock = new Object();

    public GenerateDBSyncTaskRunner(TaskInstBean taskInstBean, ScheduleContainer scheduleContainer){
        super(taskInstBean, scheduleContainer);
    }

    @Override
    public void proccess() {

        dbSyncConfService = scheduleContainer.getDbSyncConfService();
        dbSyncInstService = scheduleContainer.getDbSyncInstService();
        int counter = 0;

        while (!this.stopped) {

            try {
                List<ConfTaskBean> confTaskBeanList = dbSyncConfService.getConfTaskList();

                //没有任务或没有需要生成任务实例的任务时，进入休眠状态
                if (confTaskBeanList == null || confTaskBeanList.isEmpty() || !genTaskDBSyncTaskInst(confTaskBeanList)) {
                    this.waiting(waittimes[counter % waittimes.length]);
                    //超过空闲的次数时，跳出执行循环
                    if(counter > IDLE_TIME_MAX){
                       break;
                    }
                    counter++;
                    continue;
                }

                counter = 0;

            } catch (Exception e) {
               LOG.error("生成DBSync实例线程异常", e);
            }
        }
    }

    /**
     *
     * 后置处理，退出生成线程时，需要将任务修改为未处理
     *
     */
    public void postProccess(){
        scheduleContainer.switchTaskInstStatus(taskInstBean.getTaskInstId(),
                  TaskInstBean.Status.HANDLING.value,
                 TaskInstBean.Status.UNHANDLE.value);
    }

    /**
     * 进入等待
     * @param timeout
     */
    private void waiting(long timeout) throws InterruptedException {
        synchronized (lock) {
            lock.wait(timeout);
        }
    }

    private boolean genTaskDBSyncTaskInst(List<ConfTaskBean> confTaskBeanList ) {

        boolean isHaveGenTaskInst = false;

        ConfTaskBean tmpConfBean = null;
        Date dateStr = null;
        Calendar currCalendar = dbSyncConfService.getDBCurrDate();

        TaskInstBean tmpTaskInst = new TaskInstBean();

        int[] statusArray = {TaskInstBean.Status.UNHANDLE.value, TaskInstBean.Status.HANDLING.value};

        for(int i=0; i<confTaskBeanList.size(); i++){
            tmpConfBean = confTaskBeanList.get(i);

            //查询任务实例成功并且已有实例，则不生成新实例
            if(dbSyncInstService.isHaveTaskInst(tmpConfBean.getTaskId(),statusArray)){
                continue;
            }

            dateStr = this.calcNextplanTime(tmpConfBean, currCalendar);
            tmpTaskInst.setTaskType(tmpConfBean.getTaskType());
            tmpTaskInst.setTaskId(tmpConfBean.getTaskId());
            tmpTaskInst.setStatus(TaskInstBean.Status.UNHANDLE.value);
            tmpTaskInst.setTaskName(tmpConfBean.getTaskName());
            tmpTaskInst.setPlanTime(dateStr);
            dbSyncInstService.insertIaskInstBean(tmpTaskInst);

            LOG.info("生成实例："+ tmpTaskInst.toString());

            //任务实例的计数器
            dbSyncConfService.updateTaskConfForCount(tmpConfBean.getInstCount()+1,tmpConfBean.getTaskId());
            isHaveGenTaskInst = true;
        }

        return isHaveGenTaskInst;
    }

    /**
     * 计算下一周期的任务
     *
     * @param tmpConfBean
     * @param currdate
     * @return
     */
    private Date calcNextplanTime(ConfTaskBean tmpConfBean, Calendar currdate) {

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
                    long nexttimetmp = (tmpConfBean.getSyncCycle()-9) * 1000;
                    if (lasttime + nexttimetmp < currdate.getTimeInMillis()) {
                        nextdate.setTimeInMillis(currdate.getTimeInMillis());
                    } else {
                        nextdate.setTimeInMillis(lasttime + nexttimetmp);
                    }
                } else {
                    LOG.warn("未知的任务周期类型 SyncCycle=" + tmpConfBean.getSyncCycle());
                    return null;
                }
        }

        return nextdate.getTime();
    }
}
