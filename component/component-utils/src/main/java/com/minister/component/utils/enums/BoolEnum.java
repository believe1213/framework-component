package com.minister.component.utils.enums;

import com.minister.component.utils.constants.Constants;

/**
 * BoolEnum(非枚举类)
 *
 * @author QIUCHANGQING620
 * @date 2020-9-2 16:46
 */
public class BoolEnum {

    public static final String TRUE_STR = Constants.STR_ONE;

    public static final String FALSE_STR = Constants.STR_ZERO;

    public static final Integer TRUE_INT = Constants.INT_ONE;

    public static final Integer FALSE_INT = Constants.INT_ZERO;

    public static Boolean parse(String code) {
        if (TRUE_STR.equals(code)) {
            return true;
        } else if (FALSE_STR.equals(code)) {
            return false;
        } else {
            return null;
        }
    }

    public static String getStrCode(Boolean boo) {
        return boo == null ? null : (boo ? TRUE_STR : FALSE_STR);
    }

    public static Boolean parse(int code) {
        if (TRUE_INT.equals(code)) {
            return true;
        } else if (FALSE_INT.equals(code)) {
            return false;
        } else {
            return null;
        }
    }

    public static Integer getIntCode(Boolean boo) {
        return boo == null ? null : (boo ? TRUE_INT : FALSE_INT);
    }

}
