package com.honghe.entrance.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Description:</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 北京鸿合智能系统股份有限公司</p>
 *
 * @author hthwx
 * @date 2019/2/23
 */
public class DataUtil {
    /**
     * 检查是否是中文
     *
     * @param str 字符串
     * @return true:是，false：不是
     */
    public static boolean isChinese(String str) {
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        char c[] = str.toCharArray();
        for(int i=0;i<c.length;i++){
            Matcher matcher = pattern.matcher(String.valueOf(c[i]));
            if(!matcher.matches()){
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否是英文
     *
     * @param str 字符串
     * @return true:是，false：不是
     */
    public static boolean isEnglish(String str) {
        return str.matches("[a-zA-Z]+");//true:全文英文
    }

    /**
     * 检查是否是数字
     *
     * @param str 字符串
     * @return true:是，false：不是
     */
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 检查是否是正整数
     *
     * @param str 字符串
     * @return true:是，false：不是
     */
    public static boolean isPureDigital(String str) {

        String regEx1 = "\\d+";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(str);
        return m.matches();
    }

    /**
     * 检查是否是以英文和数字组成的字符串
     *
     * @param str 字符串
     * @return true:是，false：不是
     */
    public static boolean isEnglishNumber(String str) {
        // s.matches("[0-9a-zA-Z]*")
        return str.matches("[a-zA-Z0-9]+"); //判断英文和数字
    }

    /**
     * 判断多个字符串
     * 检查字符是否为 null || ""
     *
     * @author wuxiao
     * @date 2018/07/05
     * @param type 0：验证是否都为空或null 1：验证是否都为非空或非null
     * @param value 需要验证的多个字符，以英文逗号分隔
     * @return
     */
    public static boolean checkStringIsNull(int type, String... value){
        int count = 0;
        for(int i=0;i<value.length;i++){
            //遍历字符数组所有的参数，发现某个为 null 或者 "" ,则跳出
            if(0 == type) {
                if (StringUtils.isNotBlank(value[i])) {
                    return false;
                }
            }else {
                if (StringUtils.isBlank(value[i])) {
                    return false;
                }
            }
            count++;
        }
        return count == value.length;
    }

    /**
     * Date 转 string
     * @param date
     * @return
     */
    public static String dateToString(Date date){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
}
