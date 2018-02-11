package cn.com.dbsync.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Administrator on 2017-09-30.
 */
public class DataSet  implements Iterator, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -3555474306512799652L;
    private DAOResultMetaData resultMetaData;//�ֶ�Ԫ����
    private java.util.ArrayList resultList;//�ֶν������
    private int totalRowNum;//�ܹ���¼��
    private int returnRowNum;//��ǰ���صļ�¼��
    private int currentRow;//��ǰ��

    public DataSet(DAOResultMetaData resultMetaData, ArrayList resultList, int totalRowNum) {
        this.resultMetaData = resultMetaData;
        this.resultList = resultList;
        this.totalRowNum = totalRowNum;
        if (resultList!=null) {
            this.returnRowNum = resultList.size();
        }else{
            this.returnRowNum = 0;
        }
        this.currentRow = -1;//�տ�ʼ,��ǰ������Ϊ-1
    }

    /**
     * �Ƿ�����һ����¼
     * @return
     */
    public boolean hasNext() {
        return (returnRowNum>0)&&(currentRow<returnRowNum-1);
    }

    /**
     * ��ȡ��һ����¼
     * @return
     */
    public Object next() {
        currentRow++;
        return getDataRow(currentRow);
    }

    /**
     * ��ȡ��rowId�е�DataRow����
     * @param rowId
     */
    public DataRow getDataRow(int rowId){
        return new DataRow(resultMetaData,(ArrayList)resultList.get(rowId),rowId);
    }

    /**
     * �ָ�����ʼ״̬
     */
    public void reset(){
        this.currentRow = -1;//�տ�ʼ,��ǰ������Ϊ-1
    }

    /**
     * ���ص�resultList�Ƿ�Ϊ��
     * @return
     */
    public boolean isEmpty(){
        return (returnRowNum==0);
    }

    public void remove() {
        /**@todo Implement this java.util.Iterator method*/
        throw new java.lang.UnsupportedOperationException(
                "Method remove() not yet implemented.");
    }
    public DAOResultMetaData getResultMetaData() {
        return resultMetaData;
    }
    public void setResultMetaData(DAOResultMetaData resultMetaData) {
        this.resultMetaData = resultMetaData;
    }
    public java.util.ArrayList getResultList() {
        return resultList;
    }
    public void setResultList(java.util.ArrayList resultList) {
        this.resultList = resultList;
    }
    public int getTotalRowNum() {
        return totalRowNum;
    }
    public void setTotalRowNum(int totalRowNum) {
        this.totalRowNum = totalRowNum;
    }
    public int getReturnRowNum() {
        return returnRowNum;
    }
    public void setReturnRowNum(int returnRowNum) {
        this.returnRowNum = returnRowNum;
    }
    public int getCurrentRow() {
        return currentRow;
    }


    public String toString(){
        return "totalRowNum::"+totalRowNum+"\r\nDAOResultMetaData::"+resultMetaData+"]\r\nresultList::"+resultList;
    }

}
