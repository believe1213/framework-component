package com.minister.component.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串Util
 *
 * @author QIUCHANGQING620
 * @date 2024-02-12 14:35
 */
public class StrUtil extends cn.hutool.core.util.StrUtil {

    /**
     * 中文字符
     */
    public static final String CHINESE_REGEX = "[\u4e00-\u9fa5]";
    private static final Pattern C_PATTERN = Pattern.compile(CHINESE_REGEX);

    /**
     * 生僻字中文字符
     */
    private static final String RARE_CHINESE_REGEX = "(?![\\u3000-\\u303F])[\\u2E80-\\uFE4F]";
    private static final Pattern RARE_CHINESE_PATTERN = Pattern.compile(RARE_CHINESE_REGEX);

    /**
     * 英文字符
     */
    private static final String ENGLISH_REGEX = "[A-Za-z]";
    private static final Pattern ENGLISH_PATTERN = Pattern.compile(ENGLISH_REGEX);

    /**
     * 特殊字符
     */
    public static final String SPECIAL_REGEX = "[`~!@#$%^&*()+=|{}／＼':;\\[\\].<>［］/?！￥…（）—【】\\-‘；〈〉√〔〕：”“’。，、？\\\\｛｝「」｀～•‖『』〖〗＜＞〝〞﹝﹞＆＠＃·«»《》]";

    /**
     * 姓名特殊字符
     */
    public static final String[] PERSONAL_NAME_SPECIAL_CHARACTER = {" ", "【", "-", ".", "·", "（", "）", "(", ")", "】"};

    /**
     * 是否包含中文字符
     */
    public static boolean isContainChineseChar(String str) {
        Matcher m1 = C_PATTERN.matcher(str);
        if (m1.find()) {
           return true;
        }

        Matcher m2 = RARE_CHINESE_PATTERN.matcher(str);
        return m2.find();
    }

    /**
     * 是否包含英文字符
     */
    public static boolean isContainEnglishChar(String str) {
        Matcher m = ENGLISH_PATTERN.matcher(str);
        return m.find();
    }

    /**
     * 姓名校验
     * 至少包含一个中文
     * 仅支持指定特殊字符
     */
    public static boolean nameValidate(String content) {
        int multiCharacterLength = content.length();
        StringBuilder multiCharacterBuilder = new StringBuilder();
        int chineseFlag = 0;
        int englishFlag = 0;
        char checkChar;
        String checkStr;
        for (int i = 0; i < multiCharacterLength; i++) {
            checkChar = content.charAt(i);
            checkStr = String.valueOf(checkChar);
            if (isContainChineseChar(checkStr)) {
                // 中文字符
                chineseFlag++;
            } else if (isContainEnglishChar(checkStr)) {
                // 英文字符
                englishFlag++;
            } else {
                // 存储非中文字符和英文字母的字符
                multiCharacterBuilder.append(checkChar);
            }
        }
        // 不存在中文和英文，返回false
        if (chineseFlag == 0 && englishFlag == 0) {
            return false;
        }
        // 校验数字与指定特殊字符
        for (int j = 0; j < multiCharacterBuilder.length(); j++) {
            checkChar = multiCharacterBuilder.charAt(j);
            if (!Character.isDigit(checkChar) && !StringUtils.equalsAny(String.valueOf(checkChar), PERSONAL_NAME_SPECIAL_CHARACTER)) {
                return false;
            }
        }
        return true;
    }

}
