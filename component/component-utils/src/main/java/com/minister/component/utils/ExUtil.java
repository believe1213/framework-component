package com.minister.component.utils;

import cn.hutool.core.exceptions.ExceptionUtil;

/**
 * ExceptionUtil
 *
 * @author QIUCHANGQING620
 * @date 2020-08-06 16:15
 */
public class ExUtil extends ExceptionUtil {

    private ExUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T findExceptionFromStack(Throwable e, Class<T> queriedException) {
        if (e == null || queriedException == null) {
            return null;
        }
        while (e != null) {
            if (queriedException.isInstance(e)) {
                return (T) e;
            }
            e = e.getCause();
        }
        return null;
    }

}
