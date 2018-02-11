package cn.com.dbsync.core;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.util.CommUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2018-01-22.
 */
public class RowDataMediator {

    public enum CompareType{GR, LE,EQ, EQCH}

    private ConfTableBean conftable;
    private int pksize;

    public RowDataMediator( ConfTableBean conftable){
         this.conftable = conftable;
         this.pksize = conftable.getPkeycols().size();
    }

    public CompareType compareToData(ArrayList srcList, ArrayList tagList){

          int rt = 0;

          for(int p=srcList.size()-pksize; p<srcList.size(); p++){
              rt = compareToObj(srcList.get(p), tagList.get(p));

              if(rt != 0){
                  break;
              }
          }

          if(rt > 0){
              return CompareType.GR;
          }else if(rt < 0){
              return CompareType.LE;
          }

          for(int c=0; c < srcList.size()-pksize; c++){

              rt = compareToObj(srcList.get(c), tagList.get(c));

              if(rt != 0){
                  return CompareType.EQCH;
              }
          }

        return CompareType.EQ;
    }

    public int compareToObj(Object src, Object tag){
        int rt = 0;

        if(src == null && tag == null){
             return rt;
        }

        if((src == null && tag != null)
                ||(src != null && tag == null)){
            return 1;
        }

        if(src instanceof File){
            if(tag instanceof File){
                rt = compareToFile((File)src, (File)tag);
            }else {
                return 1;
            }
        }else{

            if(tag instanceof File){
                return 1;
            }

            if(src instanceof Integer
                    || src instanceof Long ){
                rt = compareToByLong(Long.parseLong(src.toString()), Long.parseLong(tag.toString()));
            }else if(src instanceof Double){
                rt = compareToByDouble(Double.parseDouble(src.toString()), Double.parseDouble(tag.toString()));
            }else if(src instanceof BigDecimal){
                if(tag instanceof BigDecimal) {
                    rt = ((BigDecimal) src).compareTo( (BigDecimal) tag );
                }else{
                    rt = ((BigDecimal) src).compareTo(new BigDecimal(tag.toString()));
                }
            }else if(src instanceof Date){
                if(tag instanceof Date){
                    rt = ((Date)src).compareTo((Date) tag);
                }else if(tag instanceof Long){
                    rt = ((Date)src).compareTo(new Date((Long)tag));
                }else {
                    rt = ((Date)src).compareTo(CommUtil.parseStrToDate(tag.toString()));
                }
            }else {
                byte[] srcb = transformObjToBytes(src);
                byte[] tagb = transformObjToBytes(tag);
                rt = compareToBytes(srcb, tagb);
            }
        }

        return rt;
    }


    public int compareToByDouble(double src, double tag){
        if(src == tag){
            return 0;
        }

        if(src > tag){
            return 1;
        }

        return -1;
    }

    public int compareToByLong(long src, long tag){

        if(src == tag){
            return 0;
        }

        if(src > tag){
            return 1;
        }

        return -1;
    }

    public int compareToBytes(byte[] src, byte[] tag){

        int rt = 0;
        int length = Math.min(src.length, tag.length);

        for(int i=0; i<length; i++){

             if(src[i] > tag[i]){
                 rt = 1;
                 break;
             }

            if(src[i] < tag[i]){
                 rt = -1;
                 break;
             }
         }

         if(rt == 0 && src.length != tag.length){

              if(src.length > tag.length){
                  rt = 1;
              }else{
                  rt = -1;
              }
         }

        return rt;
    }

    public int compareToFile(File src, File tag){
        FileInputStream srcinput = null;
        FileInputStream taginput = null;

        byte[] bytes = null;

        try {
            srcinput = new FileInputStream(src);
            taginput = new FileInputStream(tag);

            byte[] buffer = new byte[4096];
            int length = 0;
            byte[] tagbuffer = null;

            while((length = srcinput.read(buffer, 0, 4096))!= -1){

                tagbuffer = CommUtil.readInputStream(taginput, length);

                for(int i=0; i<length; i++){
                     if(buffer[i]!=tagbuffer[i]){
                          return 1;
                     }
                }

            }

        } catch (Exception ex) {
            //ex.printStackTrace();
        }finally {
            if (srcinput != null){
                try {
                    srcinput.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }

            if (taginput != null){
                try {
                    taginput.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }

        return 0;
    }


    private byte[] transformObjToBytes(Object o){
        byte[] srcbyte = null;

        if(o instanceof String){
            srcbyte = ((String)o).getBytes();
        }else if(o instanceof byte[]){
            srcbyte = ((byte[])o);
        }else if(o instanceof Timestamp || o instanceof Date){
            srcbyte = (CommUtil.formatDateToNormalStr((Date) o)).getBytes();
        }else {
            srcbyte = o.toString().getBytes();
        }

        return srcbyte;
    }

    public static void main(String[] args){

        BigDecimal bigDecimal = new BigDecimal(4);
        BigDecimal bigDecimal1 = new BigDecimal(4);
        //bigDecimal.setScale(9);
        System.out.println(bigDecimal.toString());
        System.out.println(bigDecimal.scale());

        System.out.println(bigDecimal.compareTo(bigDecimal1));
        System.out.println(bigDecimal.doubleValue());

        Double d = new Double(4.4444d);

        System.out.println(Long.toHexString(((new Date())).getTime()));
        System.out.println(Long.toHexString(-1000l));

    }



}
