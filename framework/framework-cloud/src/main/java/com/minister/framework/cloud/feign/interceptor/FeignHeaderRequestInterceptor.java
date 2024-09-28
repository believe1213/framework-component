package com.minister.framework.cloud.feign.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.JacksonUtil;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.entity.HeaderEntity;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * feign 请求头拦截器
 *
 * @author QIUCHANGQING620
 * @date 2020-03-06 23:24
 */
@Configuration
@Slf4j
public class FeignHeaderRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // traceId
        String traceId = TraceContext.getTraceId();
        if (StringUtils.isNotBlank(traceId)) {
            MDC.put(TraceConstants.TRACE_ID, traceId);
        }
        // HeaderEntity
        HeaderEntity headerEntity = HeadersContext.getHeaderEntity();
        Map<String, String> headerMap = JacksonUtil.convertValue(headerEntity, new TypeReference<Map<String, String>>() {
        });
        setHeader(requestTemplate, headerMap);

        // customHeader
        Map<String, String> customHeader = HeadersContext.getCustomHeader();
        setHeader(requestTemplate, customHeader);
    }

    private void setHeader(RequestTemplate requestTemplate, Map<String, String> headers) {
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    requestTemplate.header(entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
