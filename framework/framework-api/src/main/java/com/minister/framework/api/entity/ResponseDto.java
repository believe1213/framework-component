package com.minister.framework.api.entity;

import cn.hutool.core.date.DatePattern;
import com.minister.component.trace.context.TraceContext;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 * ResponseDto<T>
 *
 * @author QIUCHANGQING620
 * @date 2020-02-19 01:17
 */
@Data
public class ResponseDto<T> {

    private String code;

    private String msg;

    private T data;

    private String traceId = TraceContext.getTraceId();

    private String timestamp = DateFormatUtils.format(new Date(), DatePattern.NORM_DATETIME_PATTERN);

    public ResponseDto() {
    }

    public ResponseDto(String code, String msg) {
        this(code, msg, null);
    }

    public ResponseDto(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

}
