package cn.com.dbsync.util;

import cn.com.dbsync.core.SimpleObjectCache;
import java.io.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * Title: CommUtil
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009-2016
 * </p>
 * <p>
 * Company: servyou
 * </p>
 *
 * @author cxi
 * @version 1.0
 * @created 2016 /5/31
 */
public class CommUtil {

    private static final int ERROR_MSG_MAX = 1025;
    private static final int DATA_FORMAT_SHORT_SIZE = 11;
    private static final int DATA_FORMAT_NORMAL_SIZE = 20;
    private static final String MODID_DELIMITER = "-";

    private static final String DATA_FORMAT_LONG = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String DATA_FORMAT_NORMAL = "yyyy-MM-dd HH:mm:ss";
    private static final String DATA_FORMAT_SHORT = "yyyy-MM-dd";

    //线程安全的，软连接对象缓存
    private static final ThreadLocal<SimpleObjectCache<String,SimpleDateFormat>> THREAD_DATEFORMAT
            = new ThreadLocal<SimpleObjectCache<String,SimpleDateFormat>>() {
       public SimpleObjectCache<String, SimpleDateFormat> initialValue() {
            return new SimpleObjectCache<String, SimpleDateFormat>(
                    new SimpleObjectCache.IGenerateObject<String, SimpleDateFormat>(){
                public SimpleDateFormat create(String key) {
                    return new SimpleDateFormat(key);
                }
            });
        }
    };

    public static String subFixedStr(String msg){
        return  msg == null || msg.length()<ERROR_MSG_MAX? msg:msg.substring(0, ERROR_MSG_MAX);
    }

    public static int[] transformListInt(List<Integer> ints){
         if(ints == null && ints.size() == 0){
             return null;
         }

        int[] aints = new int[ints.size()];
        int i = 0;

        for(Integer ti: ints){
            aints[i++] = ti.intValue();
        }

        return aints;
    }


    /**
     * 支持三种格式化字符串，解析为日期类型
     *
     * @param dateStr the date str
     * @return date
     */
    public static Date parseStrToDate(String dateStr){

        Date ret = null;

        try {
            if (dateStr.length() < DATA_FORMAT_SHORT_SIZE) {
                ret = THREAD_DATEFORMAT.get().getObject(DATA_FORMAT_SHORT).parse(dateStr);
            } else if (dateStr.length() < DATA_FORMAT_NORMAL_SIZE) {
                ret = THREAD_DATEFORMAT.get().getObject(DATA_FORMAT_NORMAL).parse(dateStr);
            } else {
                ret = THREAD_DATEFORMAT.get().getObject(DATA_FORMAT_LONG).parse(dateStr);
            }
        } catch (Exception e) {
            ret = null;
        }

        return ret;
    }

    /**
     * Mk full dir file.
     *
     * @param dirpath the dirpath
     * @return the file
     */
    public static File mkFullDir(String dirpath){
        return mkFullDir(new File(dirpath));
    }


    /**
     * Mk full dir file.
     *
     * @param dirpath the dirpath
     * @return the file
     */
    public static File mkFullDir(File dirpath){
        File dirfile = dirpath;
        try {
            File tmpFile = dirfile.isFile() ? dirfile.getParentFile() : dirfile;
            List<File> filelist = new ArrayList<File>();

            while (!tmpFile.exists()) {
                filelist.add(tmpFile);
                tmpFile = tmpFile.getParentFile();
            }

            for (int i = filelist.size()-1; i >= 0; i--) {
                tmpFile = filelist.get(i);
                tmpFile.mkdir();
            }
        }catch (Exception e){
           throw new RuntimeException("新建目录失败。DIR="+dirpath, e);
        }

        return dirfile;

    }

    /**
     * Format interval to str string.
     *
     * @param interval the interval
     * @return the string
     */
    public static String formatIntervalToStr(long interval){

        StringBuilder timestr = new StringBuilder(64);

        int h = (int)(interval/(60*60*1000));
        int m = (int)(interval/(60*1000));
        int s = (int)(interval/1000);

        if(h>0){
            timestr.append(h).append("小时");
        }

        if(m - h*60 > 0){
            timestr.append(m - h*60).append("分");
        }

        if(s - m*60 > 0){
            timestr.append(s - m*60).append("秒");
        }else if(timestr.length() == 0){
            double ss = interval/1000.0;
            DecimalFormat numFormat = new DecimalFormat("0.##");
            timestr.append(numFormat.format(ss)).append("秒");
        }

        return timestr.toString();
    }

    /**
     * 格式化日期类型:yyyy-MM-dd HH:mm:ss
     *
     * @param date the date
     * @return string
     */
    public static String formatDateToNormalStr(Date date){
        return date==null?null:THREAD_DATEFORMAT.get().getObject(DATA_FORMAT_NORMAL).format(date);
    }


    /**
     * 读取字节流中固定长度的字节
     *
     * @param inputStream the input stream
     * @param size        the size
     * @return byte [ ]
     */
    public static byte[] readInputStream(InputStream inputStream, int size){

        byte[] bytes = null;
        int tmpSize = size;

        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(size);

            int length = 0;
            int readlen = Math.min(1024, size);

            while(tmpSize > 0 &&(length = inputStream.read(buffer, 0, readlen))!= -1){
                byteOutput.write(buffer, 0, length);
                tmpSize -= length;
                readlen = Math.min(readlen, tmpSize);
            }

            bytes = byteOutput.toByteArray();

        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        return bytes;
    }

    /**
     * Store temp file file.
     *
     * @param inputStream the input stream
     * @return the file
     * @throws IOException the io exception
     */
    public static File storeTempFile(InputStream inputStream)throws IOException{
        FileOutputStream outputStream = null;
        File tmpfile = null;

        try {
            tmpfile = File.createTempFile("Temp", "lb");
            outputStream = new FileOutputStream(tmpfile);
            copyStream(inputStream, outputStream);
        }finally {
            if(outputStream != null){
                outputStream.close();
            }
        }

        return tmpfile;
    }

    /**
     * Copy stream.
     *
     * @param inputStream the input stream
     * @param outputStram the output stram
     */
    public static void copyStream(InputStream inputStream, OutputStream outputStram){
        if(inputStream == null || outputStram == null){
            return;
        }

        byte[] buffer = new byte[1024*4];
        int length = 0;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                outputStram.write(buffer, 0, length);
            }
        }catch (Exception e){
            throw new RuntimeException("输出流失败。", e);
        }
    }

    /**
     * 读取字符流中固定长度的字符
     *
     * @param inputReader the input reader
     * @param size        the size
     * @return string
     */
    public static String readCharacterStream(Reader inputReader, int size){
        String str = null;
        int tmpSize = size;
        try {
            char[] buffer = new char[1024];
            StringBuilder result = new StringBuilder(1024);
            int length = 0;
            int readlen = Math.min(1024, size);

            while(tmpSize > 0 &&(length = inputReader.read(buffer, 0, readlen))!= -1){
                result.append(buffer, 0, length);
                tmpSize -= length;
                readlen = Math.min(readlen, tmpSize);
            }

            str = result.toString();
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        return str;
    }

    /**
     * 将BEAN中的值按Field读取到Map中
     *
     * @param map  the map
     * @param bean the bean
     * @throws IllegalAccessException the illegal access exception
     */
    public static void transBeanFieldToMap(Map<String, Object> map, Object bean) throws IllegalAccessException {

        Class beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();

        for(Field tmpField : fields){

            if(!tmpField.isAccessible()){
                tmpField.setAccessible(true);
            }

            map.put(tmpField.getName(), tmpField.get(bean));
        }

    }


    /**
     * 由标签ID、标签对象类型ID、时间粒度组成模板ID
     *
     * @param tagId     the tag id
     * @param objtypeId the objtype id
     * @param sjld      the sjld
     * @return string
     */
    public static String buildTaskModid(int tagId, int objtypeId,String sjld){
        return tagId+MODID_DELIMITER+objtypeId+MODID_DELIMITER+sjld;
    }

    /**
     * 用分隔符号分解模板ID，数组长度是3
     *
     * @param modid the modid
     * @return string [ ]
     */
    public static String[] parseTaskModid(String modid){
        return modid.split(MODID_DELIMITER);
    }


}