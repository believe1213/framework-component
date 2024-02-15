package com.minister.framework.boot.utils;

import cn.hutool.core.text.StrPool;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 全局 RestTemplate 工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-06-13 16:34
 */
@Slf4j
@Component
public class RestTemplateUtil {

    private RestTemplateUtil() {
    }

    private static CustomRestTemplateUtil CUSTOM_REST_TEMPLATE_UTIL;

    private static final String NAMESPACE = "customRestTemplate";

    private static final String KEY_PREFIX = "customRestTemplate";

    private static final String TIME_TO_LIVE_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "timeToLive";

    private static final String MAX_TOTAL_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "maxTotal";

    private static final String MAX_PER_ROUTE_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "maxPerRoute";

    private static final String CONNECTION_REQUEST_TIMEOUT_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "connectionRequestTimeout";

    private static final String CONNECT_TIMEOUT_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "connectTimeout";

    private static final String SOCKET_TIMEOUT_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "socketTimeout";

    private static final String PROXY_ENABLE_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "proxyEnable";

    private static final String PROXY_IP_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "proxyIp";

    private static final String PROXY_PORT_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "proxyPort";

    private static final String PROXY_ACCOUNT_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "proxyAccount";

    private static final String PROXY_PASSWORD_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "proxyPassword";

    private static final String RETRY_COUNT_PROPERTY_KEY = KEY_PREFIX + StrPool.DOT + "retryCount";

    @Resource
    private Environment environment;

    @PostConstruct
    public void initGlobalRestTemplate() {
        // 获取配置项
        Integer timeToLive = environment.getProperty(TIME_TO_LIVE_PROPERTY_KEY, Integer.class);
        Integer maxTotal = environment.getProperty(MAX_TOTAL_PROPERTY_KEY, Integer.class);
        Integer maxPerRoute = environment.getProperty(MAX_PER_ROUTE_PROPERTY_KEY, Integer.class);
        Integer connectionRequestTimeout = environment.getProperty(CONNECTION_REQUEST_TIMEOUT_PROPERTY_KEY, Integer.class);
        Integer connectTimeout = environment.getProperty(CONNECT_TIMEOUT_PROPERTY_KEY, Integer.class);
        Integer socketTimeout = environment.getProperty(SOCKET_TIMEOUT_PROPERTY_KEY, Integer.class);
        Boolean proxyEnable = environment.getProperty(PROXY_ENABLE_PROPERTY_KEY, Boolean.class);
        String proxyIp = environment.getProperty(PROXY_IP_PROPERTY_KEY);
        Integer proxyPort = environment.getProperty(PROXY_PORT_PROPERTY_KEY, Integer.class);
        String proxyAccount = environment.getProperty(PROXY_ACCOUNT_PROPERTY_KEY);
        String proxyPassword = environment.getProperty(PROXY_PASSWORD_PROPERTY_KEY);
        Integer retryCount = environment.getProperty(RETRY_COUNT_PROPERTY_KEY, Integer.class);
        // build
        CUSTOM_REST_TEMPLATE_UTIL = CustomRestTemplateUtilBuilder.instance()
                .poolingHttpClientConnectionManager(timeToLive, maxTotal, maxPerRoute)
                .requestConfig(connectionRequestTimeout, connectTimeout, socketTimeout)
                .proxy(proxyEnable, proxyIp, proxyPort, proxyAccount, proxyPassword)
                .closeableHttpClient(retryCount)
                .build();
    }

    @ApolloConfigChangeListener(value = NAMESPACE, interestedKeyPrefixes = KEY_PREFIX)
    public void globalCfgChange(ConfigChangeEvent changeEvent) {
        log.info(KEY_PREFIX + " cfg change");
        for (String str : changeEvent.changedKeys()) {
            String newStr = changeEvent.getChange(str).getNewValue();
            switch (str) {
                case TIME_TO_LIVE_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setTimeToLive(Integer.parseInt(newStr));
                    break;
                case MAX_TOTAL_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setMaxTotal(Integer.parseInt(newStr));
                    break;
                case MAX_PER_ROUTE_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setMaxPerRoute(Integer.parseInt(newStr));
                    break;
                case CONNECTION_REQUEST_TIMEOUT_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setConnectionRequestTimeout(Integer.parseInt(newStr));
                    break;
                case CONNECT_TIMEOUT_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setConnectTimeout(Integer.parseInt(newStr));
                    break;
                case SOCKET_TIMEOUT_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setSocketTimeout(Integer.parseInt(newStr));
                    break;
                case PROXY_ENABLE_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setProxyEnable(Boolean.parseBoolean(newStr));
                    break;
                case PROXY_IP_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setProxyIp(newStr);
                    break;
                case PROXY_PORT_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setProxyPort(Integer.parseInt(newStr));
                    break;
                case PROXY_ACCOUNT_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setProxyAccount(newStr);
                    break;
                case PROXY_PASSWORD_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setProxyPassword(newStr);
                    break;
                case RETRY_COUNT_PROPERTY_KEY:
                    CUSTOM_REST_TEMPLATE_UTIL.setRetryCount(Integer.parseInt(newStr));
                    break;
                default:
                    break;
            }
        }
        CUSTOM_REST_TEMPLATE_UTIL.buildRestTemplate();
    }

    /**
     * 获取自定义的RestTemplate
     * 此工具类需要在@PostConstruct后才能初始化完成
     *
     * @return RestTemplate
     */
    public static RestTemplate getRestTemplate() {
        return CUSTOM_REST_TEMPLATE_UTIL.getRestTemplate();
    }

}
