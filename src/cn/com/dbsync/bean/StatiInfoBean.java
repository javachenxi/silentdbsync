package cn.com.dbsync.bean;


import cn.com.dbsync.util.JsonUtil;

import java.util.*;

/**
 * ���ݿ�ͳ����Ϣ��:�����ͬ����¼��������
 * <p>
 * Created by cxi on 2016/5/9.
 */
public class StatiInfoBean {

    private Map<String, Integer> statMap;
    private List<ConfTaskBean.SyncLastValue> synclist;
    private long allSize;
    private String syncLastValJson;

    /**
     * Instantiates a new Stati info bean.
     */
    public StatiInfoBean(){
        statMap = new HashMap<String, Integer>();
        allSize = 0;
    }

    /**
     * ���� ConfTaskBean ���������ó�ʼ�� synclist
     *
     * @param confTaskBean the conf task bean
     */
    public StatiInfoBean(ConfTaskBean confTaskBean){

        if(confTaskBean.getLastValueList() != null){
            this.synclist = confTaskBean.getLastValueList();
        }

        statMap = new HashMap<String, Integer>();
        allSize = 0;
    }

    /**
     * Get stat map map.
     *
     * @return the map
     */
    public Map<String, Integer> getStatMap(){
        return statMap;
    }

    /**
     * ������ͳ�Ƹ������������Լ�����
     *
     * @param tablename the tablename
     * @param size      the size
     */
    public void incrStatiInfo(String tablename,int size){
        Integer i = statMap.get(tablename);
        if(i == null){
            statMap.put(tablename, size);
        }else {
            statMap.put(tablename, i+size);
        }

        allSize+=size;
    }

    /**
     * Get all size long.
     *
     * @return the long
     */
    public long getAllSize(){
        return allSize;
    }

    /**
     * Get stat info json string.
     *
     * @return the string
     */
    public String getStatInfoJson(){
        if(statMap.isEmpty()){
            return null;
        }

        return JsonUtil.getJsonStringForJavaPOJO(statMap);
    }

    /**
     * Get sync last val json string.
     *
     * @return the string
     */
    public String getSyncLastValJson(){
        if(synclist == null){
            return null;
        }

        return JsonUtil.getJsonStringForList(synclist);
    }

    /**
     * ������������������µ����ͬ��ֵ
     *
     * @param tableName the table name
     * @param colName   the col name
     * @param type      the type
     * @param value     the value
     */
    public void addSyncLastValue(String tableName,String colName,int type,Object value){

        if(synclist == null){
            synclist = new ArrayList<ConfTaskBean.SyncLastValue>();
        }

        ConfTaskBean.SyncLastValue tmpSync = null;
        boolean isExist = false;

        for(int i=0; i<synclist.size(); i++){
            tmpSync = synclist.get(i);

            if(tmpSync.getTableName().equals(tableName)&&tmpSync.getColName().equals(colName)){
                tmpSync.setColType(type);

                if(value!=null && (value instanceof Date)){
                    tmpSync.setLastVaule(((Date)value).getTime());
                }else {
                    tmpSync.setLastVaule(value);
                }
                isExist = true;
                break;
            }
        }

        if(!isExist){
            tmpSync = new ConfTaskBean.SyncLastValue();
            tmpSync.setColName(colName);
            tmpSync.setTableName(tableName);
            tmpSync.setColType(type);

            if(value!=null && (value instanceof Date)){
                tmpSync.setLastVaule(((Date)value).getTime());
            }else {
                tmpSync.setLastVaule(value);
            }
            synclist.add(tmpSync);
        }



    }

    /**
     * ǳ��¡������
     *
     * @return
     */
    public StatiInfoBean clone(){
        StatiInfoBean statiInfoBean = new StatiInfoBean();
        statiInfoBean.statMap = new HashMap<String, Integer>();
        statiInfoBean.statMap.putAll(this.statMap);
        statiInfoBean.allSize = this.allSize;
        statiInfoBean.syncLastValJson = this.syncLastValJson;

        if(this.synclist != null){
            statiInfoBean.synclist = new ArrayList<ConfTaskBean.SyncLastValue>(this.synclist);
        }

        return statiInfoBean;
    }
}
