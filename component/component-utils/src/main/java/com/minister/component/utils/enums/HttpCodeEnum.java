package com.minister.component.utils.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.minister.component.utils.exception.AppCode;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * http enum
 * {@link org.springframework.http.HttpStatus}
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 01:53
 */
@AllArgsConstructor
public enum HttpCodeEnum implements AppCode {

    /**
     *
     */
    OK("200", "操作成功"),

    MovedPermanently("301", "目标内容已永久迁移"),
    Redirect("302", "目标内容已临时迁移"),
    NotModified("304", "请求内容未修改"),

    BadRequest("400", "参数错误"),
    Unauthorized("401", "身份认证失败"),
    Forbidden("403", "权限不足，无法访问"),
    NotFound("404", "资源未找到"),
    MethodMotAllowed("405", "不支持当前请求类型"),
    NotAcceptable("406", "不支持当前请求参数类型"),
    RequestTimeout("408", "未在指定时间内发起请求"),
    PayloadTooLarge("413", "参数内容过多"),

    InternalServerError("500", "系统内部错误"),
    BadGateway("502", "代理服务器异常"),
    ServiceUnavailable("503", "服务正在维护"),
    GatewayTimeout("504", "代理服务器响应超时"),
    ;

    private final String code;

    private final String msg;

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @JsonCreator
    public static HttpCodeEnum getEnum(String code) {
        if (StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("code can not be null");
        }
        final HttpCodeEnum[] enums = HttpCodeEnum.values();
        for (HttpCodeEnum e : enums) {
            final String c = e.getCode();
            if (c.equals(code)) {
                return e;
            }
        }
        return null;
    }

}
