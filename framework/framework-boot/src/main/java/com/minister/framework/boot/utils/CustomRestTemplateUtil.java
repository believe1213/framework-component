package com.minister.framework.boot.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 个性化 RestTemplate 工具类
 *
 * @author QIUCHANGQING620
 * @date 2020-06-13 16:34
 */
@Slf4j
@Component
public class CustomRestTemplateUtil {

    /**
     * 获取自定义的RestTemplate
     * 此方法需要在 buildRestTemplate() 后才能运行
     *
     * @return RestTemplate
     */
    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    private RestTemplate restTemplate;

    // PoolingHttpClientConnectionManager
    /**
     * 长连接保持时长(s)
     */
    private static final int DEFAULT_TIME_TO_LIVE = 60;
    private int timeToLive = DEFAULT_TIME_TO_LIVE;

    /**
     * 最大连接数
     */
    private static final int DEFAULT_MAX_TOTAL = 800;
    private int maxTotal = DEFAULT_MAX_TOTAL;

    /**
     * 同路由最大并发数
     */
    private static final int DEFAULT_MAX_PER_ROUTE = 200;
    private int maxPerRoute = DEFAULT_MAX_PER_ROUTE;

    // RequestConfig
    /**
     * 从池中获取链接超时时间(ms)
     */
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 10000;
    private int connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;

    /**
     * 建立链接超时时间(ms)
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
    private int connectTimeout = DEFAULT_CONNECTION_TIMEOUT;

    /**
     * 读取超时时间(ms)
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 20000;
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    // proxy
    /**
     * 代理开关
     */
    private static final boolean DEFAULT_PROXY_ENABLE = false;
    private boolean proxyEnable = DEFAULT_PROXY_ENABLE;

    /**
     * 代理Ip
     */
    private static final String DEFAULT_PROXY_IP = "10.37.84.94";
    private String proxyIp = DEFAULT_PROXY_IP;

    /**
     * 代理端口
     */
    private static final int DEFAULT_PROXY_PORT = 8080;
    private int proxyPort = DEFAULT_PROXY_PORT;

    /**
     * 代理账号
     */
    private static final String DEFAULT_PROXY_ACCOUNT = StringUtils.EMPTY;
    private String proxyAccount = DEFAULT_PROXY_ACCOUNT;

    /**
     * 代理密码
     */
    private static final String DEFAULT_PROXY_PASSWORD = StringUtils.EMPTY;
    private String proxyPassword = DEFAULT_PROXY_PASSWORD;

    // CloseableHttpClient
    /**
     * 重试次数
     */
    private static final int DEFAULT_RETRY_COUNT = 2;
    private int retryCount = DEFAULT_RETRY_COUNT;

    public void buildRestTemplate() {
        Registry<ConnectionSocketFactory> customRegistry = getCustomRegistry();
        if (customRegistry == null) {
            log.error("initCustomRestTemplate fail.");
            return;
        }

        // 创建请求连接池
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(customRegistry,
                null, null, null, this.timeToLive, TimeUnit.SECONDS);
        // 设置最大连接数
        connectionManager.setMaxTotal(this.maxTotal);
        // 设置同路由最大并发数
        connectionManager.setDefaultMaxPerRoute(this.maxPerRoute);

        // 创建请求参数
        RequestConfig requestConfig = RequestConfig.custom()
                // 一、从池中获取链接超时时间：connectionRequestTimeout-->指的是连接不够用时的等待时间
                .setConnectionRequestTimeout(this.connectionRequestTimeout)
                // 二、连接目标服务器超时时间：ConnectionTimeout-->指的是连接一个url的连接等待时间
                .setConnectTimeout(this.connectTimeout)
                // 三、读取目标服务器数据超时时间：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
                .setSocketTimeout(this.socketTimeout)
                .build();

        // 创建默认请求headers
        Collection<Header> headers = new ArrayList<>();
//            headers.add(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml,application/json,text/plain;q=0.9,image/webp,*/*;q=0.8"));
//            headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36"));
//            headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"));
//            headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8"));
        headers.add(new BasicHeader("Connection", "Keep-Alive"));

        // 创建http客户端
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(this.retryCount, true))
                .setDefaultHeaders(headers)
                // 保持长连接配置，需要在header添加Keep-Alive
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);

        // 设置代理
        if (this.proxyEnable) {
            if (StringUtils.isBlank(this.proxyAccount) || StringUtils.isBlank(this.proxyPassword)) {
                log.error("proxyAccount or proxyPassword can not be null");
            } else {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        AuthScope.ANY,
                        new NTCredentials(this.proxyAccount, this.proxyPassword, null, null)
                );
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            httpClientBuilder.setProxy(new HttpHost(this.proxyIp, this.proxyPort));
        }

        CloseableHttpClient httpClient = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 初始化 RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(httpRequestFactory);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        restTemplate.getMessageConverters().stream()
                .filter(messageConverter -> messageConverter instanceof StringHttpMessageConverter)
                .findFirst()
                .ifPresent(messageConverter ->
                        ((StringHttpMessageConverter) messageConverter).setDefaultCharset(StandardCharsets.UTF_8));

        log.info("build restTemplate success.");
        this.restTemplate = restTemplate;
    }

    /**
     * 绕过SSL证书校验
     *
     * @return Registry<ConnectionSocketFactory>
     */
    private static Registry<ConnectionSocketFactory> getCustomRegistry() {
        try {
            // 初始化 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,
                    new TrustManager[]{new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }},
                    new SecureRandom());

            // 设置协议http和https对应的处理socket链接工厂的对象
            return RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslContext, (hostname, session) -> true))
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("getCustomRegistry fail.", e);
        }

        return null;
    }

    public void setTimeToLive(Integer timeToLive) {
        this.timeToLive = timeToLive == null ? DEFAULT_TIME_TO_LIVE : timeToLive;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal == null ? DEFAULT_MAX_TOTAL : maxTotal;
    }

    public void setMaxPerRoute(Integer maxPerRoute) {
        this.maxPerRoute = maxPerRoute == null ? DEFAULT_MAX_PER_ROUTE : maxPerRoute;
    }

    public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout == null ? DEFAULT_CONNECTION_REQUEST_TIMEOUT : connectionRequestTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout == null ? DEFAULT_CONNECTION_TIMEOUT : connectTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout == null ? DEFAULT_SOCKET_TIMEOUT : socketTimeout;
    }

    public void setProxyEnable(Boolean proxyEnable) {
        this.proxyEnable = proxyEnable == null ? DEFAULT_PROXY_ENABLE : proxyEnable;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = StringUtils.isBlank(proxyIp) ? DEFAULT_PROXY_IP : proxyIp;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort == null ? DEFAULT_PROXY_PORT : proxyPort;
    }

    public void setProxyAccount(String proxyAccount) {
        this.proxyAccount = StringUtils.isBlank(proxyAccount) ? DEFAULT_PROXY_ACCOUNT : proxyAccount;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = StringUtils.isBlank(proxyPassword) ? DEFAULT_PROXY_PASSWORD : proxyPassword;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount == null ? DEFAULT_RETRY_COUNT : retryCount;
    }

}
