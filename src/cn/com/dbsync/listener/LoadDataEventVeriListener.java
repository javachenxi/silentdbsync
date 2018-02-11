package cn.com.dbsync.listener;

import cn.com.dbsync.bean.ConfPoolBean;
import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.bean.PackResultBean;
import cn.com.dbsync.core.DBSyncException;
import cn.com.dbsync.core.DispatchEventContainer;
import cn.com.dbsync.core.RowDataMediator;
import cn.com.dbsync.dao.*;
import cn.com.dbsync.util.CommUtil;
import cn.com.dbsync.util.DBSyncConstant;
import cn.com.dbsync.util.StringConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-01-22.
 */
public class LoadDataEventVeriListener extends EventListenerAdapter{

    private final static Log LOG = LogFactory.getLog(LoadDataEventListener.class.getName());

    private static final int COMMIT_RECORD_MAX = 1000;
    private DBPooledManager pooledManager;

    public LoadDataEventVeriListener(DispatchEventContainer dispatchEventContainer){
        super(dispatchEventContainer);
        pooledManager = DBPooledManager.getInstance();
    }



    /**
     * У�������¼�
     *
     * @param event
     */
    public void load(EventSourceBean event) {
        LoadDataEventBean loadevent = (LoadDataEventBean)event;
        List<ConfTableBean> confTableBeanList = loadevent.getConfTableBeans();
        ConfTaskBean confTaskBean = event.getConfTaskBean();
        ConfTableBean tmpConftable = confTableBeanList.get(0);

        PeresistDataEventBean perDateEvent = null;

        ConfPoolBean srcPoolBean = null;
        ConfPoolBean tagPoolBean = null;

        Connection sourConn = null;
        Connection tagConn = null;

        DBDialect dbDialect = null;
        DBDialect tagDBDialect = null;

        //�������ݰ������
        int packSeqNumber = 0;
        boolean isException = false;
        PackResultBean packResultBean = null;

        try{
            sourConn = pooledManager.getConnectionByPoolName(tmpConftable.getSourceDbName());
            srcPoolBean = pooledManager.getConfPoolBean(tmpConftable.getSourceDbName());

            dbDialect = DBDialectFactory.createDBDialect(Enum.valueOf(DBSyncConstant.DBType.class, srcPoolBean.getDbType()),
                    srcPoolBean.getAppCharset(), srcPoolBean.getDbCharset());

            tagConn = pooledManager.getConnectionByPoolName(tmpConftable.getTargetDbName());
            tagPoolBean = pooledManager.getConfPoolBean(tmpConftable.getTargetDbName());

            tagDBDialect = DBDialectFactory.createDBDialect(Enum.valueOf(DBSyncConstant.DBType.class, tagPoolBean.getDbType()),
                    tagPoolBean.getAppCharset(), tagPoolBean.getDbCharset());

            PreparedStatement prepareStatement = null;
            PreparedStatement tagprepareStatement = null;

            ResultSet resultSet = null;
            ResultSet tagResultSet = null;

            DAOResult daoResult = null;
            DAOResult tagdaoResult = null;

            RowDataMediator rowDataMediator = null;

            packResultBean = new PackResultBean();

            for(int i=0; i<confTableBeanList.size(); i++){
                prepareStatement = null;
                tagprepareStatement = null;

                daoResult = new DAOResult();
                tagdaoResult = new DAOResult();

                tmpConftable = confTableBeanList.get(i);
                rowDataMediator = new RowDataMediator(tmpConftable);

                prepareStatement = sourConn.prepareStatement(tmpConftable.getSrcSelectByOrderSql());
                tagprepareStatement = tagConn.prepareStatement(tmpConftable.getTagSelectByOrderSql());

                //ִ�в�ѯ����
                prepareStatement.setFetchDirection(ResultSet.FETCH_FORWARD);
                tagprepareStatement.setFetchSize(ResultSet.FETCH_FORWARD);
                resultSet = prepareStatement.executeQuery();
                tagResultSet = tagprepareStatement.executeQuery();

                ResultSetMetaData rsmd = resultSet.getMetaData();
                ResultSetMetaData tagrsmd = tagResultSet.getMetaData();

                DAOResultMetaData currResultMetaData = dbDialect.transResultMetaData(rsmd);
                daoResult.appendDAOResultMetaData(currResultMetaData);

                DAOResultMetaData tagcurrResultMetaData = tagDBDialect.transResultMetaData(tagrsmd);
                tagdaoResult.appendDAOResultMetaData(tagcurrResultMetaData);

                int colCount = rsmd.getColumnCount();
                ArrayList resData = new ArrayList(COMMIT_RECORD_MAX+1);
                ArrayList<Integer> optToken = new ArrayList<Integer>(COMMIT_RECORD_MAX+1);
                ArrayList rowdata = new ArrayList(colCount +1);
                ArrayList tagrowdata = new ArrayList(colCount +1);

                boolean isLast = false;

                RowDataMediator.CompareType compareType = null;

                //�������ҷ�������һ����¼������״̬��STARTED��
                while (true) {

                    if(rowdata.isEmpty() && resultSet.next()) {

                        for (int r = 0; r < colCount; r++) {
                            rowdata.add(dbDialect.castResultSetToJavaType(rsmd.getColumnType(r + 1), resultSet, r + 1));
                        }

                    }else if(rowdata.isEmpty()){

                        if(!tagrowdata.isEmpty()){
                            addCopy(resData, optToken, tagrowdata, PackResultBean.OPT_TOKEN_D);
                        }

                        break;
                    }

                    //Դ�������ݣ�Ŀ���û�б������
                    if(!isLast&&!rowdata.isEmpty()){

                        if(tagrowdata.isEmpty() && tagResultSet.next()) {
                            for (int t = 0; t < colCount; t++) {
                                tagrowdata.add(tagDBDialect.castResultSetToJavaType(tagrsmd.getColumnType(t + 1), tagResultSet, t + 1));
                            }
                        }else if(tagrowdata.isEmpty()){
                            addCopy(resData, optToken, rowdata, PackResultBean.OPT_TOKEN_I);
                            isLast = true;
                        }

                        if(!isLast){

                             compareType = rowDataMediator.compareToData(rowdata, tagrowdata);

                             switch (compareType) {
                                 case EQ:
                                     rowdata.clear();
                                     tagrowdata.clear();
                                     break;
                                 case EQCH:
                                     addCopy(resData, optToken, rowdata, PackResultBean.OPT_TOKEN_U);
                                     tagrowdata.clear();
                                     break;
                                 case GR:
                                     addCopy(resData, optToken, tagrowdata, PackResultBean.OPT_TOKEN_D);
                                     break;
                                 case LE:
                                     addCopy(resData, optToken, rowdata, PackResultBean.OPT_TOKEN_I);
                                     break;
                             }

                        }
                    }else{
                        addCopy(resData, optToken, rowdata, PackResultBean.OPT_TOKEN_I);
                    }

                    //�ж��Ƿ�ﵽ����ύ��¼1000
                    if(packResultBean.getSize()+resData.size()>COMMIT_RECORD_MAX){
                        packSeqNumber ++;
                        daoResult.appendResult(resData.clone());
                        packResultBean.addDataEntity(tmpConftable, daoResult, CommUtil.transformListInt(optToken));
                        publicPeresistEvent(packResultBean, packSeqNumber, false, loadevent);
                        optToken.clear();
                        resData.clear();
                        daoResult = new DAOResult();
                        daoResult.appendDAOResultMetaData(currResultMetaData);
                        packResultBean = new PackResultBean();
                    }

                    if(!this.getStatus(loadevent)){
                        DBSyncException stopException = new DBSyncException("ǿ��ֹͣͬ������");
                        stopException.setEventType(EventSourceBean.EventType.STOP);
                        throw stopException;
                    }
                }

                //����Ŀ�������Դ
                while(tagResultSet.next()){
                    for (int t = 0; t < colCount; t++) {
                        tagrowdata.add(tagDBDialect.castResultSetToJavaType(tagrsmd.getColumnType(t + 1), tagResultSet, t + 1));
                    }

                    addCopy(resData, optToken, tagrowdata, PackResultBean.OPT_TOKEN_D);

                    //�ж��Ƿ�ﵽ����ύ��¼1000
                    if(packResultBean.getSize()+resData.size()>COMMIT_RECORD_MAX){
                        packSeqNumber ++;
                        daoResult.appendResult(resData.clone());
                        packResultBean.addDataEntity(tmpConftable, daoResult, CommUtil.transformListInt(optToken));
                        publicPeresistEvent(packResultBean, packSeqNumber, false, loadevent);
                        optToken.clear();
                        resData.clear();
                        daoResult = new DAOResult();
                        daoResult.appendDAOResultMetaData(currResultMetaData);
                        packResultBean = new PackResultBean();
                    }

                    if(!this.getStatus(loadevent)){
                        DBSyncException stopException = new DBSyncException("ǿ��ֹͣͬ������");
                        stopException.setEventType(EventSourceBean.EventType.STOP);
                        throw stopException;
                    }
                }

                if(resData.size()>0){
                    daoResult.appendResult(resData);
                    //������ݼ�
                    packResultBean.addDataEntity(tmpConftable, daoResult, CommUtil.transformListInt(optToken));
                }

                //�ж��Ƿ�ﵽ����ύ��¼1000,����״̬��STOPED��
                if(packResultBean.getSize()>COMMIT_RECORD_MAX){
                    packSeqNumber ++;
                    //packResultBean.addDataEntity(tmpConftable, daoResult);
                    publicPeresistEvent(packResultBean, packSeqNumber, false, loadevent);
                    packResultBean = new PackResultBean();
                }

                if(resultSet!= null){
                    resultSet.close();
                    resultSet = null;
                }

                if(tagResultSet!= null){
                    tagResultSet.close();
                    tagResultSet = null;
                }
            }
        }catch (Exception e){
            isException = true;
            LOG.error("��ѯ�����쳣 [TaskInstId]=" + loadevent.getTaskInstId(), e);
            loadevent.setErrorMsg("����ͬ���쳣�˳���ErrorMsg=" + e.getMessage());
            try {
                //����Ǵ���PeresistEvent�쳣ʱ�����ٷ���PeresistEvent�¼�
                if (e instanceof DBSyncException) {
                    DBSyncException dbSyncException = (DBSyncException) e;
                    if (!EventSourceBean.EventType.PERESIST.equals(dbSyncException.getEventType())) {
                        packSeqNumber ++;
                        publicPeresistEvent(null, packSeqNumber, true, loadevent);
                    }
                } else {
                    packSeqNumber ++;
                    publicPeresistEvent(null, packSeqNumber, true, loadevent);
                }
            } catch (DBSyncException dbe) {
                LOG.error("������������������ݰ�����ʧ�� [TaskInstId]=" + loadevent.getTaskInstId(), dbe);
            }

            if(srcPoolBean !=null && e instanceof SQLException){
                LOG.error(StringConverter.convertString(e.getMessage(), srcPoolBean.getDbCharset(), srcPoolBean.getAppCharset()));
            }
        } finally {

            if(sourConn != null){
                try {
                    pooledManager.closeConnectionByPoolName(tmpConftable.getSourceDbName(), sourConn);
                } catch (RuntimeException e) {
                    LOG.error("�ر�Connection [TaskInstId]="+loadevent.getTaskInstId() +" [DbName]=" + tmpConftable.getSourceDbName(),e);
                }
            }

            if(tagConn != null){
                try {
                    pooledManager.closeConnectionByPoolName(tmpConftable.getTargetDbName(), tagConn);
                } catch (RuntimeException e) {
                    LOG.error("�ر�Connection [TaskInstId]="+loadevent.getTaskInstId()+" [DbName]=" + tmpConftable.getTargetDbName(),e);
                }
            }

            if(!isException) {
                try {
                    packSeqNumber ++;
                    publicPeresistEvent(packResultBean, packSeqNumber, true, loadevent);
                }catch (DBSyncException dbe){
                    LOG.error("������������������ݰ�����ʧ�� [TaskInstId]="+loadevent.getTaskInstId(),dbe);
                }
            }

            publicStopEvent(event);
        }
    }

    private void addCopy(ArrayList resData, ArrayList<Integer> optToken, ArrayList rowData, int opt){
        resData.add(new ArrayList(rowData));
        optToken.add(opt);
        rowData.clear();
    }

    private void publicStopEvent(EventSourceBean event){
        StopEventBean stopEvent = new StopEventBean(event);
        stopEvent.setSync(true);

        //�ⲿ�����ֹͣ�¼������¼�״̬�ó� �˹�����
        if(!this.getStatus(event)){
            stopEvent.setLaunchType(EventSourceBean.LaunchType.MANUL);
        }

        dispatchEventContainer.publicEvent(stopEvent);
    }

    private void publicPeresistEvent(PackResultBean packResultBean,int index, boolean isLast,LoadDataEventBean event){
        PeresistDataEventBean perDateEvent = new PeresistDataEventBean(event);
        perDateEvent.setSync(true);
        perDateEvent.setPackResultBean(packResultBean);
        perDateEvent.setConfTableBeans(event.getConfTableBeans());
        perDateEvent.setSeqNumber(index);
        perDateEvent.setLast(isLast);
        perDateEvent.setErrorMsg(event.getErrorMsg());
        //ֱ�ӷַ��¼���ֱ�������ݱ���
        dispatchEventContainer.dispatchEvent(perDateEvent);
    }


}
