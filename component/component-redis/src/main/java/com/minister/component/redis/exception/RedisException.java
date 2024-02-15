package com.minister.component.redis.exception;

import com.minister.component.utils.enums.FrameworkExEnum;
import com.minister.component.utils.exception.BaseException;

/**
 * RedisException
 *
 * @author QIUCHANGQING620
 * @date 2024-02-13 16:51
 */
public class RedisException extends BaseException {

    public RedisException() {
        super(FrameworkExEnum.REDIS_EX);
    }

    public RedisException(Throwable e) {
        super(FrameworkExEnum.REDIS_EX, e);
    }

}
