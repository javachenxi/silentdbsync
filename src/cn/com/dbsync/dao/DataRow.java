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
    private DAOResultMetaData resultMetaData;//�ֶ�Ԫ����
    private java.util.ArrayList dataList;//��ǰ�е�����
    private int rowId;//��ǰ�к�(��0��ʼ����)
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
     * ��ȡ��colIndex���ֶ�Ԫ������ϢColumnMetaData
     * @param colIndex �����(��0��ʼ����)
     * @return
     */
    public ColumnMetaData getColumnMetaData(int colIndex){
        return resultMetaData.getColumnMetaData(colIndex);
    }

    /**
     * ��ȡ��colIndex���ֶ���
     * @param colIndex �����(��0��ʼ����)
     * @return
     */
    public String getFieldName(int colIndex){
        return getColumnMetaData(colIndex).getName();
    }

    /**
     * ��ȡfieldName��Ӧ�������
     * @param fieldName
     * @return
     */
    public int getFieldIndex(String fieldName){
        return resultMetaData.getColumnIndex(fieldName);
    }

    /**
     * �����ֶ�����ȡ��Ӧ��ֵ
     * @param fieldName �ֶ���
     * @return
     */
    public String getFieldValueByName(String fieldName){
        return getFieldValueByIndex(getFieldIndex(fieldName));
    }

    /**
     * �����ֶ������ȡ��Ӧ��ֵ
     * @param index �����
     * @return
     */
    public String getFieldValueByIndex(int index){
        if (index==-1) {
            return "δ������ֶ���,����!";
        }else{
            return String.valueOf(dataList.get(index));
        }
    }

    /**
     * ����Blob�ֶ�����ȡ��Ӧ��ֵ
     * @param fieldName �ֶ���
     * @return
     */
    public byte[] getBlobFieldValueByName(String fieldName){
        return gettBlobFieldValueByIndex(getFieldIndex(fieldName));
    }

    /**
     * ����Blob�ֶ������ȡ��Ӧ��ֵ
     * @param index �����
     * @return
     */
    public byte[] gettBlobFieldValueByIndex(int index){
        if (index==-1) {
            return "δ������ֶ���,����!".getBytes();
        }else{
            Object obj = dataList.get(index);
            if (obj instanceof byte[]) {
                return (byte[]) obj;
            }else{
                //˵���ǿ�
                return null;
            }
        }
    }

    public String toString(){
        return "dataRow[rowId::"+rowId+",dataList::"+dataList+"]";
    }


}
