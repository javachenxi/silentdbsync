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
    private boolean success = true; //成功与否(默认为true)
    private StringBuffer error = new StringBuffer(""); //反馈错误信息
    private ArrayList resultList = new ArrayList();//反馈数据(内部又由多个ArrayList(每个元素又是一个ArrayList)组成)
    private Object resultValue;
    private ArrayList dAOResultMetaDataList = new ArrayList();//列的元数据 内部与SQL对应元素为DAOResultMetaData类型
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
     * 获取返回的结果集,不推荐使用该方法了
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





//以下为增加的快速取值方法 200312301138增加

    /**
     * 获取某条SQL的ResultValue值
     * @param index
     * @return
     */
    public Object getSqlResultValue(int index) {
        return ((ArrayList)this.getResultValue()).get(index);
    }
    /**
     * 获取某条SQL的ResultValue值
     *
     * @return
     */
    public Object getFirstSqlResultValue() {
        return this.getSqlResultValue(0);
    }



    /**
     * 获取某条SQL的二维结果集
     * @param index SQL语句的INDEX 从0开始
     * @return
     */
    public ArrayList getSqlResultList(int index){
        return (ArrayList)this.getResultList().get(index);
    }
    /**
     * 获取第一条SQL的二维结果集
     * @return
     */
    public ArrayList getFirstSqlResultList(){
        return this.getSqlResultList(0);
    }


    /**
     * 获取某条SQL的某行次结果集
     * @param index SQL语句的INDEX 从0开始
     * @param row 行次
     * @return
     */
    public ArrayList getSqlResultRow(int index,int row){
        return (ArrayList)this.getSqlResultList(index).get(row);
    }
    /**
     * 获取第一条SQL的某行次结果集
     * @param row 行次
     * @return
     */
    public ArrayList getFirstSqlResultRow(int row){
        return this.getSqlResultRow(0,row);
    }
    /**
     * 获取第一条SQL的第一行次结果集
     * @return
     */
    public ArrayList getFirstSqlResultFirstRow(){
        return this.getFirstSqlResultRow(0);
    }


    /**
     * 获取某条SQL的某行某列的结果
     * @param index SQL语句的INDEX 从0开始
     * @param row 行次
     * @param col 列次
     * @return
     */
    public String getSqlResultCell(int index,int row,int col){
        return (String)this.getSqlResultRow(index,row).get(col);
    }
    /**
     * 获取第一条SQL的某行某列的结果
     * @param row 行次
     * @param col 列次
     * @return
     */
    public String getFirstSqlResultCell(int row,int col){
        return this.getSqlResultCell(0,row,col);
    }
    /**
     * 获取第一条SQL的第一行某列的结果
     * @param col 列次
     * @return
     */
    public String getFirstSqlResultFirstRowCell(int col){
        return this.getFirstSqlResultCell(0,col);
    }
    /**
     * 获取第一条SQL的第一行第一列的结果
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
     * 获取SQL语举对应的DAOResultMetaData
     * @param index
     * @return
     */
    public DAOResultMetaData getSqlResultSetMetaData(int index){
        ArrayList al = getDAOResultMetaDataList();
        if ((al!=null)&&(al.size()>index)) {
            return (DAOResultMetaData)al.get(index);
        }else{
            logMgr.debug("没有发现第["+index+"]条SQL语句对应的DAOResultMetaData!");
            return null;
        }
    }

    /**
     * 将SQLINDEX对应的SQL结果集转换为DATASET对象,方便处理
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
     * 判断结果集是否为空
     * @param sqlIndex
     * @return
     */
    public boolean isEmpty(int sqlIndex){
        ArrayList sqlResultList = this.getSqlResultList(sqlIndex);
        return (sqlResultList==null)||(sqlResultList.size()==0);
    }

    /**
     * 将查询结果集中的数据组织成xml形式返回. added by lhy 200703231456
     * @param sqlIndex int SQL顺序号
     * @return String
     */
    public String toXml(int sqlIndex) {
        return toXml(sqlIndex, true);
    }

    /**
     * 将查询结果集中的数据组织成xml形式返回. added by lhy 200703231456
     * @param sqlIndex int SQL顺序号
     * @param hasHeader boolean 是否含有XML头
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
     * 将SQLINDEX对应的SQL结果集转换为JAVABEAN对象,方便处理
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
