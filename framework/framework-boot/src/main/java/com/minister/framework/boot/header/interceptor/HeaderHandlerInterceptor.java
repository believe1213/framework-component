package com.minister.framework.boot.header.interceptor;

import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.IpUtil;
import com.minister.component.utils.constants.HeadersKey;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.entity.HeaderEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * 接口拦截器：处理 HeadersContext
 * MDC的清理在{@link com.minister.component.trace.interceptor.TraceHandlerInterceptor}
 *
 * @author QIUCHANGQING620
 * @date 2020-03-02 15:07
 */
@Slf4j
public class HeaderHandlerInterceptor extends HandlerInterceptorAdapter {

    /**
     * 拦截于请求刚进入时，进行判断，需要boolean返回值，如果返回true将继续执行，如果返回false，将不进行执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Enumeration<String> headerNames = request.getHeaderNames();
        HeadersContext.HeaderEntityBuilder headerEntityBuilder = HeadersContext.builder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String value = request.getHeader(headerName);

            if (!HeadersKey.contains(headerName)) {
                HeadersContext.setCustomHeader(headerName, value);
                continue;
            }

            // traceId（TraceContext的清理在 {@link com.minister.component.trace.interceptor.TraceHandlerInterceptor}）
            if (HeadersKey.TRACE_ID.equals(headerName)) {
                if (StringUtils.isBlank(value)) {
                    value = TraceContext.initTraceId();
                } else {
                    TraceContext.setTraceId(value);
                }
                MDC.put(TraceConstants.TRACE_ID, value);
            }
            // batchId
            if (HeadersKey.BATCH_ID.equalsIgnoreCase(headerName)) {
                MDC.put(TraceConstants.BATCH_ID, value);
            }
            // userId
            if (HeadersKey.USER_ID.equals(headerName)) {
                MDC.put(TraceConstants.USER_ID, value);
            }

            headerEntityBuilder.put(headerName, value);
        }

        HeaderEntity headerEntity = headerEntityBuilder.build();
        // traceId
        if (StringUtils.isBlank(headerEntity.getTraceId())) {
            String traceId = TraceContext.initTraceId();
            MDC.put(TraceConstants.TRACE_ID, traceId);
            headerEntity.setTraceId(traceId);
        }

        // 设置 requestIp
        long start = System.currentTimeMillis();
        if (StringUtils.isBlank(headerEntity.getRequestIp())) {
            headerEntity.setRequestIp(IpUtil.getRemoteIp(request));
        }
        log.info("cost : {}", System.currentTimeMillis() - start);

        HeadersContext.setHeaderEntity(headerEntity);

        return true;
    }

    /**
     * 拦截于方法成功返回后，视图渲染前，可以进行成功返回的日志记录
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HeadersContext.clean();
    }

}
