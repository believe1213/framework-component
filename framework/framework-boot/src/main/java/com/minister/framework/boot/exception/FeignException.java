package com.minister.framework.boot.exception;

import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.enums.FrameworkExEnum;
import com.minister.component.utils.exception.BaseException;
import lombok.Getter;
import lombok.Setter;

/**
 * 调用 Feign 抛出的异常
 *
 * @author QIUCHANGQING620
 * @date 2020-03-06 23:11
 */
@Getter
@Setter
public class FeignException extends BaseException {

    /**
     * 返回traceId
     */
    private String traceId = TraceContext.getTraceId();

    public FeignException() {
        super(FrameworkExEnum.FEIGN_EX);
    }

    public FeignException(String msg) {
        super(FrameworkExEnum.FEIGN_EX, msg);
    }

    public FeignException(String code, String msg) {
        super(code, msg);
    }

    public FeignException(String code, String msg, String traceId) {
        super(code, msg);
        this.traceId = traceId;
    }

}
