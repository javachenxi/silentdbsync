package cn.com.dbsync.util;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-09-30.
 */
public class JsonUtil {

    public static List getListForJson(String jsonString, Class pojoClass) {
        JSONArray jsonArray = JSONArray.fromObject(jsonString);
        ArrayList list = new ArrayList();

        for(int i = 0; i < jsonArray.size(); ++i) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Object pojoValue = JSONObject.toBean(jsonObject, pojoClass);
            list.add(pojoValue);
        }

        return list;
    }

    public static String getJsonStringForList(List javaObjs) {
        JSONArray json = JSONArray.fromObject(javaObjs);
        return json.toString();
    }


    public static String getJsonStringForJavaPOJO(Object javaObj) {
        JSONObject json = JSONObject.fromObject(javaObj);
        return json.toString();
    }


}
