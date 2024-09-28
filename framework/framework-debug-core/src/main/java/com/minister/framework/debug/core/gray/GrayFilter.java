package com.minister.framework.debug.core.gray;

import cn.hutool.core.text.StrPool;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import com.minister.component.trace.context.TraceContext;
import com.minister.component.utils.JacksonUtil;
import com.minister.component.utils.constants.ApplicationConstant;
import com.minister.component.utils.context.HeadersContext;
import com.minister.framework.boot.filter.utils.CustomServletRequestWrapper;
import com.minister.framework.boot.filter.utils.FilterUtils;
import com.minister.framework.debug.core.constants.DebugConstants;
import com.minister.framework.debug.core.gray.helper.GrayForwardHelper;
import com.minister.framework.debug.core.utils.GrayDebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.MDC;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 灰度拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024-05-03 00:00
 */
@Slf4j
public class GrayFilter extends GenericFilterBean {

    private GrayForwardHelper grayForwardHelper = new GrayForwardHelper();

    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        try {
            ApplicationConstant applicationConstant = SpringUtil.getBean(ApplicationConstant.class);
            if (Objects.isNull(applicationConstant) || !applicationConstant.isServiceGrayGate()) {
                chain.doFilter(sRequest, servletResponse);
                return;
            }
            HttpServletRequest hsRequest = (HttpServletRequest) sRequest;
            String uri = FilterUtils.getRequestUri(hsRequest);
            if (FilterUtils.skipPathUri(uri) || GrayForwardHelper.isCacheEmpty()) {
                chain.doFilter(hsRequest, servletResponse);
                return;
            }

            // 当前服务端口
            String serverPort = applicationConstant.getPort();
            if (DebugConstants.POST.equalsIgnoreCase(hsRequest.getMethod()) &&
                    hsRequest.getContentType() != null &&
                    hsRequest.getContentType().toLowerCase().contains(DebugConstants.FORM_URL_ENCODED)) {
                ResourceUrlEncodingFilter filter = new ResourceUrlEncodingFilter();
                String body = packFormDataBody(hsRequest);
                String host = grayForwardHelper.checkForwardHost(uri, body, serverPort);
                if (StringUtils.isNotBlank(host)) {
                    forwardFormUrlencoded(hsRequest, servletResponse, host, uri);
                    return;
                }
                filter.doFilter(hsRequest, servletResponse, chain);
                return;
            }

            if (FilterUtils.needRequestWrapper(hsRequest)) {
                CustomServletRequestWrapper rw = (hsRequest instanceof CustomServletRequestWrapper) ?
                        (CustomServletRequestWrapper) hsRequest : new CustomServletRequestWrapper(hsRequest);
                String body = null;
                if (rw.getBody() != null) {
                    body = new String(rw.getBody(), StandardCharsets.UTF_8);
                }
                String host = grayForwardHelper.checkForwardHost(uri, body, serverPort);
                if (StringUtils.isNotBlank(host)) {
                    forward(rw, servletResponse, host, uri);
                    return;
                }
                chain.doFilter(rw, servletResponse);
                return;
            }

            if (DebugConstants.GET.equalsIgnoreCase(hsRequest.getMethod())) {
                String queryString = GrayDebugUtils.getParameterFromURL(hsRequest);
                String host = grayForwardHelper.checkForwardHost(uri, paramJson(queryString), serverPort);
                if (StringUtils.isNotBlank(host)) {
                    forward(hsRequest, servletResponse, host, uri);
                    return;
                }
                chain.doFilter(hsRequest, servletResponse);
                return;
            }

            chain.doFilter(hsRequest, servletResponse);
        } catch (ServletException | IOException e) {
            logger.error("GrayFilter request/response error!");
            throw e;
        }
    }

    private String paramJson(String paramIn) {
        if (StringUtils.isBlank(paramIn)) {
            return null;
        }
        paramIn = paramIn.replaceAll("=", "\":\"");
        paramIn = paramIn.replaceAll("&", "\",\"");
        return "{\"" + paramIn + "\"}";
    }

    private String packFormDataBody(HttpServletRequest servletRequest) {
        Enumeration<String> names = servletRequest.getParameterNames();
        Map<String, Object> map = Maps.newHashMap();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = servletRequest.getParameter(name);
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
                map.put(name, value);
            }
        }
        return JacksonUtil.bean2Json(map);
    }

    /**
     * http x-www-form-urlencoded请求跳转
     */
    private void forwardFormUrlencoded(HttpServletRequest hsRequest, ServletResponse servletResponse, String host, String uri) throws IOException {
        try {
            final String rHost = getRandomHost(host);
            logger.info("gray>>>>>forward host : " + rHost);
            List<Header> list = grayForwardHelper.getOriginalHeaders(hsRequest);
            for (int i = 0; i < list.size(); i++) {
                Header header = list.get(i);
                if (header.getName().equalsIgnoreCase(DebugConstants.CONTENT_TYPE) && header.getValue().toLowerCase().contains(DebugConstants.FORM_URL_ENCODED)) {
                    list.remove(i);
                    break;
                }
            }
            // 组装x-www-form-urlencoded消息体
            ArrayList<BasicNameValuePair> items = new ArrayList<>();
            Enumeration<String> names = hsRequest.getParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String value = hsRequest.getParameter(name);
                items.add(new BasicNameValuePair(name, value));
            }
            uri = uriQueryString(uri, hsRequest);
            Header[] headers = list.toArray(new Header[0]);
            CloseableHttpResponse forwardResponse = grayForwardHelper.execute(DebugConstants.HTTP_PREFIX + rHost, uri, hsRequest.getMethod(), new UrlEncodedFormEntity(items), headers);
            grayForwardHelper.setForwardResponse((HttpServletResponse) servletResponse, forwardResponse);
        } finally {
            clearContext();
        }
    }

    /**
     * http 请求跳转
     */
    private void forward(HttpServletRequest hsRequest, ServletResponse servletResponse, String host, String uri) throws IOException {
        try {
            final String rHost = getRandomHost(host);
            logger.info("gray>>>>>forward host : " + rHost);
            List<Header> list = grayForwardHelper.getOriginalHeaders(hsRequest);
            Header[] headers = list.toArray(new Header[0]);
            String method = hsRequest.getMethod();
            HttpEntity entity = null;
            if (!method.equalsIgnoreCase(DebugConstants.GET)) {
                entity = new InputStreamEntity(hsRequest.getInputStream());
            }
            uri = uriQueryString(uri, hsRequest);

            CloseableHttpResponse forwardResponse = grayForwardHelper.execute(DebugConstants.HTTP_PREFIX + rHost, uri, method, entity, headers);
            grayForwardHelper.setForwardResponse((HttpServletResponse) servletResponse, forwardResponse);
        } finally {
            clearContext();
        }
    }

    private String uriQueryString(String uri, HttpServletRequest servletRequest) {
        if (StringUtils.isNotBlank(servletRequest.getQueryString())) {
            uri = uri + "?" + servletRequest.getQueryString();
        }
        return uri;
    }

    /**
     * 多个灰度实例的情况下随机访问
     */
    private String getRandomHost(String host) {
        if (host.contains(StrPool.COMMA)) {
            String[] arr = host.split(StrPool.COMMA);
            Random d = new Random();
            return arr[d.nextInt(arr.length)];
        }
        return host;
    }

    private void clearContext() {
        TraceContext.cleanAll();
        HeadersContext.clean();
        MDC.clear();
    }

}
