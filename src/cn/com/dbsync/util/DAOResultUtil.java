package cn.com.dbsync.util;

import cn.com.dbsync.dao.DAOResult;
import cn.com.dbsync.dao.DAOResultMetaData;
import cn.com.dbsync.dao.DataRow;
import cn.com.dbsync.dao.DataSet;
import net.sf.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017-09-30.
 */
public class DAOResultUtil {
    /**
     * ��DAOResultת��Ϊjson��ʽ�ַ���
     * @param rest �����
     * @return String json��ʽ�ַ���
     */
    public static String toJsonMoreStr(DAOResult rest) {
        return toJsonMoreStr(rest,0);
    }

    public static String toJsonMoreStr(DAOResult rest,int idx) {
        return toJsonMore(rest,idx).toString();

    }

    public static JSONArray toJsonMore(DAOResult rest) {
        return toJsonMore(rest,0);
    }

    /**
     * ��DAOResultת��Ϊjson����
     * @param rest �����
     * @return JSONObject json��ʽ����
     */
    public static JSONArray toJsonMore(DAOResult rest,int idx) {
        ArrayList record = new ArrayList();
        String[] columns = DAOResultUtil.getColumnName(rest);
        DataSet ds = rest.toDataSet(idx);
        Map map = null;
        DataRow dr = null;
        while (ds.hasNext()) {
            map = new HashMap();
            dr = (DataRow) ds.next();
            DAOResultUtil.buildRecord(dr, columns, map);
            record.add(map);
        }
        return JSONArray.fromObject(record);
    }

    /**
     * �����н����������������װmap����
     * @param dr �н����
     * @param columns ��������
     * @param map map����
     */
    public static void buildRecord(DataRow dr, String[] columns, Map map) {
        for (int i = 0; i < columns.length; ++i) {
            map.put(columns[i].toLowerCase(), dr.getFieldValueByIndex(i));
        }
    }

    /**
     * �ӽ�����л�ȡ��������
     * @param rest �����
     * @return String[] ��������
     */
    public static String[] getColumnName(DAOResult rest) {
        DAOResultMetaData dm = rest.toDataSet(0).getResultMetaData();
        return dm.getColumnNameArray();
    }
}
