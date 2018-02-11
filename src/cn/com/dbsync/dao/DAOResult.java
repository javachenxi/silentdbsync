package cn.com.dbsync.dao;

import cn.com.dbsync.util.DAOResultUtil;
import cn.com.dbsync.util.JavaBeanClass;
import cn.com.dbsync.util.StringConverter;
import net.sf.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-09-30.
 */
public class DAOResult  implements java.io.Serializable{

    private static final long serialVersionUID = 5490707593332069146L;
    private boolean success = true; //�ɹ����(Ĭ��Ϊtrue)
    private StringBuffer error = new StringBuffer(""); //����������Ϣ
    private ArrayList resultList = new ArrayList();//��������(�ڲ����ɶ��ArrayList(ÿ��Ԫ������һ��ArrayList)���)
    private Object resultValue;
    private ArrayList dAOResultMetaDataList = new ArrayList();//�е�Ԫ���� �ڲ���SQL��ӦԪ��ΪDAOResultMetaData����
    private static final Log logMgr = LogFactory.getLog(DAOResult.class);

    public DAOResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error.toString();
    }

    public void appendError(String msg) {
        this.error.append(msg + "\r\n");
    }

    public void clear() {
        error.delete(0, error.length());
        if (resultList != null) {
            resultList.clear();
        }
    }

    public void appendResult(Object obj) {
        resultList.add(obj);
    }


    /**
     * ��ȡ���صĽ����,���Ƽ�ʹ�ø÷�����
     * @return ArrayList
     * @deprecated
     */
    public ArrayList getResultList() {
        return resultList;
    }

    public Object getResultValue() {
        return resultValue;
    }

    public void setResultValue(Object resultValue) {
        this.resultValue = resultValue;
    }





//����Ϊ���ӵĿ���ȡֵ���� 200312301138����

    /**
     * ��ȡĳ��SQL��ResultValueֵ
     * @param index
     * @return
     */
    public Object getSqlResultValue(int index) {
        return ((ArrayList)this.getResultValue()).get(index);
    }
    /**
     * ��ȡĳ��SQL��ResultValueֵ
     *
     * @return
     */
    public Object getFirstSqlResultValue() {
        return this.getSqlResultValue(0);
    }



    /**
     * ��ȡĳ��SQL�Ķ�ά�����
     * @param index SQL����INDEX ��0��ʼ
     * @return
     */
    public ArrayList getSqlResultList(int index){
        return (ArrayList)this.getResultList().get(index);
    }
    /**
     * ��ȡ��һ��SQL�Ķ�ά�����
     * @return
     */
    public ArrayList getFirstSqlResultList(){
        return this.getSqlResultList(0);
    }


    /**
     * ��ȡĳ��SQL��ĳ�дν����
     * @param index SQL����INDEX ��0��ʼ
     * @param row �д�
     * @return
     */
    public ArrayList getSqlResultRow(int index,int row){
        return (ArrayList)this.getSqlResultList(index).get(row);
    }
    /**
     * ��ȡ��һ��SQL��ĳ�дν����
     * @param row �д�
     * @return
     */
    public ArrayList getFirstSqlResultRow(int row){
        return this.getSqlResultRow(0,row);
    }
    /**
     * ��ȡ��һ��SQL�ĵ�һ�дν����
     * @return
     */
    public ArrayList getFirstSqlResultFirstRow(){
        return this.getFirstSqlResultRow(0);
    }


    /**
     * ��ȡĳ��SQL��ĳ��ĳ�еĽ��
     * @param index SQL����INDEX ��0��ʼ
     * @param row �д�
     * @param col �д�
     * @return
     */
    public String getSqlResultCell(int index,int row,int col){
        return (String)this.getSqlResultRow(index,row).get(col);
    }
    /**
     * ��ȡ��һ��SQL��ĳ��ĳ�еĽ��
     * @param row �д�
     * @param col �д�
     * @return
     */
    public String getFirstSqlResultCell(int row,int col){
        return this.getSqlResultCell(0,row,col);
    }
    /**
     * ��ȡ��һ��SQL�ĵ�һ��ĳ�еĽ��
     * @param col �д�
     * @return
     */
    public String getFirstSqlResultFirstRowCell(int col){
        return this.getFirstSqlResultCell(0,col);
    }
    /**
     * ��ȡ��һ��SQL�ĵ�һ�е�һ�еĽ��
     *
     * @return
     */
    public String getFirstSqlResultFirstCell(){
        return this.getFirstSqlResultFirstRowCell(0);
    }


    public java.util.ArrayList getDAOResultMetaDataList() {
        return dAOResultMetaDataList;
    }
    public void setDAOResultMetaDataList(java.util.ArrayList dAOResultMetaDataList) {
        this.dAOResultMetaDataList = dAOResultMetaDataList;
    }

    public void appendDAOResultMetaData(DAOResultMetaData drmt){
        if(dAOResultMetaDataList==null){
            dAOResultMetaDataList=new ArrayList();
        }
        dAOResultMetaDataList.add(drmt);
    }


    /**
     * ��ȡSQL��ٶ�Ӧ��DAOResultMetaData
     * @param index
     * @return
     */
    public DAOResultMetaData getSqlResultSetMetaData(int index){
        ArrayList al = getDAOResultMetaDataList();
        if ((al!=null)&&(al.size()>index)) {
            return (DAOResultMetaData)al.get(index);
        }else{
            logMgr.debug("û�з��ֵ�["+index+"]��SQL����Ӧ��DAOResultMetaData!");
            return null;
        }
    }

    /**
     * ��SQLINDEX��Ӧ��SQL�����ת��ΪDATASET����,���㴦��
     * @param sqlIndex
     * @return
     */
    public DataSet toDataSet(int sqlIndex){
        DAOResultMetaData resultMetaData = getSqlResultSetMetaData(sqlIndex);
        ArrayList sqlResultList = this.getSqlResultList(sqlIndex);
        int totalRowNum = 0;
        if (sqlResultList!=null) {
            totalRowNum = sqlResultList.size();
            if ( (resultValue != null) && (resultValue instanceof ArrayList)) {
                totalRowNum = ( (Integer) ( (ArrayList) resultValue).get(sqlIndex)).
                        intValue();
            }
        }
        return new DataSet(resultMetaData,sqlResultList,totalRowNum);
    }


    /**
     * �жϽ�����Ƿ�Ϊ��
     * @param sqlIndex
     * @return
     */
    public boolean isEmpty(int sqlIndex){
        ArrayList sqlResultList = this.getSqlResultList(sqlIndex);
        return (sqlResultList==null)||(sqlResultList.size()==0);
    }

    /**
     * ����ѯ������е�������֯��xml��ʽ����. added by lhy 200703231456
     * @param sqlIndex int SQL˳���
     * @return String
     */
    public String toXml(int sqlIndex) {
        return toXml(sqlIndex, true);
    }

    /**
     * ����ѯ������е�������֯��xml��ʽ����. added by lhy 200703231456
     * @param sqlIndex int SQL˳���
     * @param hasHeader boolean �Ƿ���XMLͷ
     * @return String
     */
    public String toXml(int sqlIndex, boolean hasHeader) {
        StringBuffer buf = new StringBuffer();
        if (hasHeader) {
            buf.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
        }
        buf.append("<RESULT>");
        DataSet sqlDst = this.toDataSet(sqlIndex);
        int count = sqlDst.getResultMetaData().getColumnCount();
        int rowNum = this.getSqlResultList(sqlIndex).size();
        String[] name = sqlDst.getResultMetaData().getColumnNameArray();
        for (int j = 0; j < rowNum; j++) {
            buf.append("<ROW    ");
            for (int k = 0; k < count; k++) {
                String value = sqlDst.getDataRow(j).getFieldValueByIndex(k);
                buf.append(name[k] + "=\"" + StringConverter.toEncodedXml(value) +
                        "\"   ");
            }
            buf.append("/>");
        }
        buf.append("</RESULT>");
        String result = buf.toString();
        return result;
    }

    /**
     * ��SQLINDEX��Ӧ��SQL�����ת��ΪJAVABEAN����,���㴦��
     * @param sqlIndex int
     * @param clz Class
     * @return ArrayList
     */
    public ArrayList toJavaBeanList(int sqlIndex,Class clz){
        DAOResultMetaData resultMetaData = getSqlResultSetMetaData(sqlIndex);
        ArrayList sqlResultList = this.getSqlResultList(sqlIndex);
        ArrayList al = new ArrayList();
        try {
            if (sqlResultList != null) {
                for (int t = 0; t < sqlResultList.size(); t++) {
                    ArrayList rowList = (ArrayList) sqlResultList.get(t);
                    Object rowObj = clz.newInstance();
                    for (int i = 0; i < resultMetaData.getColumnCount(); i++) {
                        String colName = resultMetaData.getColumnMetaData(i).getName();
                        try {
                            JavaBeanClass.setProperty(rowObj, colName, rowList.get(i));
                        }
                        catch (Exception ex) {
                            logMgr.warn( ex.getMessage());
                        }
                    }
                    al.add(rowObj);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            logMgr.debug( ex.getMessage());
        }
        return al;
    }


    public String toJson(int sqlIndex){
        return DAOResultUtil.toJsonMoreStr(this,sqlIndex);
    }

    public JSONArray toJsonArray(int sqlIndex) {
        return DAOResultUtil.toJsonMore(this,sqlIndex);
    }

    /**
     * added by lhy 200903251319
     * @return String
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
        String tempResultValue = "";
        if(resultValue!=null){
            tempResultValue = String.valueOf(resultValue);
            tempResultValue = tempResultValue.substring(1,tempResultValue.length()-1);
        }
        buf.append("<DAORESULT success=\"" + success + "\" error=\"" + error +
                "\" resultValue=\"" + tempResultValue + "\">");
        int size = this.getResultList().size();
        for (int i = 0; i < size; i++) {
            buf.append(this.toXml(i, false));
        }
        buf.append("</DAORESULT>");
        String result = buf.toString();
        return result;
    }

}
