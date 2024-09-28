package com.minister.framework.debug.core.gray.utils;

import com.minister.framework.debug.core.utils.JsonFilterParseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 灰度规则条件工具类
 *
 * @author QIUCHANGQING620
 * @date 2024-05-03 00:00
 */
public class GrayRuleConditionUtil {

    /**
     * 组合条件标识
     */
    private final static String MULTIPLE_CONDITIONS_FLAG = "$$$";

    private final static String FRONT_BRACKET = "(";

    private final static String BACK_BRACKET = ")";

    private final static char LEFT_BRACKET = '(';

    private final static char RIGHT_BRACKET = ')';

    private final static String OR = " or ";

    private final static String AND = " and ";

    private final static String EQUAL = "=";

    /**
     * 校验参数是否命中
     *
     * @param requestBody 请求参数
     * @param filter      如：“user.name=张珊”或者“张珊&KY100002043”
     */
    public static boolean checkFilter(String requestBody, String filter) {
        try {
            if (filter.startsWith(MULTIPLE_CONDITIONS_FLAG)) {
                // 组合条件参数匹配
                // 去除标识位字符（$$$）
                filter = filter.substring(MULTIPLE_CONDITIONS_FLAG.length());
                // 组合条件参数匹配
                return compositeConditionsFilter(requestBody, filter);
            } else if (filter.contains(EQUAL)) {
                // 精准参数匹配
                return JsonFilterParseUtil.equalFilter(requestBody, filter);
            } else {
                // 模糊参数匹配
                return JsonFilterParseUtil.containsFilter(requestBody, filter);
            }
        } catch (Exception e) {
            // logger.warn("匹对异常，(不影响业务)", e);
        }
        return false;
    }

    /**
     * 组合条件参数匹配
     *
     * @param requestBody 请求体参数
     * @param filter      组合条件过滤规则
     * @return 匹配结果
     */
    private static boolean compositeConditionsFilter(String requestBody, String filter) {
        // 按OR条件关系拆分规则
        List<String> conditions = decomposeByOr(filter);
        if (conditions.size() > 1) {
            // or 关系只需 anyMatch
            return conditions.stream().anyMatch(condition -> evaluateConditions(requestBody, condition));
        } else {
            conditions = decomposeByAnd(filter);
            // and 关系需要 allMatch
            return conditions.stream().allMatch(condition -> evaluateConditions(requestBody, condition));
        }
    }

    /**
     * 匹配条件
     *
     * @param requestBody 请求体参数
     * @param filter      过滤条件
     */
    private static boolean evaluateConditions(String requestBody, String filter) {
        if (filter.contains(OR) || filter.contains(AND)) {
            // 组合条件
            return compositeConditionsFilter(requestBody, filter);
        } else if (filter.contains(EQUAL)) {
            // 精准参数匹配
            filter = deleteBracket(filter);
            return JsonFilterParseUtil.equalFilter(requestBody, filter);
        } else {
            // 模糊参数匹配
            filter = deleteBracket(filter);
            return JsonFilterParseUtil.containsFilter(requestBody, filter);
        }
    }

    /**
     * 按or条件关系拆分规则
     *
     * @param filterRule 组合条件规则
     * @return 拆分后的子条件
     */
    private static List<String> decomposeByOr(String filterRule) {
        // 去除最外层括号
        filterRule = deleteBracket(filterRule);
        List<String> stringFilterRules = new ArrayList<>();
        String[] filterRuleArray = filterRule.split(OR);
        StringBuilder filterItem = new StringBuilder();
        for (String item : filterRuleArray) {
            if (!isCompleteItem(item)) {
                filterItem.append(OR).append(item);
                String subItem = filterItem.substring(OR.length() - 1).trim();
                if (isCompleteItem(subItem)) {
                    stringFilterRules.add(subItem);
                    filterItem = new StringBuilder();
                }
            } else {
                if (filterItem.length() > OR.length()) {
                    stringFilterRules.add(filterItem.substring(OR.length() - 1).trim());
                    filterItem = new StringBuilder();
                }
                stringFilterRules.add(item.trim());
            }
        }
        return stringFilterRules;
    }

    /**
     * 按and条件关系拆分规则
     */
    private static List<String> decomposeByAnd(String filterRule) {
        // 去除最外层括号
        filterRule = deleteBracket(filterRule);
        List<String> stringFilterRules = new ArrayList<>();
        String[] filterRuleArray = filterRule.split(AND);
        StringBuilder filterItem = new StringBuilder();
        for (String item : filterRuleArray) {
            if (!isCompleteItem(item)) {
                filterItem.append(AND).append(item);
                String subItem = filterItem.substring(AND.length() - 1).trim();
                if (isCompleteItem(subItem)) {
                    stringFilterRules.add(subItem);
                    filterItem = new StringBuilder();
                }
            } else {
                if (filterItem.length() > AND.length()) {
                    stringFilterRules.add(filterItem.substring(AND.length() - 1).trim());
                    filterItem = new StringBuilder();
                }
                stringFilterRules.add(item.trim());
            }
        }
        return stringFilterRules;
    }

    /**
     * 去除最外层括号
     */
    private static String deleteBracket(String filterRule) {
        while (needDeleteBracket(filterRule)) {
            // 去除最外层括号
            filterRule = filterRule.trim().substring(1, filterRule.length() - 1);
        }
        return filterRule.trim();
    }

    /**
     * 判断是否需要删除最外层括号
     * 最左侧的开始括号对应的结束括号在最右侧，则需要删除
     */
    private static boolean needDeleteBracket(String filterRule) {
        filterRule = filterRule.trim();
        if (!filterRule.startsWith(FRONT_BRACKET) || !filterRule.endsWith(BACK_BRACKET)) {
            return false;
        }
        int count = 1;
        char[] chars = filterRule.toCharArray();
        for (int index = 1; index < chars.length; index++) {
            if (chars[index] == LEFT_BRACKET) {
                count++;
            }
            if (chars[index] == RIGHT_BRACKET) {
                count--;
            }
            if (count == 0) {
                if (index == filterRule.length() - 1) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    /**
     * 判断是否是一个完整规则项
     */
    private static boolean isCompleteItem(String rule) {
        if (rule.indexOf(FRONT_BRACKET) < 0 && rule.indexOf(BACK_BRACKET) < 0) {
            return true;
        }
        // 判断左右括号数量是否一样
        String temStr = rule.replaceAll("\\(", "");
        int frontBracketCount = rule.length() - temStr.length();
        temStr = rule.replaceAll("\\)", "");
        int backBracketCount = rule.length() - temStr.length();

        return frontBracketCount == backBracketCount;
    }

}
