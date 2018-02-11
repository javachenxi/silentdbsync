package cn.com.dbsync.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2018-01-21.
 */
public class StringBuilderCreater {

    private Map<String, StringBuilder> mapcache = null;

    public StringBuilderCreater(){
        mapcache = new HashMap<String, StringBuilder>();
    }

    public StringBuilder getStringBuilder(String sbkey){
        return getStringBuilder(sbkey, 0);
    }

    public StringBuilder getStringBuilder(String sbkey, int inisize){
        StringBuilder sb = mapcache.get(sbkey);

        if(sb == null){
            sb = inisize ==0 ?new StringBuilder():new StringBuilder(inisize);
            mapcache.put(sbkey, sb) ;
        }

        return sb;
    }

    public StringBuilder removeStringBuilder(String sbkey){
        StringBuilder sb = mapcache.remove(sbkey);
        return sb;
    }

    public void clearAll(){
        Iterator<StringBuilder> it = mapcache.values().iterator();
        StringBuilder stringBuilder = null;

        while(it.hasNext()){
            stringBuilder = it.next();
            stringBuilder.delete(0, stringBuilder.length()-1);
        }
    }
}
