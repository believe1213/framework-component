package com.minister.framework.boot.utils;

import cn.hutool.core.util.StrUtil;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 标准 Okhttp RestTemplate 工具类
 *
 * @author QIUCHANGQING620
 * @date 2024/09/16
 */
public class OkHttpRestTemplateUtil {

    private RestTemplate rt;

    // ----- ConnectionPool -----
    /**
     * 最大空闲连接数
     */
    private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 50;
    private int maxIdleConnections = DEFAULT_MAX_IDLE_CONNECTIONS;

    /**
     * 空闲连接保持时间(秒)
     */
    private static final int DEFAULT_KEEP_ALIVE_DURATION = 3 * 60;
    private int keepAliveDuration = DEFAULT_KEEP_ALIVE_DURATION;

    // ----- Dispatcher -----
    /**
     * 最大请求数
     */
    private static final int DEFAULT_MAX_REQUESTS = 100;
    private int maxRequests = DEFAULT_MAX_REQUESTS;

    /**
     * 每个主机最大请求数
     */
    private static final int DEFAULT_MAX_REQUESTS_PER_HOST = 50;
    private int maxRequestsPerHost = DEFAULT_MAX_REQUESTS_PER_HOST;

    // ----- base config -----
    /**
     * 请求连接超时时间(秒)
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    /**
     * 请求读取超时时间(秒)
     */
    private static final int DEFAULT_READ_TIMEOUT = 10;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    // ----- proxy -----
    /**
     * 代理开关
     */
    private static final boolean DEFAULT_PROXY_ENABLE = false;
    private boolean proxyEnable = DEFAULT_PROXY_ENABLE;

    /**
     * 代理IP
     */
    private static final String DEFAULT_PROXY_IP = null;
    private String proxyIp = DEFAULT_PROXY_IP;

    /**
     * 代理端口
     */
    private static final Integer DEFAULT_PROXY_PORT = null;
    private Integer proxyPort = DEFAULT_PROXY_PORT;

    /**
     * 代理账号
     */
    private static final String DEFAULT_PROXY_ACCOUNT = null;
    private String proxyAccount = DEFAULT_PROXY_ACCOUNT;

    /**
     * 代理密码
     */
    private static final String DEFAULT_PROXY_PASSWORD = null;
    private String proxyPassword = DEFAULT_PROXY_PASSWORD;

    public static SSLSocketFactory createSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new X509TrustManager[]{X_509_TRUST_MANAGER}, null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

    }

    public static final X509TrustManager X_509_TRUST_MANAGER = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    public RestTemplate buildRestTemplate() {
        ClientHttpRequestFactory requestFactory = createRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        restTemplate.getMessageConverters().stream()
                .filter(httpMessageConverter -> httpMessageConverter instanceof StringHttpMessageConverter)
                .findFirst().ifPresent(httpMessageConverter ->
                        ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8)
                );

        restTemplate.getInterceptors().add(new HttpRequestLogInterceptor());

        this.rt = restTemplate;
        return restTemplate;
    }

    private ClientHttpRequestFactory createRequestFactory() {
        ConnectionPool connectionPool = new ConnectionPool(this.maxIdleConnections, this.keepAliveDuration, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(this.maxRequests);
        dispatcher.setMaxRequestsPerHost(this.maxRequestsPerHost);
        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .dispatcher(dispatcher)
                .connectTimeout(this.connectTimeout, TimeUnit.SECONDS)
                .readTimeout(this.readTimeout, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .sslSocketFactory(createSSLSocketFactory(), X_509_TRUST_MANAGER);

        if (this.proxyEnable && StrUtil.isNotBlank(this.proxyIp) && Objects.nonNull(this.proxyPort)) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyIp, this.proxyPort));
            builder.proxy(proxy);

            if (StrUtil.isNotBlank(this.proxyAccount) && StrUtil.isNotBlank(this.proxyPassword)) {
                builder.proxyAuthenticator((route, response) -> response.request().newBuilder()
                        .header("Proxy-Authorization", Credentials.basic(this.proxyAccount, this.proxyPassword))
                        .build());
            }
        }

        return new OkHttp3ClientHttpRequestFactory(builder.build());
    }

    public RestTemplate getRestTemplate() {
        return rt;
    }

    public void setMaxIdleConnections(Integer maxIdleConnections) {
        this.maxIdleConnections = Objects.isNull(maxIdleConnections) ? DEFAULT_MAX_IDLE_CONNECTIONS : maxIdleConnections;
    }

    public void setKeepAliveDuration(Integer keepAliveDuration) {
        this.keepAliveDuration = Objects.isNull(keepAliveDuration) ? DEFAULT_KEEP_ALIVE_DURATION : keepAliveDuration;
    }

    public void setMaxRequests(Integer maxRequests) {
        this.maxRequests = Objects.isNull(maxRequests) ? DEFAULT_MAX_REQUESTS : maxRequests;
    }

    public void setMaxRequestsPerHost(Integer maxRequestsPerHost) {
        this.maxRequestsPerHost = Objects.isNull(maxRequestsPerHost) ? DEFAULT_MAX_REQUESTS_PER_HOST : maxRequestsPerHost;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = Objects.isNull(connectTimeout) ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = Objects.isNull(readTimeout) ? DEFAULT_READ_TIMEOUT : readTimeout;
    }

    public void setProxyEnable(Boolean proxyEnable) {
        this.proxyEnable = Objects.isNull(proxyEnable) ? DEFAULT_PROXY_ENABLE : proxyEnable;
    }

    public void setProxyIp(String proxyIp) {
        this.proxyIp = Objects.isNull(proxyIp) ? DEFAULT_PROXY_IP : proxyIp;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = Objects.isNull(proxyPort) ? DEFAULT_PROXY_PORT : proxyPort;
    }

    public void setProxyAccount(String proxyAccount) {
        this.proxyAccount = Objects.isNull(proxyAccount) ? DEFAULT_PROXY_ACCOUNT : proxyAccount;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = Objects.isNull(proxyPassword) ? DEFAULT_PROXY_PASSWORD : proxyPassword;
    }

    public static Build builder() {
        return new Build();
    }

    public static class Build {

        private Integer maxIdleConnections;

        private Integer keepAliveDuration;

        private Integer maxRequests;

        private Integer maxRequestsPerHost;

        private Integer connectTimeout;

        private Integer readTimeout;

        private Boolean proxyEnable;

        private String proxyIp;

        private Integer proxyPort;

        private String proxyAccount;

        private String proxyPassword;

        public Build baseConfig(Integer connectTimeout, Integer readTimeout) {
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            return this;
        }

        public Build pool(Integer maxRequests, Integer maxRequestsPerHost, Integer maxIdleConnections, Integer keepAliveDuration) {
            this.maxRequests = maxRequests;
            this.maxRequestsPerHost = maxRequestsPerHost;
            this.maxIdleConnections = maxIdleConnections;
            this.keepAliveDuration = keepAliveDuration;
            return this;
        }

        public Build proxyEnable(Boolean proxyEnable) {
            this.proxyEnable = proxyEnable;
            return this;
        }

        public Build proxy(String proxyIp, Integer proxyPort) {
            this.proxyEnable = true;
            this.proxyIp = proxyIp;
            this.proxyPort = proxyPort;
            return this;
        }

        public Build proxyAccount(String proxyAccount, String proxyPassword) {
            this.proxyAccount = proxyAccount;
            this.proxyPassword = proxyPassword;
            return this;
        }

        public OkHttpRestTemplateUtil build() {
            OkHttpRestTemplateUtil restTemplateUtil = new OkHttpRestTemplateUtil();
            restTemplateUtil.setMaxIdleConnections(maxIdleConnections);
            restTemplateUtil.setKeepAliveDuration(keepAliveDuration);
            restTemplateUtil.setMaxRequests(maxRequests);
            restTemplateUtil.setMaxRequestsPerHost(maxRequestsPerHost);
            restTemplateUtil.setConnectTimeout(connectTimeout);
            restTemplateUtil.setReadTimeout(readTimeout);
            restTemplateUtil.setProxyEnable(proxyEnable);
            restTemplateUtil.setProxyIp(proxyIp);
            restTemplateUtil.setProxyPort(proxyPort);
            restTemplateUtil.setProxyAccount(proxyAccount);
            restTemplateUtil.setProxyPassword(proxyPassword);
            return restTemplateUtil;
        }

    }

}
