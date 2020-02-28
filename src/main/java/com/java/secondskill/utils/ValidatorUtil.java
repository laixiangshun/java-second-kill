package com.java.secondskill.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验工具类
 */
public class ValidatorUtil {

    private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    /**
     * 判断是否是手机号
     */
    public static boolean isMobile(String phoneNum) {
        if (StringUtils.isBlank(phoneNum)) {
            return false;
        }
        Matcher matcher = mobile_pattern.matcher(phoneNum);
        return matcher.matches();
    }
}
