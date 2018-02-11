package cn.com.dbsync.core;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;
import cn.com.dbsync.service.DBSyncConfService;
import cn.com.dbsync.util.SpringManager;

import java.text.MessageFormat;
import java.util.List;

/**
 * 依据 ConfTableBean 生成对应的SQL语句，并将SQL语句赋值给 ConfTableBean 的属性上
 * <p>
 * Created by cxi on 2016/5/7.
 */
public class DefaultGeneratorSql implements IGeneratorSql{

    private static final String  SELECT_SQL_FROMAT = "SELECT {0} FROM {1} {2} ";
    private static final String  ORDERBY_SQL_FROMAT = " ORDER BY {0} ";
    private static final String  DELETE_SQL_FROMAT = "DELETE FROM {0} ";
    private static final String  INSERT_SQL_FROMAT = "INSERT INTO {0}({1}) VALUES({2}) ";
    private static final String  UPDATE_SQL_FROMAT = "UPDATE {0} T SET {1} WHERE {2} ";
    private static final String  MINSERT_SQL_FROMAT = "MERGE INTO {0} T USING (SELECT {1} FROM DUAL)F ON ({2}) " +
            "WHEN MATCHED THEN UPDATE SET {3} WHEN NOT MATCHED THEN INSERT({4}) VALUES({5}) ";
    private List<ConfTableBean> confTableBeanList;
    private ConfTaskBean confTaskBean;
    private DBSyncConfService confService;

    /**
     * Instantiates a new Default generator sql.
     *
     * @param confTableBeanList the conf table bean list
     * @param confTaskBean      the conf task bean
     */
    public DefaultGeneratorSql(List<ConfTableBean> confTableBeanList, ConfTaskBean confTaskBean){
        this.confTableBeanList = confTableBeanList;
        this.confTaskBean = confTaskBean;
        this.confService = SpringManager.getInstance().getBeanByType(DBSyncConfService.class);
    }

    /**
     * 按同步配置表的关系，生成对应的SQL语句
     */
    public void buildsql(){
        StringBuilderCreater stringBuilderCreater = new StringBuilderCreater();
        ConfTableBean tmpConfTableBean = null;

        for(int i=0; confTableBeanList.size()>i; i++) {

            tmpConfTableBean = confTableBeanList.get(i);

            if(this.confService.getConfTableSqlBeanById(tmpConfTableBean) == null){
                buildsqlForBean(tmpConfTableBean, stringBuilderCreater);
            }


        }
    }

    private void buildsqlForBean(ConfTableBean confTableBean, StringBuilderCreater stringBuilderCreater ){

        List<ConfTableBean.ConfColumn> columns = confTableBean.getColumnList();
        List<ConfTableBean.ConfColumn> pkcolumns = confTableBean.getPkeycols();

        ConfTableBean.ConfColumn column = null;
        stringBuilderCreater.clearAll();

        StringBuilder selectcolsb = stringBuilderCreater.getStringBuilder("src_selectcolsb");
        StringBuilder selectcolsbPk = stringBuilderCreater.getStringBuilder("src_selectcolsbPk");

        StringBuilder taginsertcolsb = stringBuilderCreater.getStringBuilder("tag_insertcolsb");
        StringBuilder insertvaluecolsb = stringBuilderCreater.getStringBuilder("tag_insertvaluecolsb");
        StringBuilder updateSetcolsb = stringBuilderCreater.getStringBuilder("tag_updateSetcolsb");
        StringBuilder updateWherecolsb= stringBuilderCreater.getStringBuilder("tag_updateWherecolsb");
        StringBuilder mergeSelcolsb= stringBuilderCreater.getStringBuilder("tag_mergeSelcolsb");
        StringBuilder mergeOncolsb= stringBuilderCreater.getStringBuilder("tag_mergeOncolsb");
        StringBuilder mergeUpdcolsb= stringBuilderCreater.getStringBuilder("tag_mergeUpdcolsb");
        StringBuilder mergeInsvcolsb= stringBuilderCreater.getStringBuilder("tag_mergeInsvcolsb");
        StringBuilder incerWherestr= stringBuilderCreater.getStringBuilder("src_incerWherestr");
        StringBuilder incerorderbysql= stringBuilderCreater.getStringBuilder("src_incerorderbysql");

        StringBuilder tagorderbysql= stringBuilderCreater.getStringBuilder("tag_orderbysql");
        StringBuilder srcorderbysql= stringBuilderCreater.getStringBuilder("src_orderbysql");


        for(int i=0; i<columns.size(); i++){
            column = columns.get(i);

            if(column.isPkeyToken()){
                continue;
            }

            mergeUpdcolsb.append("T.").append(column.getTargetColumn()).append("=");
            mergeUpdcolsb.append("F.").append(column.getTargetColumn()).append(',');

            updateSetcolsb.append("T.").append(column.getTargetColumn()).append("=? ,");

            selectcolsb.append(column.getSourceColumn()).append(',');
            taginsertcolsb.append(column.getTargetColumn()).append(',');

            mergeSelcolsb.append("? ").append(column.getTargetColumn()).append(',');
            insertvaluecolsb.append("?,");
            mergeInsvcolsb.append("F.").append(column.getTargetColumn()).append(',');

            if(column.isIncerToken()&&confTaskBean.isIncTask()){

                if(incerWherestr.length() == 0){
                    incerWherestr.append(" WHERE ").append(column.getSourceColumn()).append(">=? ");
                }else{
                    incerWherestr.append(" AND ").append(column.getSourceColumn()).append(">=? ");
                }

                if(incerorderbysql.length() == 0){
                    incerorderbysql.append(column.getSourceColumn());

                }else{
                    incerorderbysql.append(",").append(column.getSourceColumn());
                }
            }


        }

        //处理主键列表
        for(int p=0; p<pkcolumns.size(); p++){
            column = pkcolumns.get(p);

            mergeOncolsb.append("T.").append(column.getTargetColumn()).append("=");
            mergeOncolsb.append("F.").append(column.getTargetColumn()).append(" AND ");

            updateWherecolsb.append(" T.").append(column.getTargetColumn()).append("=? AND");

            selectcolsb.append(column.getSourceColumn()).append(',');
            taginsertcolsb.append(column.getTargetColumn()).append(',');

            mergeSelcolsb.append("? ").append(column.getTargetColumn()).append(',');
            insertvaluecolsb.append("?,");
            mergeInsvcolsb.append("F.").append(column.getTargetColumn()).append(',');

            tagorderbysql.append(column.getTargetColumn()).append(',');
            srcorderbysql.append(column.getSourceColumn()).append(',');

            if(column.isIncerToken()){

                if(incerWherestr.length() == 0){
                    incerWherestr.append(" WHERE ").append(column.getSourceColumn()).append(">=? ");
                }else{
                    incerWherestr.append(" AND ").append(column.getSourceColumn()).append(">=? ");
                }

                if(incerorderbysql.length() == 0){
                    incerorderbysql.append(column.getSourceColumn());
                }else{
                    incerorderbysql.append(",").append(column.getSourceColumn());
                }
            }
        }



        if(confTaskBean.isIncTask()){
            //有依赖字段是子表，不需要配置增量字段；没有依赖关系是主表，应该在SELECTSQL增加增量字段排序
            if(confTableBean.getDependTable()!= null){
                incerWherestr.delete(0, incerWherestr.length());
                incerWherestr.append(" WHERE ").append(confTableBean.getRelateColumn()).append("=?");
            }else {
                incerWherestr.append( MessageFormat.format(ORDERBY_SQL_FROMAT, incerorderbysql.toString()) );
            }

            confTableBean.setSelectSql(
            MessageFormat.format(SELECT_SQL_FROMAT,selectcolsb.substring(0,selectcolsb.length()-1),
                    confTableBean.getSourceTable(), incerWherestr.toString()));
            //shortSelectSql 当任务配置中没有增量字段值时，使用该查询语句
            confTableBean.setShortSelectSql(
                    MessageFormat.format(SELECT_SQL_FROMAT,selectcolsb.substring(0,selectcolsb.length()-1),
                            confTableBean.getSourceTable(),MessageFormat.format(ORDERBY_SQL_FROMAT, incerorderbysql.toString())));

            confTableBean.setMergInsertSql(
                    MessageFormat.format(MINSERT_SQL_FROMAT,
                            confTableBean.getTargetTable(),
                            mergeSelcolsb.substring(0,mergeSelcolsb.length()-1),
                            mergeOncolsb.substring(0,mergeOncolsb.length()-4),   //减去结尾的and与空格
                            mergeUpdcolsb.substring(0,mergeUpdcolsb.length()-1),
                            taginsertcolsb.substring(0,taginsertcolsb.length()-1),
                            mergeInsvcolsb.substring(0,mergeInsvcolsb.length()-1)));


        }else{
            //有依赖字段是子表
            if(confTableBean.getDependTable()!= null){
                incerWherestr.delete(0, incerWherestr.length());
                incerWherestr.append(" WHERE ").append(confTableBean.getRelateColumn()).append("=?");
            }

            confTableBean.setSelectSql(
                    MessageFormat.format(SELECT_SQL_FROMAT,selectcolsb.substring(0,selectcolsb.length()-1),
                            confTableBean.getSourceTable(),incerWherestr.toString()));

        }

        confTableBean.setInsertSql(
                MessageFormat.format(INSERT_SQL_FROMAT,confTableBean.getTargetTable(),taginsertcolsb.substring(0,taginsertcolsb.length()-1),
                        insertvaluecolsb.substring(0,insertvaluecolsb.length()-1)));

        if(updateWherecolsb.length() > 3){
            updateWherecolsb.delete(updateWherecolsb.length()-3, updateWherecolsb.length());
        }

        confTableBean.setUpdateSql(MessageFormat.format(UPDATE_SQL_FROMAT, confTableBean.getTargetTable(),
                                   updateSetcolsb.deleteCharAt(updateSetcolsb.length()-1).toString(), updateWherecolsb.toString()));

        confTableBean.setDeleteSqlById(MessageFormat.format(DELETE_SQL_FROMAT,confTableBean.getTargetTable() + " T where " + updateWherecolsb.toString()));

        confTableBean.setDeleteSql(MessageFormat.format(DELETE_SQL_FROMAT, confTableBean.getTargetTable()));

        confTableBean.setSrcSelectByOrderSql(MessageFormat.format(SELECT_SQL_FROMAT,selectcolsb.substring(0,selectcolsb.length()-1),
                confTableBean.getSourceTable(), MessageFormat.format(ORDERBY_SQL_FROMAT, srcorderbysql.substring(0, srcorderbysql.length()-1))));

        confTableBean.setTagSelectByOrderSql(MessageFormat.format(SELECT_SQL_FROMAT,taginsertcolsb.substring(0,taginsertcolsb.length()-1),
                confTableBean.getTargetTable(),MessageFormat.format(ORDERBY_SQL_FROMAT, tagorderbysql.substring(0, tagorderbysql.length()-1))));

        this.confService.insertConfTableSql(confTableBean);

        List<ConfTableBean> tableBeanList = confTableBean.getChildList();

        if(tableBeanList != null){
            ConfTableBean tmpConfTableBean = null;

            for(int t=0; tableBeanList.size()>t; t++){
                tmpConfTableBean = tableBeanList.get(t);

                if(this.confService.getConfTableSqlBeanById(tmpConfTableBean) == null){
                    this.buildsqlForBean(tmpConfTableBean,stringBuilderCreater);
                }

            }
        }
    }


}





