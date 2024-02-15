package com.minister.component.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 校验Util
 *
 * @author QIUCHANGQING620
 * @date 2020-04-25 19:38
 */
public class ValidatorUtil {

    private ValidatorUtil() {
    }


    private static final String REGEX_EMAIL = "^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$";

    public static boolean email(String email) {
        return StringUtils.isNotBlank(email) && Pattern.matches(REGEX_EMAIL, email);
    }

}
