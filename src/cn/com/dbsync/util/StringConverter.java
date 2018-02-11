package cn.com.dbsync.util;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2017-09-30.
 */
public class StringConverter {



    private StringConverter() {

    }

    //---Constants-------------------------------------------------------

    static final int MAXSPACES = 2560;

    static final int MAXZEROS = 2560;

    static final int DEFAULTDECIMALS = 6;

    static final int EXPONENTLEN = 3;

    //---Pseudo Constants------------------------------------------------

    static String SPACES = "                                        ";

    static int SPACESLEN = 40;

    static String ZEROS = "0000000000000000000000000000000000000000";

    static int ZEROSLEN = 40;

    static Object lock = new Object();

    //---Public general String handling methods--------------------------

    /**
     * 判断字符串是否为null或空
     * @param str
     * @return
     */
    public static boolean isNullString(String str) {

        return (str == null || str.equals(""));

    }

    /**
     * 返回固定个数空格的空字符串 (i.e. a String which consists of only blanks)
     * @param       len length of String to return.
     * @return      Blank String of the given length.
     * @exception   StringIndexOutOfBoundsException if len is larger than
     *              MAXSPACES (currently 2560).
     */

    static public String getSpaces(int len)

    {

        if (len > SPACESLEN && SPACESLEN < MAXSPACES) {

            //aquire lock only when neccessary

            synchronized (lock) {

                while (len > SPACESLEN && SPACESLEN < MAXSPACES) {

                    SPACES += SPACES;

                    SPACESLEN += SPACESLEN;

                }

            }

        }

        return SPACES.substring(0, len);

    }

    /**
     * 返回固定个数0的空字符串
     * @param       len length of String to return.
     * @return      Zero-filled String of the given length.
     * @exception   StringIndexOutOfBoundsException if len is larger than
     *              MAXZEROS (currently 2560).
     */

    static public String getZeros(int len)

    {

        if (len > ZEROSLEN && ZEROSLEN < MAXZEROS) {

            //aquire lock only when neccessary

            synchronized (lock) {

                while (len > ZEROSLEN && ZEROSLEN < MAXZEROS) {

                    ZEROS += ZEROS;

                    ZEROSLEN += ZEROSLEN;

                }

            }

        }

        return ZEROS.substring(0, len);

    }

    /**
     * 将源字符串复制times个后返回,如果源字符串为null或空则按照空格对待
     * @param source 源字符串
     * @param times 复制次数
     * @return 复制后的字符串
     */
    public static String getCopy(String source, int times) {
        if (StringConverter.isNullString(source)) {
            return StringConverter.getSpaces(times); //如果是NULL或"",则返回times个空
        }
        StringBuffer result = new StringBuffer( (source.length()) * times);
        for (int i = 0; i < times; i++) {
            result.append(source);
        }
        return result.toString();
    }

    /**
     * 将源字符串左填某一个字符到固定长度(如果S长度小于len,则s左填str到len长字节,否则返回len长的s,要求str.length=1)
     * @param s 源字符串
     * @param len 长度
     * @param str 填充字符(只能是一个字符)
     * @return len长的串
     */
    static public String padLeftChar(String s, int len, String str) {
        s = StringConverter.nvl(s);
        int slen = s.getBytes().length;
        String ret;
        if (slen < len) {
            ret = StringConverter.getCopy(str, len - slen) + s;
        }
        else {

            ret = substring(s, len); //s.substring(0, len);
        }
        return ret;
    }

    /**
     * 截取源字符串从开始位置起固定字节长度的串(主要用于包含汉字按字节位置和长度截取字符串情况,例如substring("我爱你的", 5)返回值为"我爱 ")
     * @param s 源字符串
     * @param bytelength 长度,都是字节单位而不是字符
     * @return
     */
    public static String substring(String s, int bytelength) {
//    byte[]  = new byte[length];
        int charlength = 0;
        for (int i = 0; i < s.length(); i++) {
            charlength += String.valueOf(s.charAt(i)).getBytes().length;
            if (charlength == bytelength) {
                return s.substring(0, i + 1);
            }
            else if (charlength > bytelength) { //当双字节时才有可能到这步，加一个" "
                return s.substring(0, i) + " ";
            }
            ;
        }
        return s;
    }

    /**
     * 将源字符串右填某一个字符到固定长度(如果S长度小于len,则s左填str到len长,否则,返回len长字节的s,要求str.length=1)
     * @param s 源字符串
     * @param len 长度
     * @param str 填充字符(只能是一个字符)
     * @return len长的串
     */
    static public String padRightChar(String s, int len, String str) {
        s = StringConverter.nvl(s);
        int slen = s.getBytes().length;
        String ret;
        if (slen < len) {
            ret = s + StringConverter.getCopy(str, len - slen);
        }
        else {
            ret = substring(s, len); //s.substring(0, len);
        }
        return ret;
    }

    /**
     * 将源字符串左填空格到固定长度,对于源字符串不执行trim操作(如果S长度小于len,则s左填空格到len长字节,否则返回len长的s)
     * @param s 源字符串
     * @param len 长度
     * @return len长的串
     */

    static public String padLeftSpace(String s, int len)

    {

        return padLeftSpace(s, len, false);

    }

    /**
     * 将源字符串左填空格到固定长度,对于源字符串根据传递的trim属性决定是否先取消掉左右的空格(如果S长度小于len,则s左填空格到len长字节,否则返回len长的s)
     * @param s 源字符串
     * @param len 长度
     * @param trim 是否要先取消掉左右的空格
     * @return len长的串
     */

    static public String padLeftSpace(String s, int len, boolean trim)

    {
        s = StringConverter.nvl(s);
        if (trim) {
            s = s.trim();
        }
        return padLeftChar(s, len, " ");
    }

    /**
     * 将源字符串右填空格到固定长度,对于源字符串不执行trim操作(如果S长度小于len,则s右填空格到len长字节,否则返回len长的s)
     * @param s 源字符串
     * @param len 长度
     * @return len长的串
     */

    static public String padRightSpace(String s, int len)

    {

        return padRightSpace(s, len, false);

    }

    /**
     * 将源字符串右填空格到固定长度,对于源字符串根据传递的trim属性决定是否先取消掉左右的空格(如果S长度小于len,则s右填空格到len长字节,否则返回len长的s)
     * @param s 源字符串
     * @param len 长度
     * @param trim 是否要先取消掉左右的空格
     * @return len长的串
     */

    static public String padRightSpace(String s, int len, boolean trim)

    {
        s = StringConverter.nvl(s);
        if (trim) {
            s = s.trim();
        }
        return padRightChar(s, len, " ");
    }


    private static final char cKanJiSpace = '\uFFFD';

    /**
     * 看SUB串是否是SOURCE串的子串
     * @param source 不能为null
     * @param sub 不能为null
     * @return
     */
    public static boolean isSubstring(String source, String sub) {
        return (source.indexOf(sub) != -1);
    }

    private static String replaceStr_old(String source, String oldString,
                                         String newString) {//deleted by lhy 201105262223
        if (oldString == null || oldString.length() == 0) {
            return source;
        }
        if (source == null) {
            return "";
        }
        if (newString == null) {
            newString = "";
        }

        StringBuffer output = new StringBuffer();
        int lengthOfSource = source.length();
        int lengthOfOld = oldString.length();
        int posStart = 0;
        int pos;
        while ( (pos = source.indexOf(oldString, posStart)) >= 0) {
            output.append(source.substring(posStart, pos));
            output.append(newString);
            posStart = pos + lengthOfOld;
        }
        if (posStart < lengthOfSource) {
            output.append(source.substring(posStart));
        }
        return output.toString();
    }

    /**
     * 把源字符串中的oldString以newString来替换掉,如果oldString为null或空则返回源字符串,如果newString为null则认为空
     * @param source 源字符串
     * @param oldString 被替换的字符串
     * @param newString 新的字符串
     * @return 替换后的字符串
     */
    public static String replaceStr(String source, String oldString,
                                    String newString) { //edited by lhy 201105262223 对当没有找到源串时候替换效率进行了提高,速度提高5倍
        if (oldString == null || oldString.length() == 0) {
            return source;
        }
        if (source == null) {
            return "";
        }
        if (newString == null) {
            newString = "";
        }
        int posStart = 0;
        int pos = source.indexOf(oldString, posStart);
        if (pos==-1) {
            return source;
        }
        StringBuffer output = new StringBuffer();
        int lengthOfSource = source.length();
        int lengthOfOld = oldString.length();
        while ( pos >= 0) {
            output.append(source.substring(posStart, pos));
            output.append(newString);
            posStart = pos + lengthOfOld;
            pos = source.indexOf(oldString, posStart);
        }
        if (posStart < lengthOfSource) {
            output.append(source.substring(posStart));
        }
        return output.toString();
    }

    /**
     * 将源字符串中的控制字符和TAB字符转换成空格
     * @param str 源字符串
     * @return 转换后的字符串
     */
    public static String chgCRLFTAB2SPC(String str) {

        String tmp;

        tmp = str;

        tmp = replaceStr(tmp, "\r\n", " ");

        tmp = replaceStr(tmp, "\r", " ");

        tmp = replaceStr(tmp, "\n", " ");

        tmp = replaceStr(tmp, "\b", " ");

        return tmp;

    }

    /**
     * 看源字符串是否是整数
     * @param str 源字符串
     * @return
     */
    public static boolean isNumber(String str) {
        if(str==null || str.trim().length()==0)
            return false;
        int i;
        for( i = 0; i < str.length(); i++ ) {
            if( !java.lang.Character.isDigit(str.charAt(i)) )
                return false;
        }
        return true;
    }

    /**
     * 看源字符串是否是数字或英文字母
     * @param str 源字符串
     * @return
     */
    public static boolean isEntNum(String str) {

        String all =
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        int i;

        for (i = 0; i < str.length(); i++) {

            if (all.indexOf(str.substring(i, i + 1)) < 0) {

                return false;

            }

        }

        return true;

    }

    /**
     * 如果传递的源字符串为null则返回空"",否则返回本身
     * @param sIn 源字符串
     * @return
     */
    public static String nvl(String sIn) {

        return (sIn == null) ? "" : sIn;

    }


    /**
     * 如果传递的源字符串为null则返回指定的串,否则返回本身
     * @param sIn 源字符串
     * @param sDef 源字符串为null时候返回的指定串
     * @return
     */
    public static String nvl(String sIn, String sDef) {

        return (sIn == null) ? sDef : sIn;

    }

    /**
     * 检测传入的字符串是否是EMAIL
     * @param mail
     * @return
     */
    public static boolean chkMail(String mail) {

        int i;

        int len = mail.length();

        int aPos = mail.indexOf("@");

        int dPos = mail.indexOf(".");

        int aaPos = mail.indexOf("@@");

        int adPos = mail.indexOf("@.");

        int ddPos = mail.indexOf("..");

        int daPos = mail.indexOf(".@");

        String lastChar = mail.substring(len - 1, len);

        String chkStr =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_@.";

        if ( (aPos <= 0)

                || (aPos == len - 1)

                || (dPos <= 0)

                || (dPos == len - 1) || (adPos > 0) || (daPos > 0) ||

                (lastChar.equals("@"))

                || (lastChar.equals(".")) || (aaPos > 0) || (ddPos > 0)) {

            return false;

        }

        if (mail.indexOf("@", aPos + 1) > 0) {

            return false;

        }

        while (aPos > dPos) {

            dPos = mail.indexOf(".", dPos + 1);

            if (dPos < 0) {

                return false;

            }

        }

        for (i = 0; i < len; i++) {

            if (chkStr.indexOf(mail.charAt(i)) < 0) {

                return false;

            }

        }

        return true;

    }

    /**
     * 从源字符串中删除固定的字符串
     *
     * @param   source    input string
     * @param   rc     removed char
     *
     * @return  string
     */

    public static String deleteStr(String source, String rc) {
        if (source == null) {
            return null;
        }
        if (isSubstring(source, rc)) {
            return replaceStr(source, rc, "");
        }
        return source;
    }

    /**
     * 检测传入的字符串是否是电话号码或手机号码
     * @param phone
     * @return
     */
    public static boolean chkPhone(String phone) {

        int i = phone.indexOf("--");

        int len = phone.length();

        if (i >= 0) {

            return false;

        }

        i = phone.indexOf("-");

        if ( (i == 0) || (i == len - 1)) {

            return false;

        }

        else if (i > 0) {

            i = phone.lastIndexOf("-");

            if (i == len - 1) {

                return false;

            }

            phone = deleteStr(phone, "-");

        }

        if (!isNumber(phone)) {

            return false;

        }

        return true;

    }

    /**
     * 转换传入的字符串为查询SQL语句格式,具体转换字符为:<br>
     * ' ==> ''  <br>
     * _ ==> ~_  <br>
     * % ==> ~%  <br>
     * ~ ==> ~~  <br>
     * @param sIn
     * @return
     */
    public static String toDBSelStr(String sIn)

    {

        if (sIn == null) {

            return sIn;

        }
        String sOut = sIn;

        sOut = replaceStr(sOut, "~", "~~");

        sOut = replaceStr(sOut, "%", "~%");

        sOut = replaceStr(sOut, "_", "~_");

        sOut = replaceStr(sOut, "'", "''");

        return sOut;

    }

    /**
     * 转换传入的字符串为插入SQL语句格式,具体转换字符为:<br>
     * ' ==> ''  <br>
     * \" ==> \uFFFD  <br>
     * @param sIn
     * @return
     */
    public static String toDBInsStr(String sIn)

    {

        return toDBInsStr(sIn, -1);

    }

    /**
     * 转换传入的字符串为插入SQL语句格式,字节长度超过nMaxLen则截取nMaxLen长返回,具体转换字符为:<br>
     * ' ==> '' <br>
     * \" ==> \uFFFD <br>
     * @param sIn
     * @param nMaxLen
     * @return
     */
    public static String toDBInsStr(String sIn, int nMaxLen)

    {

        if (sIn == null) {

            return sIn;

        }

        String sOut = sIn;

        sOut = replaceStr(sOut, "\"", "\uFFFD");

        if (nMaxLen != -1) {

            byte[] bs = sOut.getBytes();

            if (bs.length > nMaxLen) {

                sOut = new String(bs, 0, nMaxLen);

            }

        }

        sOut = replaceStr(sOut, "'", "''");

        return sOut;

    }

    /**
     * 将输入串转换成HTML的可输出串,主要替换以下字符:<br>
     * "<" ==> "&lt;"  <br>
     * ">" ==> "&gt;"  <br>
     * @param sIn 输入串
     * @return HTML输出串
     */
    public static String toHTMLOutStr(String sIn)

    {

        if (sIn == null) {

            return sIn;

        }

        String sOut = sIn;

        sOut = replaceStr(sOut, "<", "&lt;");

        sOut = replaceStr(sOut, ">", "&gt;");

        return sOut;

    }

    private static String sepReturn = "\r\n";

    /**
     * 转换输入串回车换行为HTML的回车符号<br>
     * @param sIn 输入串
     * @return
     */
    public static String toHTMLRtnStr(String sIn) {

        if (sIn == null) {

            return sIn;

        }

        String sOut = sIn;

        sOut = replaceStr(sOut, sepReturn, "<br>");

        return sOut;

    }

    /**
     * 将输入串进行XML编码,主要替换的字符如下:<br>
     * "&" ==> "&amp;"  <br>
     * "<" ==> "&lt;"  <br>
     * ">" ==> "&gt;"  <br>
     * """ ==> "&quot;"  <br>
     * "'" ==> "&apos;"  <br>
     * @param s_string 输入串
     * @return XML编码后的串
     */
    public static String toEncodedXml(String s_string) {
        if (s_string == null) {
            return "";
        }
        s_string = replaceStr(s_string, "&", "&amp;");
        s_string = replaceStr(s_string, "<", "&lt;");
        s_string = replaceStr(s_string, ">", "&gt;");
        s_string = replaceStr(s_string, "\"", "&quot;");
        s_string = replaceStr(s_string, "'", "&apos;");
        return s_string;
    }

    /**
     * 将输入串进行XML解码,主要替换的字符如下:<br>
     * "&lt;" ==> "<"  <br>
     * "&gt;" ==> ">"  <br>
     * "&quot;" ==> """  <br>
     * "&apos;" ==> "'"  <br>
     * "&amp;" ==> "&"  <br>
     * @param s 输入串
     * @return  XML解码后的串
     */
    public static String toUnEncodedXml(String s) {
        if (s == null) {
            return "";
        }

        s = StringConverter.replaceStr(s, "&lt;", "<");
        s = StringConverter.replaceStr(s, "&gt;", ">");
        s = StringConverter.replaceStr(s, "&quot;", "\"");
        s = StringConverter.replaceStr(s, "&apos;", "'");
        s = StringConverter.replaceStr(s, "&amp;", "&");
        return s;
    }

    /**
     * 将输入串从一种字符集转换到另外一种字符集
     * @param s 输入串
     * @param character_s 源字符集
     * @param character_d 目标字符集
     * @return 转换后的串
     */
    public static String convertString(String s, String character_s,
                                       String character_d) {
        s = nvl(s);
        String s_unicode = "";
        try {
            s = s.trim();
            byte[] bytes = s.getBytes(character_s); // 源字符集
            s_unicode = new String(bytes, character_d); // 目标字符集
        }
        catch (UnsupportedEncodingException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return s_unicode;
    }

    /**
     * 将输入串从ISO8859_1字符集转换到GB2312字符集
     * @param s
     * @return
     */
    public static String ISO8859_1ToGB2312(String s) { //用于中文处理
        return convertString(s, "ISO8859_1", "GB2312");
    }

    /**
     * 将输入串从GB2312字符集转换到ISO8859_1字符集
     * @param s
     * @return
     */
    public static String GB2312ToISO8859_1(String s) {
        return convertString(s, "GB2312", "ISO8859_1");
    }

    /**
     * 将输入串从GB2312字符集转换到UTF8字符集
     * @param s
     * @return
     */
    public static String GB2312ToUTF8(String s) { //用于中文处理
        return convertString(s, "GB2312", "UTF8");
    }

    /**
     * 将输入串从UTF8字符集转换到GB2312字符集
     * @param s
     * @return
     */
    public static String UTF8ToGB2312(String s) { //用于中文处理
        return convertString(s, "UTF8", "GB2312");
    }

    /**
     * 看字符C是否为空格" "或"\t"
     * @param c
     * @return
     */
    public static boolean isSpace(char c)

    {

        return (c == ' ' || c == '\t');

    }

    /**
     * 看字符串是否为空串(多个空格或\t组成的"    "或"   \t  "),如果传递的是null返回false
     * @param s
     * @return
     */
    public static boolean isSpace(String s)

    {

        if (s == null) {
            return false;
        }

        for (int i = 0; i < s.length(); i++) {

            if (!isSpace(s.charAt(i))) {

                return false;

            }

        }

        return true;

    }

    /**
     * 去掉字符串的首尾空格,如果传递的是null则返回null
     * @param s
     * @return
     */
    public static String trim(String s)

    {

        if (s == null) {
            return null;
        }

        int begin, end;

        for (begin = 0; (begin < s.length())

                && isSpace(s.charAt(begin)); begin++) {
            ;
        }

        for (end = s.length() - 1; (end >= 0) && isSpace(s.charAt(end)); end--) {
            ;
        }

        if (end < begin) {

            return "";

        }

        return s.substring(begin, end + 1);

    }

    /**
     * 转换类似串"243,434,343"为大整数243434343
     * @param input
     * @return
     */
    public static java.math.BigDecimal unFormatNum(String input) {

        if (input == null || input.equals("")) {

            return new java.math.BigDecimal(0);

        }

        try {

            return new java.math.BigDecimal(deleteStr(input, ","));

        }

        catch (Exception e) {

            return new java.math.BigDecimal( -1);

        }

    }

    // End of update


    /**
     * 将数值串转换成金额表示法(如:9999999 -> 9,999,999)
     * @param input0
     * @return
     */
    public static String formatNum(String input0) {

        if (input0 == null) {
            return null;
        }

        // we want a "" return if input is ""

        ///if(input.trim().length()== 0)return null;

        if (input0.trim().length() == 0) {
            return "";
        }

        String input = input0;

        boolean neg = false;

        if (input.startsWith("-")) {

            neg = true;

            input = input0.substring(1);

        }

        int point = (input.indexOf(".") > 0) ? input.indexOf(".") : input.length();

        StringBuffer result = new StringBuffer();

        for (int i = 0; i < point - 1; i++) {

            if ( (point - i - 1) % 3 == 0) {

                result.append(input.charAt(i)).append(",");

            }
            else {

                result.append(input.charAt(i));

            }
        }
        result.append(input.substring(point - 1));

        // modified maxiang 2001/06/22 for [\uFFFD20010621-01]

        //return result.toString();

        return neg ? "-" + result.toString() : result.toString();

        // end modify maxiang 2001/06/22

        // End of update

    }

    /**
     * 将数值转换成金额表示法(如:9999999 -> 9,999,999)
     * @param src
     * @return
     */
    public static String formatNum(java.math.BigDecimal src) {

        return formatNum(src.toString());

    }

    /**
     * 将输入串进行以下替换后返回:<br>
     * "&" ==> "&amp;"  <br>
     * "<" ==> "&lt;"  <br>
     * ">" ==> "&gt;"  <br>
     * """ ==> "&quot;"  <br>
     * @param value
     * @return
     */
    public static String filterForHtml(String value) {

        if (value == null) {

            return (null);

        }

        StringBuffer result = new StringBuffer();

        value = value.trim();

        for (int i = 0; i < value.length(); i++) {

            char ch = value.charAt(i);

            if (ch == '<') {

                result.append("&lt;");

            }
            else if (ch == '>') {

                result.append("&gt;");

            }
            else if (ch == '&') {

                result.append("&amp;");

            }
            else if (ch == '"') {

                result.append("&quot;");

            }
            else {

                result.append(ch);

            }
        }
        String temp = replaceStr(result.toString(),"\r\n","<br>");
        temp = replaceStr(temp,"\r","<br>");
        temp = replaceStr(temp,"\n","<br>");
        return temp;

    }

    /**
     * 将输入串替换以下字符返回:<br>
     * '\'  ==> "\\"  <br>
     * '\''  ==> "\\'"  <br>
     * '"'  ==> "\\\""  <br>
     * "\r\n"  ==> "\\r\\n"  <br>
     * @param value
     * @return
     */
    public static String filterForJS(String value) {

        if (value == null) {

            return (null);

        }

        StringBuffer result = new StringBuffer();

        value = value.trim();

        for (int i = 0; i < value.length(); i++) {

            char ch = value.charAt(i);

            if (ch == '\\') {
                result.append("\\\\");
            }else if (ch == '\'') {

                result.append("\\'");

            }
            else if (ch == '"') {

                result.append("\\\"");

            }
            else {

                result.append(ch);

            }
        }
        String temp = replaceStr(result.toString(),"\r","\\r");
        temp = replaceStr(temp,"\n","\\n");
        return temp;

    }

    /**
     * 将字符串转换成大整数对象
     * @param src
     * @return
     */

    public static java.math.BigDecimal toBigDecimal(String src) {

        if (src == null) {

            return new java.math.BigDecimal(0);
        }

        else {

            return new java.math.BigDecimal(src);
        }

    }

    /**
     * 格式化CLAUSE,取掉换行符/回车符/TAB符号为空
     * @param clause
     * @return
     */
    public static String deleteRNT(String clause) {
        String result = StringConverter.replaceStr(clause, "\r", "");
        result = StringConverter.replaceStr(result, "\n", "");
        result = StringConverter.replaceStr(result, "\t", "");
        return result;
    }

    public static final String LINE_SEP = System.getProperty("line.separator");


    /**
     * 将STRING转换成为boolean类型,如果传递的值为1或true则返回true,否则返回false
     * @param s
     * @return
     */
    public static boolean getBoolean(String s) {
        if (StringConverter.isNullString(s)) {
            return false;
        }
        return ( (s.trim().equalsIgnoreCase("1")) ||
                (s.trim().equalsIgnoreCase("true")));
    }
}
