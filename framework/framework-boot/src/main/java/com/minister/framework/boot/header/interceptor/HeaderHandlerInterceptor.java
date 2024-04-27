package com.minister.framework.boot.header.interceptor;

import com.minister.component.utils.context.HeadersContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 接口拦截器：处理 HeadersContext
 * 逻辑迁移至{@link com.minister.framework.boot.filter.WebFilter#initHeadersContextAndMDC}
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
