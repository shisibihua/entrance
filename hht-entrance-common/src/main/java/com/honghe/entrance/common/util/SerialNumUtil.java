package com.honghe.entrance.common.util;

import com.alibaba.druid.util.StringUtils;

import java.text.DecimalFormat;


/**
 * 生成对应的设备编号
 *
 * @author lyx
 * @create 2017-01-07 11:18
 **/
public class SerialNumUtil {

    private SerialNumUtil() {
    }

    private final static String NUM_CODE = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 生成一个 tags+年份+五位字符串的单号
     ** @param oldNumber //从数据库查询出的最大编号 若为NUll则表示从00001开始
     * @return
     */
    public static String getNumber(String tag,String oldNumber) {
        DecimalFormat df = new DecimalFormat("00000");
        String orderNo;
        if (!StringUtils.isEmpty(oldNumber)&&oldNumber.contains(tag)) {
            // 截取标识字符串最后编码位，结果例:00001
            String uidEnd = oldNumber.substring(tag.length(), oldNumber.length());

            char[] uidEndArray = uidEnd.toCharArray();

            //是否进位标识符。进位-true，不进位-false
            boolean b_carry = false;
            //从个位开始计算

            b_carry = codeCarryCount(uidEndArray, uidEndArray.length-1);

            orderNo = String.valueOf(uidEndArray);

            if (b_carry)
            {//默认五位数不够使用，自动增加一位
                orderNo = "1" + orderNo;
            }

        } else {
            orderNo = "00001";
        }

        orderNo = tag + orderNo;

        return orderNo;
    }

    /**
     * 设备编码计算
     * 低位满最后一个字符，将进位
     * @param codeArray 设备编码字符数据
     * @param index 设备编码当前位索引值
     * @return
     */
    private static boolean codeCarryCount(char[] codeArray, int index)
    {
        //进位是否超出最高位，超出-true，没超出-false
        boolean b_isOutSize = false;

        if (index < 0)
        {//当前位索引进位超出最高位
            return true;
        }
        if (index >= codeArray.length)
        {//当前位索引数组越界
            return false;
        }

        char ch = codeArray[index];
        int numcode_index = NUM_CODE.lastIndexOf(ch);
        numcode_index = (numcode_index+1)%NUM_CODE.length();
        ch = NUM_CODE.charAt(numcode_index);

        if (numcode_index == 0)
        {
            b_isOutSize = codeCarryCount(codeArray, index-1);
        }

        codeArray[index] = ch;

        return b_isOutSize;
    }

    /***
     * 去掉字符串中的位数
     *
     * @param str
     * @param start
     * @return
     */
    public static String subStr(String str, int start) {
        if (str == null || "".equals(str) || str.length() == 0)
            return "";
        if (start < str.length()) {
            return str.substring(start);
        } else {
            return "";
        }
    }
}
