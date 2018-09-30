package cn.com.dbsync.core;

import cn.com.dbsync.bean.ConfTableBean;
import cn.com.dbsync.bean.ConfTaskBean;

import java.text.MessageFormat;
import java.util.List;

/**
 * Created by Administrator on 2018/9/26/026.
 */
public class MysqlGeneratorSql extends DefaultGeneratorSql{

    protected static final String MYSQL_MINSERT_SQL_FROMAT = "INSERT INTO {0}({1}) VALUES({2}) ON DUPLICATE KEY UPDATE {3} ";
    protected static final String MYSQL_UPDATE_SQL_FROMAT = "UPDATE {0} SET {1} WHERE {2} ";

    /**
     * Instantiates a new Default generator sql.
     *
     * @param confTableBeanList the conf table bean list
     * @param confTaskBean      the conf task bean
     */
    public MysqlGeneratorSql(List<ConfTableBean> confTableBeanList, ConfTaskBean confTaskBean) {
        super(confTableBeanList, confTaskBean);
    }

    protected void buildsqlForBean(ConfTableBean confTableBean, StringBuilderCreater stringBuilderCreater ){
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

            StringBuilder incerWherestr= stringBuilderCreater.getStringBuilder("src_incerWherestr");
            StringBuilder incerorderbysql= stringBuilderCreater.getStringBuilder("src_incerorderbysql");

            StringBuilder tagorderbysql= stringBuilderCreater.getStringBuilder("tag_orderbysql");
            StringBuilder srcorderbysql= stringBuilderCreater.getStringBuilder("src_orderbysql");


            for(int i=0; i<columns.size(); i++){
                column = columns.get(i);

                if(column.isPkeyToken()){
                    continue;
                }

                updateSetcolsb.append(column.getTargetColumn()).append("=? ,");
                selectcolsb.append(column.getSourceColumn()).append(',');
                taginsertcolsb.append(column.getTargetColumn()).append(',');
                insertvaluecolsb.append("?,");


                if(column.isIncerToken()&&confTaskBean.isIncTask()){

                    if(incerWherestr.length() == 0){
                        incerWherestr.append(" WHERE ").append(column.getSourceColumn()).append(">? ");
                    }else{
                        incerWherestr.append(" AND ").append(column.getSourceColumn()).append(">? ");
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
                updateWherecolsb.append(column.getTargetColumn()).append("=? AND");

                selectcolsb.append(column.getSourceColumn()).append(',');
                taginsertcolsb.append(column.getTargetColumn()).append(',');
                insertvaluecolsb.append("?,");

                tagorderbysql.append(column.getTargetColumn()).append(',');
                srcorderbysql.append(column.getSourceColumn()).append(',');

                if(column.isIncerToken()){
                    if(incerWherestr.length() == 0){
                        incerWherestr.append(" WHERE ").append(column.getSourceColumn()).append(">? ");
                    }else{
                        incerWherestr.append(" AND ").append(column.getSourceColumn()).append(">? ");
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

            confTableBean.setUpdateSql(MessageFormat.format(MYSQL_UPDATE_SQL_FROMAT, confTableBean.getTargetTable(),
                    updateSetcolsb.deleteCharAt(updateSetcolsb.length()-1).toString(), updateWherecolsb.toString()));

            confTableBean.setDeleteSqlById(MessageFormat.format(DELETE_SQL_FROMAT,confTableBean.getTargetTable() + " where " + updateWherecolsb.toString()));

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
