package com.minister.framework.debug.core.gray.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.minister.component.utils.constants.HeadersKey;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.entity.HeaderEntity;
import com.minister.framework.debug.core.gray.entity.GrayTaskDispenseEntity;
import com.minister.framework.debug.core.gray.enums.GrayType;
import com.minister.framework.debug.core.gray.utils.GrayRuleConditionUtil;
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
import java.util.*;

import static com.minister.framework.debug.core.gray.enums.GrayType.*;

/**
 * 灰度转发Helper
 *
 * @author QIUCHANGQING620
 * @date 2024-04-24 00:30
 */
@Slf4j
public class GrayForwardHelper {

    /**
     * 把枪和外部司机APP灰度头 isGray=1灰度
     */
    public static final String APP_GRAY_HEADER = "isGray";

    /**
     * header灰度前缀
     */
    public static final String GRAY_HEADER_PREFIX = "X-framework-";

    public static final String X_APP_GRAY_HEADER = GRAY_HEADER_PREFIX + APP_GRAY_HEADER;

    // 单位毫秒
    private static int socketTimeout = 10000;
    private static int connectTimeout = 10000;
    private static int connectionRequestTimeout = 10000;

    private static volatile Map<String, GrayTaskDispenseEntity.RuleItem> grayRuleCacheMap = new CaseInsensitiveMap<>();

    /**
     * 刷新灰度配置到缓存
     */
    public static void refreshGrayRuleCache(Map<String, GrayTaskDispenseEntity.RuleItem> map) {
        if (map == null) {
            grayRuleCacheMap = Maps.newHashMap();
        } else {
            grayRuleCacheMap = new CaseInsensitiveMap<>(map);
        }
    }

    /**
     * 获取所有灰度配置
     */
    public static Map<String, GrayTaskDispenseEntity.RuleItem> getGrayRuleCache() {
        return grayRuleCacheMap;
    }

    public static boolean isCacheEmpty() {
        return MapUtil.isEmpty(grayRuleCacheMap);
    }

    /**
     * 检测是否需要转发,并返回转换host
     */
    public String checkForwardHost(String uri, String body, String port) {
        // 灰度节点不判断
        if (GrayDebugUtils.isGrayHost()) {
            return null;
        }
        if (grayRuleCacheMap.size() <= 0) {
            return null;
        }

        // 获取转发host
        String configHost = getForwardHost(uri, body);
        if (StringUtils.isBlank(configHost)) {
            return null;
        }

        // 获取本机host
        String registerIp = GrayDebugUtils.getRegisterIp();
        String myHost = registerIp + StrPool.COLON + port;

        List<String> hostList = Lists.newArrayList();
        for (String host : configHost.split(StrPool.COMMA)) {
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
     * 获取转发host
     */
    private String getForwardHost(String uri, String body) {
        HeaderEntity headerEntity = HeadersContext.getHeaderEntity();
        if (Objects.isNull(headerEntity)) {
            return null;
        }
        String mobile = headerEntity.getMobile();
        // 手机号
        if (existGrayType(MOBILE.getType())) {
            String host = getHostByMobile(mobile);
            if (StringUtils.isNotBlank(host)) {
                return host;
            }
        }
        // URI
        if (existGrayType(URI.getType())) {
            String host = getHostByUri(uri, mobile, body);
            if (StringUtils.isNotBlank(host)) {
                return host;
            }
        }
        // 自定义头
        if (existGrayType(HEADER.getType())) {
            return getHostByRequestHeader(headerEntity.getGray());
        }
        return null;
    }

    /**
     * 判断是否存在此类型配置 ，tue表示存在
     */
    private boolean existGrayType(Integer grayType) {
        if (grayRuleCacheMap.size() > 0) {
            Collection<GrayTaskDispenseEntity.RuleItem> collection = grayRuleCacheMap.values();
            for (GrayTaskDispenseEntity.RuleItem rule : collection) {
                if (rule.getGrayType().equals(grayType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据手机号获取主机信息
     */
    public String getHostByMobile(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return null;
        }

        String key = MOBILE.getType() + "#" + mobile;
        GrayTaskDispenseEntity.RuleItem ruleItem = grayRuleCacheMap.get(key);
        if (Objects.nonNull(ruleItem)) {
            return ruleItem.getHost();
        }

        return null;
    }

    /**
     * 根据uri获取主机信息
     */
    public String getHostByUri(String uri, String mobile, String body) {
        String key = URI.getType() + "#" + uri;
        GrayTaskDispenseEntity.RuleItem ruleItem = grayRuleCacheMap.get(key);
        if (Objects.isNull(ruleItem)) {
            return null;
        }
        if (StringUtils.isBlank(ruleItem.getFilter())) {
            return ruleItem.getHost();
        }
        String filter = ruleItem.getFilter();
        // 手机号匹配
        if (StringUtils.isNotBlank(mobile) && StrUtil.isWrap(filter, "^(", ")")) {
            filter = StrUtil.unWrap(filter, "^(", ")");
            if (StringUtils.isBlank(filter)) {
                return null;
            }
            for (String str : filter.split(StrPool.COMMA)) {
                if (StrUtil.equals(str, mobile)) {
                    return ruleItem.getHost();
                }
            }
            return null;
        }
        // 参数匹配
        if (StringUtils.isNotBlank(body)) {
            if (GrayRuleConditionUtil.checkFilter(body, ruleItem.getFilter())) {
                return ruleItem.getHost();
            }
            return null;
        }

        return null;
    }

    /**
     * 根据请求头中x-gray值获取跳转主机
     */
    public String getHostByRequestHeader(String headerValue) {
        List<GrayTaskDispenseEntity.RuleItem> configs = new ArrayList<>();
        for (GrayTaskDispenseEntity.RuleItem ruleItem : grayRuleCacheMap.values()) {
            if (ruleItem.getGrayType().equals(HEADER.getType())) {
                configs.add(ruleItem);
            }
        }
        for (GrayTaskDispenseEntity.RuleItem ruleItem : configs) {
            if (StringUtils.isNotBlank(headerValue) && headerValue.equalsIgnoreCase(ruleItem.getFilter())) {
                return ruleItem.getHost();
            }
        }
        return null;
    }

    private String splitHost(String host) {
        if (StringUtils.isBlank(host)) {
            return null;
        }
        return host.split(":", 2)[0];
    }

    /**
     * 根据灰度类型获取灰度IP
     *
     * @param grayType    灰度类型
     * @param typeOfValue 灰度值
     * @param body        请求体
     * @return 返回跳转主机
     */
    public String getGrayIpByGayType(GrayType grayType, String typeOfValue, String body) {
        if (StringUtils.isBlank(typeOfValue)) {
            return null;
        }
        if (grayType.equals(TOPIC) || grayType.equals(QUEUE)) {
            String key = grayType.getType() + "#" + typeOfValue;
            GrayTaskDispenseEntity.RuleItem grayRule = grayRuleCacheMap.get(key);
            if (grayRule != null) {
                String filter = grayRule.getFilter();
                if (StringUtils.isBlank(filter)) {
                    return splitHost(grayRule.getHost());
                }
                if (GrayRuleConditionUtil.checkFilter(body, filter)) {
                    return splitHost(grayRule.getHost());
                }
            }
        }
        if (grayType.equals(MOBILE)) {
            String phoneKey = MOBILE.getType() + "#" + typeOfValue;
            GrayTaskDispenseEntity.RuleItem grayRule = grayRuleCacheMap.get(phoneKey);
            if (grayRule != null) {
                return splitHost(grayRule.getHost());
            }
        }
        if (grayType.equals(HEADER)) {
            String key = grayType.getType() + "#" + HeadersKey.GRAY;
            GrayTaskDispenseEntity.RuleItem grayRule = grayRuleCacheMap.get(key);
            if (grayRule != null && body.equals(grayRule.getFilter())) {
                return splitHost(grayRule.getHost());
            }
        }
        return null;
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
            //返回前端灰度标识
            httpServletResponse.setHeader(X_APP_GRAY_HEADER, "1");

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
