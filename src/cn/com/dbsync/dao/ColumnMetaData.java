package cn.com.dbsync.dao;

/**
 * Created by Administrator on 2017-09-30.
 */
public class ColumnMetaData implements java.io.Serializable{
    /**
     *
     */
    private static final long serialVersionUID = -2086189975269679731L;
    private String catalogName;//获取列的表的目录名。DB2 Everyplace 总是返回 ""（不适用）。
    private int displaySize;//指示指定列的正常最大宽度（以字符计）。
    private String label;//获取在打印输出和显示中使用的建议列标题。
    private String name;//获取指定列的名称。
    private int type;//获取指定列的 SQL 类型。
    private String typeName;//检索列的特定于数据库的类型名。
    private int precision;//获取指定列的小数位数。
    private int scale;//获取指定列的小数点右边的位数。
    private String schemaName;//获取列的表的模式名。DB2 Everyplace 总是返回 ""（不适用）。//指示指定列中的值的可空性。
    private boolean writable;//指示对列的写操作能否成功
    private boolean readOnly;//如果此列为只读，则返回 true。
    private boolean currency;//如果此列包含带有货币单位的一个数字，则返回 true。
    private boolean autoIncrement;//如果此列自动递增，则返回 true。这类列通常为键，而且始终是只读的。
    private boolean caseSensitive;
    private boolean searchable;
    private boolean signed;
    private String tableName;
    private boolean definitelyWritable;
    private String className;
    private int nullable;

    public ColumnMetaData() {
    }
    public String getCatalogName() {
        return catalogName;
    }
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    public int getDisplaySize() {
        return displaySize;
    }
    public void setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    public int getPrecision() {
        return precision;
    }
    public void setPrecision(int precision) {
        this.precision = precision;
    }
    public int getScale() {
        return scale;
    }
    public void setScale(int scale) {
        this.scale = scale;
    }
    public String getSchemaName() {
        return schemaName;
    }
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    public int getNullable() {
        return nullable;
    }
    public void setNullable(int nullable) {
        this.nullable = nullable;
    }
    public boolean isWritable() {
        return writable;
    }
    public void setWritable(boolean writable) {
        this.writable = writable;
    }
    public boolean isReadOnly() {
        return readOnly;
    }
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    public boolean isCurrency() {
        return currency;
    }
    public void setCurrency(boolean currency) {
        this.currency = currency;
    }
    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    public boolean isSearchable() {
        return searchable;
    }
    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }
    public boolean isSigned() {
        return signed;
    }
    public void setSigned(boolean signed) {
        this.signed = signed;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public boolean isDefinitelyWritable() {
        return definitelyWritable;
    }
    public void setDefinitelyWritable(boolean definitelyWritable) {
        this.definitelyWritable = definitelyWritable;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }

    public String toString(){
        return "ColumnMetaData[name:"+name+
                ",catalogName:"+catalogName+
                ",displaySize:"+displaySize+
                ",label:"+label+
                ",type:"+type+
                ",typeName:"+typeName+
                ",precision:"+precision+
                ",scale:"+scale+
                ",schemaName:"+schemaName+
                ",writable:"+writable+
                ",readOnly:"+readOnly+
                ",currency:"+currency+
                ",autoIncrement:"+autoIncrement+
                ",caseSensitive:"+caseSensitive+
                ",searchable:"+searchable+
                ",signed:"+signed+
                ",tableName:"+tableName+
                ",definitelyWritable:"+definitelyWritable+
                ",className:"+className+
                ",nullable:"+nullable+
                "]\r\n";
    }
}
