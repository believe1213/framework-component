package com.minister.component.utils;

import cn.hutool.core.text.StrPool;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * MathUtil
 * {@link java.math.RoundingMode}
 * (UP : 远离0方向舍入。向绝对值最大的方向舍入，只要舍弃位非0即进位)
 * (DOWN : 趋向0方向舍入。向绝对值最小的方向舍入，所有的位都要舍弃，不存在进位)
 * (CEILING : 向正无穷方向舍入。向正最大方向靠拢。若是正数舍入行为类似ROUND_UP,若是负数舍入行为类似ROUND_DOWN)
 * (FLOOR : 向负无穷方向舍入，向负最大方向靠拢。若是正数舍入行为类似ROUND_DOWN，若是负数舍入行为类似ROUND_UP)
 * (HALF_UP : 最近数字舍入(5进))
 * (HALF_DOWN : 最近数字舍入(5舍))
 * (HAIL_EVEN : 银行家舍入法)
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 22:06
 */
public class MathUtil {

    private MathUtil() {
    }

    public static int compare(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2);
    }

    /**
     * 数值校验
     *
     * @param value    校验值
     * @param min      最小值(为null时不校验)
     * @param minEqual 是否允许等于最小值（默认：true）
     * @param max      最大值(为null时不校验)
     * @param maxEqual 是否允许等于最大值（为null时不校验, 默认：true）
     * @param maxScale 小数点最大位数
     * @return 校验结果(失败返回true)
     */
    public static boolean check(BigDecimal value, BigDecimal min, Boolean minEqual,
                                BigDecimal max, Boolean maxEqual, Integer maxScale) {
        if (Objects.isNull(value)) {
            return false;
        }
        // 入参修正
        minEqual = Objects.isNull(minEqual) || minEqual;
        maxEqual = Objects.isNull(maxEqual) || maxEqual;
        // 最小值校验
        boolean error = false;
        if (Objects.nonNull(min)) {
            error = minEqual ? (value.compareTo(min) < 0) : (value.compareTo(min) <= 0);
            if (error) {
                return true;
            }
        }
        // 最大值校验
        if (Objects.nonNull(max)) {
            error = maxEqual ? (value.compareTo(max) > 0) : (value.compareTo(max) >= 0);
            if (error) {
                return true;
            }
        }
        // 小数点位数校验
        if (Objects.nonNull(maxScale)) {
            String[] str = StrUtil.splitToArray(value.toPlainString(), StrPool.DOT);
            if (str.length > 1) {
                error = str[1].length() > maxScale;
            }
            if (error) {
                return true;
            }
        }

        return false;
    }

    /**
     * 数值校验
     *
     * @param value    校验值
     * @param min      最小值(为null时不校验)
     * @param minEqual 是否允许等于最小值（默认：true）
     * @param max      最大值(为null时不校验)
     * @param maxEqual 是否允许等于最大值（为null时不校验, 默认：true）
     * @return 校验结果(失败返回true)
     */
    public static boolean check(Integer value, Integer min, Boolean minEqual,
                                Integer max, Boolean maxEqual) {
        if (Objects.isNull(value)) {
            return false;
        }
        // 入参修正
        minEqual = Objects.isNull(minEqual) || minEqual;
        maxEqual = Objects.isNull(maxEqual) || maxEqual;
        // 最小值校验
        boolean error;
        if (Objects.nonNull(min)) {
            error = minEqual ? (value.compareTo(min) < 0) : (value.compareTo(min) <= 0);
            if (error) {
                return true;
            }
        }
        // 最大值校验
        if (Objects.nonNull(max)) {
            error = maxEqual ? (value.compareTo(max) > 0) : (value.compareTo(max) >= 0);
            if (error) {
                return true;
            }
        }

        return false;
    }

}
