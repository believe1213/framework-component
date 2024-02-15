package com.minister.component.utils.constants;

import com.minister.component.utils.id.SnowflakeIdUtil;

/**
 * 常量
 *
 * @author QIUCHANGQING620
 * @date 2020-07-23 17:22
 */
public interface Constants {

    SnowflakeIdUtil SNOWFLAKE_ID_UTIL = new SnowflakeIdUtil();

    String GMT_8 = "GMT+8";

    String RUNNING_CAP = "RUNNING";

    String RUNNING_LOW = "running";

    String SUCCESS_CAP = "SUCCESS";

    String SUCCESS_LOW = "success";

    String FAIL_CAP = "FAIL";

    String FAIL_LOW = "fail";

    String TIMEOUT_CAP = "TIMEOUT";

    String TIMEOUT_LOW = "timeout";

    String DEFAULT_CAP = "DEFAULT";

    String DEFAULT_LOW = "default";

    String POUND_SIGN = "#";

    String ASTERISK = "*";

    String SEMICOLON = ";";

    String EXCLAMATION_MARK = "!";

    String QUESTION_MARK = "?";

    String QUOTATION_MARK = "\"";

    String PARENTHESES_START = "(";

    String PARENTHESES_END = ")";

    String STR_ZERO = "0";

    String STR_ONE = "1";

    Integer INT_ZERO = 0;

    Integer INT_ONE = 1;

    Double DOUBLE_ZERO = 0D;

}
