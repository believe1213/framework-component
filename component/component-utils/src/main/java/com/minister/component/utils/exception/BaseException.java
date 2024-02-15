package com.minister.component.utils.exception;

import com.minister.component.utils.enums.HttpCodeEnum;
import lombok.Getter;

/**
 * 封装的最底层异常类型
 *
 * @author QIUCHANGQING620
 * @date 2020-03-06 23:06
 */
@Getter
public class BaseException extends RuntimeException {

    private String code = HttpCodeEnum.InternalServerError.getCode();

    public BaseException() {
        super(HttpCodeEnum.InternalServerError.getMsg());
    }

    // ----- 特殊情况允许使用start -----

    public BaseException(String msg) {
        super(msg);
    }

    public BaseException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    // ----- 特殊情况允许使用end -----

    public BaseException(AppCode appCode) {
        super(appCode.getMsg());
        this.code = appCode.getCode();
    }

    public BaseException(AppCode appCode, Throwable cause) {
        super(appCode.getMsg(), cause);
        this.code = appCode.getCode();
    }

    public BaseException(AppCode appCode, String msg) {
        super(msg);
        this.code = appCode.getCode();
    }

    public BaseException(AppCode appCode, String msg, Throwable cause) {
        super(msg, cause);
        this.code = appCode.getCode();
    }

}
