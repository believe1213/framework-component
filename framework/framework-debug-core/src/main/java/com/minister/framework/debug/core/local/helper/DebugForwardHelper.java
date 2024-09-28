package com.minister.framework.debug.core.local.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.minister.component.utils.IpUtil;
import com.minister.component.utils.constants.ApplicationConstant;
import com.minister.component.utils.context.HeadersContext;
import com.minister.component.utils.entity.HeaderEntity;
import com.minister.framework.debug.core.gray.utils.GrayRuleConditionUtil;
import com.minister.framework.debug.core.local.entity.DebugDispenseEntity;
import com.minister.framework.debug.core.local.enums.DebugType;
import com.minister.framework.debug.core.utils.GrayDebugUtils;
import com.minister.framework.debug.core.utils.JsonFilterParseUtil;
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

import static com.minister.framework.debug.core.local.enums.DebugType.*;

/**
 * 本地调试转发Helper
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@Slf4j
public class DebugForwardHelper {

    // 单位毫秒
    private static int socketTimeout = 6500;
    private static int connectTimeout = 6500;
    private static int connectionRequestTimeout = 6500;

    private static volatile Map<String, DebugDispenseEntity.RuleItem> forwardCache = new CaseInsensitiveMap<>();

    /**
     * 存储格式： <applicationName:hostName/ipaddress>
     */
    private static volatile Map<String, String> hostIPRelationCache = new CaseInsensitiveMap<>();

    /**
     * 刷新本地调试配置到缓存
     */
    public static void refreshForwardCache(Map<String, DebugDispenseEntity.RuleItem> map) {
        if (map == null) {
            forwardCache = Maps.newHashMap();
        } else {
            forwardCache = new CaseInsensitiveMap<>(map);
        }
    }

    /**
     * 获取所有本地调试配置
     */
    public static Map<String, DebugDispenseEntity.RuleItem> getAllForwardCache() {
        return forwardCache;
    }

    public static boolean isCacheEmpty() {
        return MapUtil.isEmpty(forwardCache);
    }

    /**
     * 更新主机与NAT IP信息映射
     */
    public static void refreshHostIPRelationCache(Map<String, String> map) {
        if (map != null) {
            hostIPRelationCache = map;
        }
    }

    /**
     * 获取NAT IP信息
     */
    public static String getNatIpAddressByHost(String host) {
        return hostIPRelationCache.get(host);
    }

    /**
     * 检测是否需要转发,并返回转换host
     */
    public String checkForwardHost(String uri, String body) {
        // 灰度节点不判断
        if (GrayDebugUtils.isGrayHost()) {
            return null;
        }
        if (forwardCache.size() <= 0) {
            return null;
        }

        // 获取转发host
        String configHost = getForwardHost(uri, body);
        if (StringUtils.isBlank(configHost)) {
            return null;
        }

        // 获取注册ip和主机信息
        String registerIp = GrayDebugUtils.getRegisterIp();
        String localhostName = GrayDebugUtils.getLocalHostName();

        List<String> hostList = Lists.newArrayList();
        for (String host : configHost.split(StrPool.COMMA)) {
            // 本机不需要跳转
            if (StringUtils.isBlank(host) ||
                    host.contains("127.0.0.1") || host.contains("localhost")) {
                continue;
            }

            if (!isHostName(host)) {
                // 本机不需要跳转
                if (host.contains(registerIp)) {
                    continue;
                }
                hostList.add(host);
            } else {
                //判断跳转主机是否为自己， 是则不需要跳转
                ApplicationConstant applicationConstant = SpringUtil.getBean(ApplicationConstant.class);
                String applicationName = applicationConstant.getApplicationName().toUpperCase();
                String configHostName = splitHost(host);
                if (StrUtil.equals(localhostName, configHostName)) {
                    continue;
                }
                // 则根据主机信息获取NAT IP 信息
                String debugHost = getNatIpAddressByHost(applicationName + ":" + configHostName);
                if (StringUtils.isBlank(debugHost)) {
                    continue;
                }
                hostList.add(debugHost);
            }
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
        return null;
    }

    /**
     * 判断是否存在此类型配置 ，tue表示存在
     */
    private boolean existGrayType(Integer grayType) {
        if (forwardCache.size() > 0) {
            Collection<DebugDispenseEntity.RuleItem> collection = forwardCache.values();
            for (DebugDispenseEntity.RuleItem rule : collection) {
                if (rule.getDebugType().equals(grayType)) {
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
        DebugDispenseEntity.RuleItem ruleItem = forwardCache.get(key);
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
        DebugDispenseEntity.RuleItem ruleItem = forwardCache.get(key);
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

    private String splitHost(String host) {
        if (StringUtils.isBlank(host)) {
            return null;
        }
        return host.split(":", 2)[0];
    }

    /**
     * 判断是否为主机信息，true=主机，false=IP端口
     */
    private boolean isHostName(String forwardHost) {
        if (StringUtils.isBlank(forwardHost)) {
            return false;
        }
        String ip = splitHost(forwardHost);
        return !IpUtil.correctIp(ip);
    }

    /**
     * 根据调试类型获取灰度主机名称
     */
    public String getHostNameByDebugType(DebugType debugType, String typeOfValue, String body) {
        if (StringUtils.isBlank(typeOfValue)) {
            return null;
        }
        if (!TOPIC.equals(debugType) && !QUEUE.equals(debugType)) {
            return null;
        }

        String key = URI.getType() + "#" + typeOfValue;
        DebugDispenseEntity.RuleItem ruleItem = forwardCache.get(key);
        if (Objects.isNull(ruleItem)) {
            return null;
        }
        String filter = ruleItem.getFilter();
        if (StringUtils.isBlank(filter)) {
            return ruleItem.getHost();
        }

        if (JsonFilterParseUtil.checkFilter(body, filter)) {
            return ruleItem.getHost();
        }

        // TODO 考虑 ruleItem.getHost() 是否可能是 ip:port

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
