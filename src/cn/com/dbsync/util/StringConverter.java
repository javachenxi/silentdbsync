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
     * �ж��ַ����Ƿ�Ϊnull���
     * @param str
     * @return
     */
    public static boolean isNullString(String str) {

        return (str == null || str.equals(""));

    }

    /**
     * ���ع̶������ո�Ŀ��ַ��� (i.e. a String which consists of only blanks)
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
     * ���ع̶�����0�Ŀ��ַ���
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
     * ��Դ�ַ�������times���󷵻�,���Դ�ַ���Ϊnull������տո�Դ�
     * @param source Դ�ַ���
     * @param times ���ƴ���
     * @return ���ƺ���ַ���
     */
    public static String getCopy(String source, int times) {
        if (StringConverter.isNullString(source)) {
            return StringConverter.getSpaces(times); //�����NULL��"",�򷵻�times����
        }
        StringBuffer result = new StringBuffer( (source.length()) * times);
        for (int i = 0; i < times; i++) {
            result.append(source);
        }
        return result.toString();
    }

    /**
     * ��Դ�ַ�������ĳһ���ַ����̶�����(���S����С��len,��s����str��len���ֽ�,���򷵻�len����s,Ҫ��str.length=1)
     * @param s Դ�ַ���
     * @param len ����
     * @param str ����ַ�(ֻ����һ���ַ�)
     * @return len���Ĵ�
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
     * ��ȡԴ�ַ����ӿ�ʼλ����̶��ֽڳ��ȵĴ�(��Ҫ���ڰ������ְ��ֽ�λ�úͳ��Ƚ�ȡ�ַ������,����substring("�Ұ����", 5)����ֵΪ"�Ұ� ")
     * @param s Դ�ַ���
     * @param bytelength ����,�����ֽڵ�λ�������ַ�
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
            else if (charlength > bytelength) { //��˫�ֽ�ʱ���п��ܵ��ⲽ����һ��" "
                return s.substring(0, i) + " ";
            }
            ;
        }
        return s;
    }

    /**
     * ��Դ�ַ�������ĳһ���ַ����̶�����(���S����С��len,��s����str��len��,����,����len���ֽڵ�s,Ҫ��str.length=1)
     * @param s Դ�ַ���
     * @param len ����
     * @param str ����ַ�(ֻ����һ���ַ�)
     * @return len���Ĵ�
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
     * ��Դ�ַ�������ո񵽹̶�����,����Դ�ַ�����ִ��trim����(���S����С��len,��s����ո�len���ֽ�,���򷵻�len����s)
     * @param s Դ�ַ���
     * @param len ����
     * @return len���Ĵ�
     */

    static public String padLeftSpace(String s, int len)

    {

        return padLeftSpace(s, len, false);

    }

    /**
     * ��Դ�ַ�������ո񵽹̶�����,����Դ�ַ������ݴ��ݵ�trim���Ծ����Ƿ���ȡ�������ҵĿո�(���S����С��len,��s����ո�len���ֽ�,���򷵻�len����s)
     * @param s Դ�ַ���
     * @param len ����
     * @param trim �Ƿ�Ҫ��ȡ�������ҵĿո�
     * @return len���Ĵ�
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
     * ��Դ�ַ�������ո񵽹̶�����,����Դ�ַ�����ִ��trim����(���S����С��len,��s����ո�len���ֽ�,���򷵻�len����s)
     * @param s Դ�ַ���
     * @param len ����
     * @return len���Ĵ�
     */

    static public String padRightSpace(String s, int len)

    {

        return padRightSpace(s, len, false);

    }

    /**
     * ��Դ�ַ�������ո񵽹̶�����,����Դ�ַ������ݴ��ݵ�trim���Ծ����Ƿ���ȡ�������ҵĿո�(���S����С��len,��s����ո�len���ֽ�,���򷵻�len����s)
     * @param s Դ�ַ���
     * @param len ����
     * @param trim �Ƿ�Ҫ��ȡ�������ҵĿո�
     * @return len���Ĵ�
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
     * ��SUB���Ƿ���SOURCE�����Ӵ�
     * @param source ����Ϊnull
     * @param sub ����Ϊnull
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
     * ��Դ�ַ����е�oldString��newString���滻��,���oldStringΪnull����򷵻�Դ�ַ���,���newStringΪnull����Ϊ��
     * @param source Դ�ַ���
     * @param oldString ���滻���ַ���
     * @param newString �µ��ַ���
     * @return �滻����ַ���
     */
    public static String replaceStr(String source, String oldString,
                                    String newString) { //edited by lhy 201105262223 �Ե�û���ҵ�Դ��ʱ���滻Ч�ʽ��������,�ٶ����5��
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
     * ��Դ�ַ����еĿ����ַ���TAB�ַ�ת���ɿո�
     * @param str Դ�ַ���
     * @return ת������ַ���
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
     * ��Դ�ַ����Ƿ�������
     * @param str Դ�ַ���
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
     * ��Դ�ַ����Ƿ������ֻ�Ӣ����ĸ
     * @param str Դ�ַ���
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
     * ������ݵ�Դ�ַ���Ϊnull�򷵻ؿ�"",���򷵻ر���
     * @param sIn Դ�ַ���
     * @return
     */
    public static String nvl(String sIn) {

        return (sIn == null) ? "" : sIn;

    }


    /**
     * ������ݵ�Դ�ַ���Ϊnull�򷵻�ָ���Ĵ�,���򷵻ر���
     * @param sIn Դ�ַ���
     * @param sDef Դ�ַ���Ϊnullʱ�򷵻ص�ָ����
     * @return
     */
    public static String nvl(String sIn, String sDef) {

        return (sIn == null) ? sDef : sIn;

    }

    /**
     * ��⴫����ַ����Ƿ���EMAIL
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
     * ��Դ�ַ�����ɾ���̶����ַ���
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
     * ��⴫����ַ����Ƿ��ǵ绰������ֻ�����
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
     * ת��������ַ���Ϊ��ѯSQL����ʽ,����ת���ַ�Ϊ:<br>
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
     * ת��������ַ���Ϊ����SQL����ʽ,����ת���ַ�Ϊ:<br>
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
     * ת��������ַ���Ϊ����SQL����ʽ,�ֽڳ��ȳ���nMaxLen���ȡnMaxLen������,����ת���ַ�Ϊ:<br>
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
     * �����봮ת����HTML�Ŀ������,��Ҫ�滻�����ַ�:<br>
     * "<" ==> "&lt;"  <br>
     * ">" ==> "&gt;"  <br>
     * @param sIn ���봮
     * @return HTML�����
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
     * ת�����봮�س�����ΪHTML�Ļس�����<br>
     * @param sIn ���봮
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
     * �����봮����XML����,��Ҫ�滻���ַ�����:<br>
     * "&" ==> "&amp;"  <br>
     * "<" ==> "&lt;"  <br>
     * ">" ==> "&gt;"  <br>
     * """ ==> "&quot;"  <br>
     * "'" ==> "&apos;"  <br>
     * @param s_string ���봮
     * @return XML�����Ĵ�
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
     * �����봮����XML����,��Ҫ�滻���ַ�����:<br>
     * "&lt;" ==> "<"  <br>
     * "&gt;" ==> ">"  <br>
     * "&quot;" ==> """  <br>
     * "&apos;" ==> "'"  <br>
     * "&amp;" ==> "&"  <br>
     * @param s ���봮
     * @return  XML�����Ĵ�
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
     * �����봮��һ���ַ���ת��������һ���ַ���
     * @param s ���봮
     * @param character_s Դ�ַ���
     * @param character_d Ŀ���ַ���
     * @return ת����Ĵ�
     */
    public static String convertString(String s, String character_s,
                                       String character_d) {
        s = nvl(s);
        String s_unicode = "";
        try {
            s = s.trim();
            byte[] bytes = s.getBytes(character_s); // Դ�ַ���
            s_unicode = new String(bytes, character_d); // Ŀ���ַ���
        }
        catch (UnsupportedEncodingException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return s_unicode;
    }

    /**
     * �����봮��ISO8859_1�ַ���ת����GB2312�ַ���
     * @param s
     * @return
     */
    public static String ISO8859_1ToGB2312(String s) { //�������Ĵ���
        return convertString(s, "ISO8859_1", "GB2312");
    }

    /**
     * �����봮��GB2312�ַ���ת����ISO8859_1�ַ���
     * @param s
     * @return
     */
    public static String GB2312ToISO8859_1(String s) {
        return convertString(s, "GB2312", "ISO8859_1");
    }

    /**
     * �����봮��GB2312�ַ���ת����UTF8�ַ���
     * @param s
     * @return
     */
    public static String GB2312ToUTF8(String s) { //�������Ĵ���
        return convertString(s, "GB2312", "UTF8");
    }

    /**
     * �����봮��UTF8�ַ���ת����GB2312�ַ���
     * @param s
     * @return
     */
    public static String UTF8ToGB2312(String s) { //�������Ĵ���
        return convertString(s, "UTF8", "GB2312");
    }

    /**
     * ���ַ�C�Ƿ�Ϊ�ո�" "��"\t"
     * @param c
     * @return
     */
    public static boolean isSpace(char c)

    {

        return (c == ' ' || c == '\t');

    }

    /**
     * ���ַ����Ƿ�Ϊ�մ�(����ո��\t��ɵ�"    "��"   \t  "),������ݵ���null����false
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
     * ȥ���ַ�������β�ո�,������ݵ���null�򷵻�null
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
     * ת�����ƴ�"243,434,343"Ϊ������243434343
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
     * ����ֵ��ת���ɽ���ʾ��(��:9999999 -> 9,999,999)
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
     * ����ֵת���ɽ���ʾ��(��:9999999 -> 9,999,999)
     * @param src
     * @return
     */
    public static String formatNum(java.math.BigDecimal src) {

        return formatNum(src.toString());

    }

    /**
     * �����봮���������滻�󷵻�:<br>
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
     * �����봮�滻�����ַ�����:<br>
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
     * ���ַ���ת���ɴ���������
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
     * ��ʽ��CLAUSE,ȡ�����з�/�س���/TAB����Ϊ��
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
     * ��STRINGת����Ϊboolean����,������ݵ�ֵΪ1��true�򷵻�true,���򷵻�false
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
