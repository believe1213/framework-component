package com.minister.component.utils;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

import static com.minister.component.utils.constants.Constants.ASTERISK;

/**
 * 脱敏Util
 *
 * @author QIUCHANGQING620
 * @date 2020-04-25 18:35
 */
public class MaskingUtil {

    private MaskingUtil() {
    }

    /**
     * 数据脱敏（前三后四）
     *
     * @param data 源数据
     * @return 脱敏后的数据
     */
    public static String masking34(String data) {
        return masking(data, 3, 4);
    }

    /**
     * 数据脱敏（前一后一）
     *
     * @param data 源数据
     * @return 脱敏后的数据
     */
    public static String masking11(String data) {
        return masking(data, 1, 1);
    }

    /**
     * 数据脱敏
     *
     * @param data   源数据
     * @param prefix 保留前缀位数
     * @param suffix 保留后缀位数
     * @return 脱敏后的数据
     */
    public static String masking(String data, int prefix, int suffix) {
        if (StringUtils.isBlank(data) || prefix < 0 || suffix < 0) {
            return data;
        }
        int len = data.length();
        if ((prefix + suffix) > len) {
            return data;
        }
        return StrUtil.hide(data, prefix, len - suffix);
    }

    /**
     * 对姓名脱敏
     *
     * @param realName 姓名
     * @return 脱敏后姓名
     */
    public static String realName(String realName) {
        if (StringUtils.isBlank(realName)) {
            return realName;
        }
        int len = realName.length();
        if (len == 1) {
            return realName;
        } else if (len == 2) {
            return "*".concat(realName.substring(1, 2));
        } else {
            return realName.substring(0, 1) + String.join(StringUtils.EMPTY, Collections.nCopies(len - 2, ASTERISK)) + realName.substring(len - 1, len);
        }
    }

}
