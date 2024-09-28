package com.minister.framework.debug.core.stg.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.entity.HeaderEntity;
import com.minister.framework.debug.core.stg.cache.MultiVersionCache;
import com.minister.framework.debug.core.stg.entity.MultiVersionEntity;
import com.minister.framework.debug.core.utils.GrayDebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * 多版本测试转发Helper
 *
 * @author QIUCHANGQING620
 * @date 2024-05-02 00:00
 */
@Slf4j
public class MultiVersionForwardHelper {

    // 单位毫秒
    private static int socketTimeout = 10000;
    private static int connectTimeout = 2000;
    private static int connectionRequestTimeout = 2000;

    /**
     * 检测是否需要转发,并返回转换host
     */
    public String checkForwardHost(String port) {
        // 灰度节点不判断
        if (GrayDebugUtils.isGrayHost()) {
            return null;
        }
        if (MultiVersionCache.getMultiVersionCache().size() <= 0) {
            return null;
        }

        HeaderEntity headerEntity = HeadersContext.getHeaderEntity();
        if (Objects.isNull(headerEntity)) {
            return null;
        }
        String envId = headerEntity.getEnvId();
        MultiVersionEntity multiVersion = MultiVersionCache.getMultiVersion(envId);
        if (Objects.isNull(multiVersion) || StringUtils.isBlank(multiVersion.getHost())) {
            return null;
        }
        String forwardHost = multiVersion.getHost();

        // 获取本机host
        String registerIp = GrayDebugUtils.getRegisterIp();
        String myHost = registerIp + StrPool.COLON + port;

        List<String> hostList = Lists.newArrayList();
        for (String host : forwardHost.split(StrPool.COMMA)) {
            // 本机不需要跳转
            if (StringUtils.isBlank(host) ||
                    host.contains("127.0.0.1") || host.contains("localhost")) {
                continue;
            }
            // 配置host与当前host一致，不需要跳转
            if (StringUtils.isNotBlank(registerIp)) {
                if (StrUtil.equals(myHost, host)) {
                    continue;
                }
            }
            hostList.add(host);
        }

        if (CollUtil.isEmpty(hostList)) {
            return null;
        }

        return CollUtil.join(hostList, StrPool.COMMA);
    }

    /**
     * 获取原始头信息
     */
    public List<Header> getOriginalHeaders(HttpServletRequest request) {
        List<Header> headerList = new ArrayList<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (GrayDebugUtils.isIncludedHeader(name)) {
                    Enumeration<String> values = request.getHeaders(name);
                    while (values.hasMoreElements()) {
                        String value = values.nextElement();
                        headerList.add(new BasicHeader(name, value));
                    }
                }
            }
        }
        return headerList;
    }

    /**
     * 设置转发响应
     */
    public void setForwardResponse(HttpServletResponse httpServletResponse, HttpResponse forwardResponse) {
        if (Objects.isNull(forwardResponse)) {
            return;
        }
        try {
            httpServletResponse.setStatus(forwardResponse.getStatusLine().getStatusCode());
            Header[] headers = forwardResponse.getAllHeaders();
            if (headers != null && headers.length > 0) {
                for (Header header : headers) {
                    if (GrayDebugUtils.isIncludedHeader(header.getName())) {
                        httpServletResponse.setHeader(header.getName(), header.getValue());
                    }
                }
            }

            if (forwardResponse.getEntity() != null) {
                OutputStream outputStream = httpServletResponse.getOutputStream();
                forwardResponse.getEntity().writeTo(outputStream);
                outputStream.flush();
            } else {
                OutputStream outputStream = httpServletResponse.getOutputStream();
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error("set forward response fail", e);
        }
    }

    /**
     * 建立请求参数对象
     *
     * @param method  请求方式
     * @param uri     路径
     * @param entity  数据流对象
     * @param headers 请求头信息
     */
    protected HttpRequest buildHttpRequest(String method, String uri, HttpEntity entity, Header[] headers) {
        HttpRequest httpRequest;
        switch (method.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uri);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uri);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uri);
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            case "DELETE":
                BasicHttpEntityEnclosingRequest entityRequest = new BasicHttpEntityEnclosingRequest(
                        method, uri);
                httpRequest = entityRequest;
                entityRequest.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(method, uri);
        }

        httpRequest.setHeaders(headers);
        return httpRequest;
    }

    /**
     * 执行http请求
     */
    public CloseableHttpResponse execute(String host, String uri, String method, HttpEntity entity, Header[] headers) {
        HttpRequest httpRequest = buildHttpRequest(method, uri, entity, headers);
        return execute(host, httpRequest);
    }

    /**
     * 执行http请求
     */
    public CloseableHttpResponse execute(String host, HttpRequest httpRequest) {
        //为了保护系统稳定性，限定调用超时时间为3秒
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
        try {
            return httpClient.execute(HttpHost.create(host), httpRequest);
        } catch (Exception e) {
            log.error("http request fail", e);
        }
        return null;
    }

}
