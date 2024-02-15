package com.minister.framework.api.utils;

import com.minister.component.utils.enums.HttpCodeEnum;
import com.minister.component.utils.exception.AppCode;
import com.minister.framework.api.entity.ResponseDto;

/**
 * ResponseDtoFactory
 *
 * @author QIUCHANGQING620
 * @date 2020-02-21 06:01
 */
public class ResponseDtoFactory {

    public static <T> ResponseDto<T> success() {
        return new ResponseDto<>(HttpCodeEnum.OK.getCode(), HttpCodeEnum.OK.getMsg());
    }

    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(HttpCodeEnum.OK.getCode(), HttpCodeEnum.OK.getMsg(), data);
    }

    // ----- 以下方法特殊情况允许使用 -----

    public static <T> ResponseDto<T> error() {
        return new ResponseDto<>(HttpCodeEnum.InternalServerError.getCode(), HttpCodeEnum.InternalServerError.getMsg());
    }

    public static <T> ResponseDto<T> error(String msg) {
        return new ResponseDto<>(HttpCodeEnum.InternalServerError.getCode(), msg);
    }

    public static <T> ResponseDto<T> error(AppCode code) {
        return new ResponseDto<>(code.getCode(), code.getMsg());
    }

    public static <T> ResponseDto<T> error(AppCode code, String msg) {
        return new ResponseDto<>(code.getCode(), msg);
    }

    public static <T> ResponseDto<T> error(String code, String msg) {
        return new ResponseDto<>(code, msg);
    }

}
