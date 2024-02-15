package com.minister.framework.boot.exception;

import com.minister.component.utils.exception.AppCode;
import com.minister.component.utils.exception.BaseException;

/**
 * 调用第三方接口异常继承类
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 17:39
 */
public class RemoteCallException extends BaseException {

    // ----- 特殊情况允许使用start -----

    public RemoteCallException(String code, String msg) {
        super(code, msg);
    }

    public RemoteCallException(String code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    // ----- 特殊情况允许使用end -----

    public RemoteCallException(AppCode appCode) {
        super(appCode);
    }

    public RemoteCallException(AppCode appCode, Throwable cause) {
        super(appCode, cause);
    }

    public RemoteCallException(AppCode appCode, String msg) {
        super(appCode, msg);
    }

    public RemoteCallException(AppCode appCode, String msg, Throwable cause) {
        super(appCode, msg, cause);
    }

}
