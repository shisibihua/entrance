package com.honghe.entrance.common.util;

import com.honghe.security.MD5;

import java.util.Random;

/**
 * Created by wj on 2014-10-13.
 */
public final class SaltRandom {


    static String ssource = "0123456789";
    static char[] src = ssource.toCharArray();
    static Random r = new Random();

    private SaltRandom() {

    }

    private static String randString(int length) {
        char[] buf = new char[length];
        int rnd;
        for (int i = 0; i < length; i++) {
            rnd = Math.abs(r.nextInt()) % src.length;

            buf[i] = src[rnd];
        }
        return new String(buf);
    }

    public static String runVerifyCode(int i) {
        return randString(i);
    }


    /**
     * 生成默认密码
     *
     * @param salt 盐值
     * @return 返回加密密码
     */
    public static String createDefaultPassword(String salt) {
        return createPassword("123456", salt);
    }

    /**
     * 加密密码
     *
     * @param password 明码
     * @return 加密后的密码
     */
    public static String createPassword(String password) {
        return createPassword(password, runVerifyCode(6));
    }

    /**
     * 生成加密密码
     *
     * @param password 明码
     * @param salt     加密盐值
     * @return 加密后的密码
     */
    public static String createPassword(String password, String salt) {
        return new MD5().encrypt(password + (salt == null ? runVerifyCode(6) : salt));
    }
}
