package cn.com.dbsync.dao;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017-09-30.
 */
public class DataRow implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8057456074988617778L;
    private DAOResultMetaData resultMetaData;//字段元数据
    private java.util.ArrayList dataList;//当前行的数据
    private int rowId;//当前行号(从0开始计算)
    public DataRow(DAOResultMetaData resultMetaData, ArrayList dataList, int rowId) {
        this.resultMetaData = resultMetaData;
        this.dataList = dataList;
        this.rowId = rowId;
    }
    public DAOResultMetaData getResultMetaData() {
        return resultMetaData;
    }
    public void setResultMetaData(DAOResultMetaData resultMetaData) {
        this.resultMetaData = resultMetaData;
    }
    public java.util.ArrayList getDataList() {
        return dataList;
    }
    public void setDataList(java.util.ArrayList dataList) {
        this.dataList = dataList;
    }
    public int getRowId() {
        return rowId;
    }
    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    /**
     * 获取列colIndex的字段元数据信息ColumnMetaData
     * @param colIndex 列序号(从0开始计算)
     * @return
     */
    public ColumnMetaData getColumnMetaData(int colIndex){
        return resultMetaData.getColumnMetaData(colIndex);
    }

    /**
     * 获取列colIndex的字段名
     * @param colIndex 列序号(从0开始计算)
     * @return
     */
    public String getFieldName(int colIndex){
        return getColumnMetaData(colIndex).getName();
    }

    /**
     * 获取fieldName对应的列序号
     * @param fieldName
     * @return
     */
    public int getFieldIndex(String fieldName){
        return resultMetaData.getColumnIndex(fieldName);
    }

    /**
     * 根据字段名获取对应的值
     * @param fieldName 字段名
     * @return
     */
    public String getFieldValueByName(String fieldName){
        return getFieldValueByIndex(getFieldIndex(fieldName));
    }

    /**
     * 根据字段列序号取对应的值
     * @param index 列序号
     * @return
     */
    public String getFieldValueByIndex(int index){
        if (index==-1) {
            return "未定义的字段名,请检查!";
        }else{
            return String.valueOf(dataList.get(index));
        }
    }

    /**
     * 根据Blob字段名获取对应的值
     * @param fieldName 字段名
     * @return
     */
    public byte[] getBlobFieldValueByName(String fieldName){
        return gettBlobFieldValueByIndex(getFieldIndex(fieldName));
    }

    /**
     * 根据Blob字段列序号取对应的值
     * @param index 列序号
     * @return
     */
    public byte[] gettBlobFieldValueByIndex(int index){
        if (index==-1) {
            return "未定义的字段名,请检查!".getBytes();
        }else{
            Object obj = dataList.get(index);
            if (obj instanceof byte[]) {
                return (byte[]) obj;
            }else{
                //说明是空
                return null;
            }
        }
    }

    public String toString(){
        return "dataRow[rowId::"+rowId+",dataList::"+dataList+"]";
    }


}
