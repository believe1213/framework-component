package com.minister.component.utils.enums;

import com.minister.component.utils.exception.AppCode;
import lombok.AllArgsConstructor;

/**
 * 框架内部异常Enum
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 18:14
 */
@AllArgsConstructor
public enum FrameworkExEnum implements AppCode {

    /**
     *
     */
    DEFAULT_ERROR("000", "系统繁忙,请稍后再试"),
    MISSING_HEADER_EX("001", "请求头缺失异常"),
    MISSING_PARAM_EX("002", "参数缺失异常"),

    MISSING_PART_EX("003", "请求中必须至少包含一个有效文件"),
    MULTI_PART_EX("004", "获取文件异常"),

    PARAM_READ_EX("005", "参数解析异常"),
    PARAM_VALID_EX("006", "参数校验异常"),
    PARAM_BIND_EX("007", "参数类型绑定异常"),
    METHOD_ARG_VALID_EX("008", "方法参数校验异常"),
    METHOD_ARG_TYPE_EX("009", "参数类型解析异常"),

    ILLEGAL_STATE_EX("010", "无效状态异常"),
    ILLEGAL_ARGUMENT_EX("011", "无效参数异常"),
    NULL_POINT_EX("012", "空指针异常"),

    SQL_EX("050", "SQL异常"),

    REDIS_EX("060", "REDIS调用异常"),

    MQ_EX("070", "MQ调用异常"),

    FLOW_EX("080", "流程引擎调用异常"),

    FEIGN_EX("100", "feign调用异常"),
    SERVICE_NOT_ONLINE("101", "服务未上线"),
    SERVICE_OFFLINE("102", "服务已下线"),
    ;

    /**
     * 组件编号
     */
    private static final String COMPONENT = "01";

    /**
     * 功能编号
     */
    private static final String MODULE = "01";

    /**
     * 前缀
     */
    private static final String PREFIX = COMPONENT + MODULE;

    /**
     * toString() 分隔符
     */
    private static final String SPLIT_CHAR = ":";

    private final String code;

    private final String msg;

    @Override
    public String getCode() {
        return PREFIX + this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }

    @Override
    public String toString() {
        return PREFIX + this.code + SPLIT_CHAR + this.msg;
    }

}
