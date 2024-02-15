package com.minister.component.trace.interceptor;

import com.minister.component.trace.constants.TraceConstants;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.context.ThreadLocalContext;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 接口拦截器：处理 TraceContext
 * traceId的处理在{@link com.minister.framework.boot.header.interceptor.HeaderHandlerInterceptor}
 *
 * @author QIUCHANGQING620
 * @date 2020-03-02 15:07
 */
public class TraceHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MDC.put(TraceConstants.THREAD_ID, TraceContext.initThreadId());

        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.clear();
        TraceContext.clean();
        ThreadLocalContext.clean();
        super.afterCompletion(request, response, handler, ex);
    }

}
