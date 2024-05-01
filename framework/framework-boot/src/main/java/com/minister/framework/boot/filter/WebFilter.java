package com.minister.framework.boot.filter;

import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.IpUtil;
import com.minister.component.utils.constants.HeadersKey;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.entity.HeaderEntity;
import com.minister.framework.boot.filter.utils.FilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 基础请求拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-04-24 00:23
 */
@Slf4j
public class WebFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (!(sRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
                throw new ServletException("Filter just supports HTTP requests");
            }
            HttpServletRequest servletRequest = (HttpServletRequest) sRequest;

            String uri = FilterUtils.getRequestUri(servletRequest);
            if (FilterUtils.skipPathUri(uri)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            initHeadersContextAndMDC(servletRequest);

            filterChain.doFilter(servletRequest, servletResponse);
        } catch (ServletException | IOException e) {
            logger.error("WebFilter request/response error!");
            throw e;
        }
    }

    private void initHeadersContextAndMDC(HttpServletRequest request) {
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
        log.debug("get request ip cost : {}", System.currentTimeMillis() - start);

        HeadersContext.set(headerEntity);
    }

}
