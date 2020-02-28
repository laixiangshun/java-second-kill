package com.java.secondskill.utils;

import com.java.secondskill.constants.Common;
import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {

    private static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    /**
     * 第一次MD5加密，用于网络传输
     */
    public static String inputPassToFormPass(String inputPass) {
        //避免在网络传输被截取然后反推出密码，所以在md5加密前先打乱密码
        String str = "" + Common.salt.charAt(0) + Common.salt.charAt(2) + inputPass + Common.salt.charAt(5) + Common.salt.charAt(4);
        return md5(str);
    }

    /**
     * 第二次MD5加密，用于存储到数据库
     */
    public static String formPassToDBPass(String formPass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //合并
    public static String inputPassToDbPass(String input, String saltDB) {
        String formPass = inputPassToFormPass(input);
        return formPassToDBPass(formPass, saltDB);
    }

    public static void main(String[] args) {
        System.out.println(inputPassToFormPass("123456"));

    }

}
