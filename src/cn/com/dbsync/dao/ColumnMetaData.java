package cn.com.dbsync.dao;

/**
 * Created by Administrator on 2017-09-30.
 */
public class ColumnMetaData implements java.io.Serializable{
    /**
     *
     */
    private static final long serialVersionUID = -2086189975269679731L;
    private String catalogName;//��ȡ�еı��Ŀ¼����DB2 Everyplace ���Ƿ��� ""�������ã���
    private int displaySize;//ָʾָ���е���������ȣ����ַ��ƣ���
    private String label;//��ȡ�ڴ�ӡ�������ʾ��ʹ�õĽ����б��⡣
    private String name;//��ȡָ���е����ơ�
    private int type;//��ȡָ���е� SQL ���͡�
    private String typeName;//�����е��ض������ݿ����������
    private int precision;//��ȡָ���е�С��λ����
    private int scale;//��ȡָ���е�С�����ұߵ�λ����
    private String schemaName;//��ȡ�еı��ģʽ����DB2 Everyplace ���Ƿ��� ""�������ã���//ָʾָ�����е�ֵ�Ŀɿ��ԡ�
    private boolean writable;//ָʾ���е�д�����ܷ�ɹ�
    private boolean readOnly;//�������Ϊֻ�����򷵻� true��
    private boolean currency;//������а������л��ҵ�λ��һ�����֣��򷵻� true��
    private boolean autoIncrement;//��������Զ��������򷵻� true��������ͨ��Ϊ��������ʼ����ֻ���ġ�
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
