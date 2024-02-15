package com.minister.framework.boot.utils;

/**
 * 个性化 RestTemplate 构造器
 *
 * @author QIUCHANGQING620
 * @date 2022-06-06 18:16
 */
public class CustomRestTemplateUtilBuilder {

    private final CustomRestTemplateUtil customRestTemplateUtil = new CustomRestTemplateUtil();

    public static CustomRestTemplateUtilBuilder instance() {
        return new CustomRestTemplateUtilBuilder();
    }

    public CustomRestTemplateUtilBuilder poolingHttpClientConnectionManager(Integer timeToLive, Integer maxTotal, Integer maxPerRoute) {
        this.customRestTemplateUtil.setTimeToLive(timeToLive);
        this.customRestTemplateUtil.setMaxTotal(maxTotal);
        this.customRestTemplateUtil.setMaxPerRoute(maxPerRoute);
        return this;
    }

    public CustomRestTemplateUtilBuilder requestConfig(Integer connectionRequestTimeout, Integer connectTimeout, Integer socketTimeout) {
        this.customRestTemplateUtil.setConnectionRequestTimeout(connectionRequestTimeout);
        this.customRestTemplateUtil.setConnectTimeout(connectTimeout);
        this.customRestTemplateUtil.setSocketTimeout(socketTimeout);
        return this;
    }

    public CustomRestTemplateUtilBuilder proxy(String proxyIp, Integer proxyPort) {
        return proxy(true, proxyIp, proxyPort);
    }

    public CustomRestTemplateUtilBuilder proxy(Boolean proxyEnable, String proxyIp, Integer proxyPort) {
        return proxy(proxyEnable, proxyIp, proxyPort, null, null);
    }

    public CustomRestTemplateUtilBuilder proxy(String proxyIp, Integer proxyPort, String proxyAccount, String proxyPassword) {
        return proxy(true, proxyIp, proxyPort, proxyAccount, proxyPassword);
    }

    public CustomRestTemplateUtilBuilder proxy(Boolean proxyEnable, String proxyIp, Integer proxyPort, String proxyAccount, String proxyPassword) {
        this.customRestTemplateUtil.setProxyEnable(proxyEnable);
        this.customRestTemplateUtil.setProxyIp(proxyIp);
        this.customRestTemplateUtil.setProxyPort(proxyPort);
        this.customRestTemplateUtil.setProxyAccount(proxyAccount);
        this.customRestTemplateUtil.setProxyPassword(proxyPassword);
        return this;
    }

    public CustomRestTemplateUtilBuilder closeableHttpClient(Integer retryCount) {
        this.customRestTemplateUtil.setRetryCount(retryCount);
        return this;
    }

    public CustomRestTemplateUtil build() {
        this.customRestTemplateUtil.buildRestTemplate();
        return customRestTemplateUtil;
    }

}
