package com.minister.framework.cloud.utils;

import com.minister.framework.boot.exception.FeignException;
import feign.codec.DecodeException;

/**
 * FeignExceptionUtil
 *
 * @author QIUCHANGQING620
 * @date 2020-08-06 17:33
 */
public class FeignExceptionUtil {

    private FeignExceptionUtil() {
    }

    public static FeignException get(Throwable e) {
        if (!(e instanceof DecodeException)) {
            return null;
        }
        Throwable t = e.getCause();
        return t instanceof FeignException ? (FeignException) t : null;
    }

}
