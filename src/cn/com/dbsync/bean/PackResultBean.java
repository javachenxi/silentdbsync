package cn.com.dbsync.bean;


import cn.com.dbsync.dao.DAOResult;
import cn.com.dbsync.util.CommUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxi on 2016/5/9.
 */
public class PackResultBean {

    public static final int OPT_TOKEN_U = 1;
    public static final int OPT_TOKEN_I = 2;
    public static final int OPT_TOKEN_D = 3;

    private List<DataEntity> dataEntityList;
    private int allsize = 0;

    /**
     * Add data entity.
     *
     * @param confTableBean the conf table bean
     * @param result        the result
     */
    public void addDataEntity(ConfTableBean confTableBean, DAOResult result){
        if(dataEntityList == null){
            dataEntityList = new ArrayList<DataEntity>();
        }

        dataEntityList.add(new DataEntity(confTableBean, result));
    }

    public void addDataEntity(ConfTableBean confTableBean, DAOResult result, int[] optToken){
        if(dataEntityList == null){
            dataEntityList = new ArrayList<DataEntity>();
        }

        dataEntityList.add(new DataEntity(confTableBean, result, optToken));
    }

    /**
     * Get data entity list list.
     *
     * @return the list
     */
    public List<DataEntity> getDataEntityList(){
        return dataEntityList;
    }

    /**
     * Get size int.
     *
     * @return the int
     */
    public int getSize(){
        return this.allsize;
    }

    /**
     * The type Data entity.
     */
    public class DataEntity{
        private ConfTableBean confTableBean;
        private DAOResult result;
        private int size;
        private int[] optToken;

        public DataEntity(ConfTableBean confTableBean, DAOResult result, int[] optToken){
            this.confTableBean = confTableBean;
            this.result = result;
            this.size = result.getFirstSqlResultList().size();
            this.optToken = optToken;
            allsize+=this.size;
        }

        /**
         * Instantiates a new Data entity.
         *
         * @param confTableBean the conf table bean
         * @param result        the result
         */
        public DataEntity(ConfTableBean confTableBean, DAOResult result){
            this.confTableBean = confTableBean;
            this.result = result;
            this.size = result.getFirstSqlResultList().size();

            allsize+=this.size;
        }

        public int[] getOptToken() {
            return optToken;
        }

        public void setOptToken(int[] optToken) {
            this.optToken = optToken;
        }

        /**
         * Gets conf table bean.
         *
         * @return the conf table bean
         */
        public ConfTableBean getConfTableBean() {
            return confTableBean;
        }

        /**
         * Sets conf table bean.
         *
         * @param confTableBean the conf table bean
         */
        public void setConfTableBean(ConfTableBean confTableBean) {
            this.confTableBean = confTableBean;
        }

        /**
         * Gets result.
         *
         * @return the result
         */
        public DAOResult getResult() {
            return result;
        }

        /**
         * Sets result.
         *
         * @param result the result
         */
        public void setResult(DAOResult result) {
            this.result = result;
        }

        /**
         * Gets size.
         *
         * @return the size
         */
        public int getSize() {
            return size;
        }

        /**
         * Sets size.
         *
         * @param size the size
         */
        public void setSize(int size) {
            this.size = size;
        }
    }

}
