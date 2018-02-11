package cn.com.dbsync.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

/**
 * Created by Administrator on 2017-09-30.
 */
public class DAOResultMetaData implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2406114825426802268L;
    private int columnCount = 0; //�е���Ŀ
    private ColumnMetaData[] columnMetaDataArray; //��������������
    private HashMap columnNameToIndexMap = new HashMap();//�ֶ���������Ŷ�Ӧ��ϵMAP
    private static final Log logMgr = LogFactory.getLog(DAOResultMetaData.class);


    public DAOResultMetaData() {
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public ColumnMetaData[] getColumnMetaDataArray() {
        return columnMetaDataArray;
    }

    public void setColumnMetaDataArray(ColumnMetaData[] columnMetaDataArray) {
        this.columnMetaDataArray = columnMetaDataArray;
        if (columnMetaDataArray != null) {
            columnNameToIndexMap.clear();
            for (int i = 0; i < columnMetaDataArray.length; i++) {
                columnNameToIndexMap.put(columnMetaDataArray[i].getName().toUpperCase(), new Integer(i));
            }
        }
    }

    public ColumnMetaData getColumnMetaData(int index) {
        return columnMetaDataArray[index];
    }

    public ColumnMetaData getColumnMetaData(String name) {
        return columnMetaDataArray[((Integer) columnNameToIndexMap.get(name)).intValue()];
    }

    public String[] getColumnNameArray() {
        String[] names = new String[this.columnCount];
        for (int i = 0; i < names.length; i++) {
            names[i] = this.getColumnMetaData(i).getName();
        }
        return names;
    }

    public int getColumnIndex(String name) {
        String key = name.trim().toUpperCase();
        if (columnNameToIndexMap.containsKey(key)) {
            return ((Integer) columnNameToIndexMap.get(key)).intValue();
        } else {
            logMgr.debug( "δ������ֶ���[" + key + "]!");
            return -1;//û���ҵ�
        }
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < columnCount; i++) {
            s = s + getColumnMetaData(i);
        }
        return "DAOResultMetaData[columnCount:" + columnCount +
                ",ColumnMetaDataArray:" + s +
                "]\r\n";
    }
}
