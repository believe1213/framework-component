package com.minister.component.utils;

import java.util.regex.Pattern;

/**
 * 密码校验
 *
 * @author QIUCHANGQING620
 * @date 2020-07-22 18:23
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * 非法参数检测
     * <p>
     * 只允许含有
     * 1. (0-9)(A-Z)(a-z)
     * 2. `-=[]\;',./
     * 3. ~!@#$%^&*()_+
     * 4. {}|:"<>?
     */
    public static final Pattern ILLEGAL_PATTERN = Pattern.compile("^[0-9A-Za-z`\\-=\\[\\]\\\\;',./~!@#$%^&*()_+{}|:\"<>?]+$");

    /**
     * 复杂度检测(不包含长度)
     * <p>
     * 至少包含有
     * 1. (0-9)
     * 2. A-Z
     * 3. a-z
     * 4. -=[]\;',./~!@#$%^&*()_+{}|:"<>?
     * </p>
     */
    public static final Pattern WEAK_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[`\\-=\\[\\]\\\\;',./~!@#$%^&*()_+{}|:\"<>?]).+$");

    /**
     * 密码校验
     *
     * @param password 密码
     * @return 校验状态(异常为true)
     */
    public static boolean check(String password) {
        return check(password, 8, 20, 3);
    }

    /**
     * 密码校验
     * <p>
     * 1. 非法校验(非空 + ILLEGAL_PATTERN)
     * 2. 长度校验(8-20)
     * 3. 复杂度校验(WEAK_PATTERN)
     * 4. 连续字符校验(3)
     * </p>
     *
     * @param password   密码
     * @param min        最小长度
     * @param max        最大长度
     * @param continuous 连续字符长度
     * @return 校验状态(异常为true)
     */
    public static boolean check(String password, int min, int max, int continuous) {
        return isIllegal(password) || password.length() > max || isWeak(password, min) || hasContinuous(password, continuous);
    }

    /**
     * 非法字符串检测
     *
     * @param password 密码
     * @return 校验状态
     */
    public static boolean isIllegal(String password) {
        return password == null || !ILLEGAL_PATTERN.matcher(password).matches();
    }

    /**
     * 复杂度检测
     *
     * @param password 密码
     * @return 校验状态
     */
    public static boolean isWeak(String password, int min) {
        return password.length() < min || !WEAK_PATTERN.matcher(password).matches();
    }

    /**
     * 连续字符检测
     *
     * @param password 密码
     * @param len      允许连续字符长度
     * @return 检测状态
     */
    public static boolean hasContinuous(String password, int len) {
        // 连续长度
        int continuous = 0;
        // 当前字符
        char current = 0;
        // 前一个字符
        for (char c : password.toCharArray()) {
            if (current == c) {
                continuous++;
            } else {
                continuous = 1;
                current = c;
            }
            if (continuous >= len) {
                return true;
            }
        }
        return false;
    }

}
