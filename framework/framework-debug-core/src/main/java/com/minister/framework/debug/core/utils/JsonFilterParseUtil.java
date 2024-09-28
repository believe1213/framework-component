package com.minister.framework.debug.core.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.minister.framework.debug.core.utils.json.FilterJsonUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * json拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
public class JsonFilterParseUtil {

    /**
     * 校验参数是否命中
     *
     * @param jsonStr 请求参数
     * @param filter  如：“user.name=张珊”或者“张珊&host”
     */
    public static boolean checkFilter(String jsonStr, String filter) {
        try {
            if (filter.contains("=")){
                // 精准参数匹配
                return equalFilter(jsonStr, filter);
            } else {
                // 模糊参数匹配
                return containsFilter(jsonStr, filter);
            }
        } catch (Exception e) {
            // logger.warn("匹对异常，(不影响业务)", e);
        }
        return false;
    }

    /**
     * 模糊参数过滤
     *
     * @param jsonStr 请求参数
     * @param filter  过滤条件
     * @return
     */
    public static boolean containsFilter(String jsonStr, String filter) {
        // 模糊匹配参数
        String[] filters = filter.split("&");
        for (String f : filters) {
            if (jsonStr.contains(f.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 精准参数过滤
     *
     * @param jsonStr 请求参数
     * @param filter  过滤条件
     * @return
     */
    public static boolean equalFilter(String jsonStr, String filter) {
        // 精准判断
        String[] filters = filter.split("&");
        for (String f : filters) {
            String[] arr = f.split("=", 2);
            String key = arr[0].trim();
            String value = arr[1].trim();
            if (!isArrayListStr(jsonStr) && !isJsonStr(jsonStr)) {
                jsonStr = FilterJsonUtils.serializable(jsonStr, String.class);
            }
            String valueR = JsonFilterParseUtil.parseString(jsonStr, key);
            if (StringUtils.isNotBlank(valueR) && valueR.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为数组字符串
     *
     * @param jsonStr
     * @return
     */
    private static boolean isArrayListStr(String jsonStr) {
        if (StringUtils.isNotBlank(jsonStr)) {
            jsonStr = jsonStr.replaceAll("\r\n","");
            if (jsonStr.indexOf("[") == 0 && jsonStr.lastIndexOf("]") == jsonStr.length() - 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为json对象字符串
     *
     * @param jsonStr
     * @return
     */
    private static boolean isJsonStr(String jsonStr) {
        if (StringUtils.isNotBlank(jsonStr)) {
            jsonStr = jsonStr.replaceAll("\r\n","");
            if (jsonStr.indexOf("{") == 0 && jsonStr.lastIndexOf("}") == jsonStr.length() - 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析出STRING, 判断逻辑如下
     * 1、根据.割据分段
     * 2、判断当前分段是否有[]关键字，有，则分离出来key和index,
     * 3、根据[]符号转换为数组或者对象
     * 4、仅有符号，没有key，则直接取数组index
     * 5、仅有key,没有符号，则根据key读取对象
     * 6、有符号有key,则先根据key取数组再取index
     *
     * @param jsonStr
     * @param keys    generic.vos[0].propertyName;  generic.vos[0].values[1];  [1].generic.vos[1].columnName
     * @return
     */
    public static String parseString(String jsonStr, String keys) {
        try {
            JSONArray jsonArray = null;
            JSONObject jsonObject = null;
            if (isJsonStr(jsonStr)) {
                jsonObject = JSON.parseObject(jsonStr);
            } else if (isArrayListStr(jsonStr)) {
                jsonArray = JSON.parseArray(jsonStr);
            }
            String[] keyArr = keys.split("\\.");
            if (keyArr.length == 1) {
                return jsonObject.getString(keys);
            }
            if (keyArr.length > 1) {
                for (int i = 0; i < keyArr.length - 1; i++) {
                    String item = keyArr[i];
                    String itemKey = getKeyFromItem(item);
                    Integer index = getIndexFromItem(item);
                    if (isArrayWithItem(item)) {
                        if (StringUtils.isBlank(itemKey)) {
                            jsonObject = jsonArray.getJSONObject(index);
                        } else {
                            jsonArray = jsonObject.getJSONArray(itemKey);
                            jsonObject = jsonArray.getJSONObject(index);
                        }
                    } else {
                        jsonObject = jsonObject.getJSONObject(itemKey);
                    }
                }
            }
            String item = keyArr[keyArr.length - 1];
            String value = null;
            if (isArrayWithItem(item)) {
                String itemKey = getKeyFromItem(item);
                Integer index = getIndexFromItem(item);
                value = jsonObject.getJSONArray(itemKey).getString(index);
            } else {
                String itemKey = getKeyFromItem(item);
                value = jsonObject.getString(itemKey);
            }
            return value;
        } catch (Exception e) {
            // logger.error("ERROR", e);
        }
        return null;
    }

    /**
     * 判断Item是否为数组 ，true为数组
     */
    public static boolean isArrayWithItem(String item) {
        if (StringUtils.isNotBlank(item)) {
            if (item.indexOf("[") >= 0 && item.lastIndexOf("]") >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从Item获取数组下标
     */
    public static Integer getIndexFromItem(String item) {
        if (StringUtils.isNotBlank(item)) {
            int start = item.indexOf("[");
            int end = item.lastIndexOf("]");
            if (start >= 0 && end >= 0) {
                return Integer.valueOf(item.substring(start + 1, end).trim());

            }
        }
        return null;
    }

    /**
     * 从Item获取Key
     */
    public static String getKeyFromItem(String item) {
        if (StringUtils.isNotBlank(item)) {
            int start = item.indexOf("[");
            if (start >= 0) {
                return item.substring(0, start).trim();
            }
        }
        return item;
    }

}
