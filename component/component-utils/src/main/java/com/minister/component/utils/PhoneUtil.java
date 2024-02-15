package com.minister.component.utils;

import cn.hutool.core.text.StrPool;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 电话号码Util
 *
 * @author QIUCHANGQING620
 * @date 2024-02-12 15:38
 */
public class PhoneUtil {

    private PhoneUtil() {
    }

    /**
     * 大陆手机号
     */
    public static final String MAINLAND_MOBILE_REGEX = "^(?:0|86|\\+86)?(1[3-9]\\d{9})$";
    public static final String MAINLAND_MOBILE_AXN_REGEX = "^(?:0|86|\\+86)?(1[3-9]\\d{9})-?([\\d]{1,4})?$";
    /**
     * 香港手机号8位数，5|6|8|9开头+7位任意数
     */
    private static final String REGEX_HK_MOBILE = "^(?:852|00852|\\+852)?(-)?[5689]\\d{7}$";
    /**
     * 澳门手机号8位数
     */
    private static final String REGEX_MACAU_MOBILE = "^(?:853|00853|\\+853)?(-)?\\d{8}$";
    /**
     * 台湾手机号9位数
     */
    private static final String REGEX_TAIWAN_MOBILE = "^(?:886|00886|\\+886)?(-)?\\d{9}$";


    /**
     * 大陆固定号码（不带分机号）
     */
    private final static String MAINLAND_PHONE_REGEX = "^(((\\+?00[- ]?86[- ]?)|(\\+?86[- ]?))?((010|02\\d|0[3-9]\\d{2,3})[- ]?)?\\d{7,8})|((400-?|800-?)\\d{5,10}(-\\d{1,6})?)$";
    /**
     * 大陆固定号码（带分机号）
     */
    private final static String MAINLAND_PHONE_AXN_REGEX = "^(((\\+?00[- ]?86[- ]?)|(\\+?86[- ]?))?((010|02\\d|0[3-9]\\d{2,3})[- ]?)?\\d{7,8}(-\\d{1,6})?)|((400-?|800-?)\\d{5,10}(-\\d{1,6})?)$";
    /**
     * 台湾固定号码
     */
    private final static String TW_PHONE_REGEX = "^((\\+?00[- ]?886[- ]?)|(\\+?886[- ]?))?([0]?[2-9]{1}\\d{0,2})[- ]?\\d{7,8}([-]\\d{1,8})?$";
    /**
     * 香港固定号码
     */
    private final static String HK_PHONE_REGEX = "^((\\+?00[- ]?852[- ]?)|(\\+?852[- ]?))?[2|3]\\d{7}([-]\\d{2,5})?$";
    /**
     * 澳门固定号码
     */
    private final static String AM_PHONE_REGEX = "^((\\+?00853[- ]?)|(\\+?853[- ]?))?((28\\d{6})|(8\\d{7})|(23\\d{6})|(66\\d{6}))([-]\\d{1,6})?$";


    public static Boolean isMainlandMobile(String mobile) {
        return StringUtils.isNotBlank(mobile) && mobile.matches(MAINLAND_MOBILE_REGEX);
    }

    public static boolean isMainlandExtMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return false;
        }

        mobile = StrUtil.replaceFirst(mobile, "转", StrPool.DASHED);

        return mobile.matches(MAINLAND_MOBILE_AXN_REGEX);
    }


    public static boolean isHkMobile(String mobile) {
        return StringUtils.isNotBlank(mobile) && mobile.matches(REGEX_HK_MOBILE);
    }

    /**
     * 校验大陆固定号码（带分机号）
     */
    public static boolean isMainlandExtPhone(String phone) {
        return StringUtils.isNotBlank(phone) && phone.matches(MAINLAND_PHONE_AXN_REGEX);
    }

    @Data
    public static class PhoneNumberExt {

        /**
         * 区号+号码
         */
        private String phoneNumber;

        /**
         * 分机号
         */
        private String extension;

        /**
         * 手机号
         */
        private String mobile;
    }

    public static final String TELEPHONE_SPLITTER = "^(?:86|\\+86)?-?(010|02\\d|0[3-9]\\d{2})?-?(\\d{7,8})-?(\\d{1,6})?";
    public static final String TELEPHONE_SPLITTER2 = "^(400-?|800-?)(\\d{5,10})-?(\\d{1,6})?";

    public static PhoneNumberExt phoneNumberExtSplit(String phoneNumber) {
        PhoneNumberExt ext = new PhoneNumberExt();
        if (StringUtils.isBlank(phoneNumber)) {
            return ext;
        }
        if (phoneNumber.matches(MAINLAND_MOBILE_AXN_REGEX)) {
            ext.setMobile(phoneNumber.replaceFirst(MAINLAND_MOBILE_AXN_REGEX, "$1"));
            ext.setExtension(phoneNumber.replaceFirst(MAINLAND_MOBILE_AXN_REGEX, "$2"));
            return ext;
        }
        if (phoneNumber.matches(TELEPHONE_SPLITTER)) {
            String areaCode= phoneNumber.replaceFirst(TELEPHONE_SPLITTER, "$1");
            String phNumber = phoneNumber.replaceFirst(TELEPHONE_SPLITTER, "$2");
            String extension= phoneNumber.replaceFirst(TELEPHONE_SPLITTER, "$3");
            ext.setPhoneNumber((StringUtils.isNotBlank(areaCode) ? areaCode + StrPool.DASHED : StringUtils.EMPTY) + phNumber);
            ext.setExtension(extension);
            return ext;
        }
        if (phoneNumber.matches(TELEPHONE_SPLITTER2)) {
            String code = phoneNumber.replaceFirst(TELEPHONE_SPLITTER2, "$1");
            String phNumber = phoneNumber.replaceFirst(TELEPHONE_SPLITTER2, "$2");
            String extension = phoneNumber.replaceFirst(TELEPHONE_SPLITTER2, "$3");
            ext.setPhoneNumber((StrUtil.isNotBlank(code) ? code : StringUtils.EMPTY) + phNumber);
            ext.setExtension(extension);
            return ext;
        }
        String[] splits = phoneNumber.split(StrPool.DASHED);
        if (splits.length > 2) {
            int index = phoneNumber.lastIndexOf(StrPool.DASHED);
            ext.setPhoneNumber(phoneNumber.substring(0, index));
            ext.setExtension(splits[splits.length - 1]);
        } else {
            ext.setPhoneNumber(phoneNumber);
        }
        return ext;
    }

    public static String getPhone(String str) {
        if (StringUtils.isBlank(str)) {
            return StringUtils.EMPTY;
        }
        PhoneNumberExt ext = phoneNumberExtSplit(str);
        return StringUtils.isNotBlank(ext.getMobile()) ? ext.getMobile() : ext.getPhoneNumber();
    }

    public static String getExtension(String str) {
        if (StringUtils.isBlank(str)) {
            return StringUtils.EMPTY;
        }
        PhoneNumberExt ext = phoneNumberExtSplit(str);
        return StringUtils.isEmpty(ext.getExtension()) ? StringUtils.EMPTY : ext.getExtension();
    }

    /**
     * 获取固定电话 0755-XXXXX-1
     *
     * @param phone     固定电话
     * @param extension 分机号
     * @return 固定电话
     */
    public static String getFullPhone(String phone, String extension) {
        if (StringUtils.isBlank(phone)) {
            return StringUtils.EMPTY;
        }
        // 电话分机号
        String extensionNumber = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(extension)) {
            extensionNumber = StrPool.DASHED + extension;
        }
        // 0755-XXXXX-1
        return phone + extensionNumber;
    }

}
