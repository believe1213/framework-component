package com.minister.framework.boot.exception;

import com.minister.component.utils.exception.AppCode;
import com.minister.component.utils.exception.BaseException;

/**
 * 内部组件异常继承类
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 17:36
 */
public class CommonException extends BaseException {

    // ----- 特殊情况允许使用start -----

    public CommonException(String code, String msg) {
        super(code, msg);
    }

    public CommonException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    // ----- 特殊情况允许使用end -----

    public CommonException(AppCode appCode) {
        super(appCode);
    }

    public CommonException(AppCode appCode, Throwable cause) {
        super(appCode, cause);
    }

    public CommonException(AppCode appCode, String msg) {
        super(appCode, msg);
    }

    public CommonException(AppCode appCode, String msg, Throwable cause) {
        super(appCode, msg, cause);
    }

}
