package com.minister.framework.debug.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 普通的http请求简化封装
 *
 * @author QIUCHANGQING620
 * @date 2024-05-01 00:00
 */
@Slf4j
public class HttpCaller {

    //单位毫秒
    private static int socketTimeout = 12000;

    /**
     * 接口说明：post请求（默认编码为 Content-Type →application/json;charset=UTF-8）
     *
     * @param url 访问地址
     * @param msg 请求body
     * @return
     */
    public static String post(String url, String msg) throws Exception {
        Header[] headers = new Header[1];
        headers[0] = new BasicHeader("Content-Type", "application/json");
        return post(url, msg, "UTF-8", headers, socketTimeout);
    }

    /**
     * 接口说明：post请求（默认编码为utf-8）
     *
     * @param url    访问地址
     * @param msg    请求body
     * @param header 请求头信息
     * @return
     */
    public static String post(String url, String msg, Header header) throws Exception {
        Header[] headers = new Header[1];
        headers[0] = header;
        return post(url, msg, "UTF-8", headers, socketTimeout);
    }

    /**
     * 接口说明：post请求（默认编码为utf-8）
     *
     * @param url     访问地址
     * @param msg     请求body
     * @param headers 请求头信息
     * @return
     */
    public static String post(String url, String msg, Header[] headers) throws Exception {
        return post(url, msg, "UTF-8", headers, socketTimeout);
    }

    /**
     * 接口说明：post请求
     *
     * @param url     访问地址
     * @param msg     请求body
     * @param charset 编码
     * @param header  请求头信息
     * @return
     */
    public static String post(String url, String msg, String charset, Header header) throws Exception {
        Header[] headers = new Header[1];
        headers[0] = header;
        return post(url, msg, charset, headers, socketTimeout);
    }

    /**
     * 接口说明：post请求
     *
     * @param url
     * @param msg
     * @param charset
     * @param header
     * @param socketTimeout 毫秒
     * @return
     * @throws Exception
     */
    public static String post(String url, String msg, String charset, Header header, int socketTimeout) throws Exception {
        Header[] headers = new Header[1];
        headers[0] = header;
        return post(url, msg, charset, headers, socketTimeout);
    }

    /**
     * 接口说明：post请求
     *
     * @param url     访问地址
     * @param msg     请求body
     * @param charset 编码
     * @param headers 请求头信息
     * @return
     */
    public static String post(String url, String msg, String charset, Header[] headers, int socketTimeout) throws Exception {
        //为了保护系统稳定性，限定外部调用超时时间为3秒
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(socketTimeout)
                .setConnectionRequestTimeout(socketTimeout)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setRetryHandler((exception, executionCount, context) -> {
                    if (executionCount > 1) {
                        // Do not retry if over max retry count
                        return false;
                    }
                    if (exception instanceof Exception) {
                        log.warn("请求链接异常重试,url={}, 次数={}。。。。msg={}", url, executionCount, msg);
                        return true;
                    }
                    return false;
                })
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
        CloseableHttpResponse httpResponse = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(msg, charset);
            httpPost.setHeaders(headers);
            httpPost.setEntity(entity);
            httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
                String rsp = new BufferedReader(new InputStreamReader(responseEntity.getContent(), "UTF-8")).lines().collect(Collectors.joining("\n"));
                return rsp;
            }
        } catch (Exception e) {
            log.error("主动请求-请求异常：", e);
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
        return null;
    }

    /**
     * 接口说明：get请求
     *
     * @param url     访问地址
     * @param msg     请求body
     * @param headers 请求头信息
     * @return
     */
    public static String get(String url, Map<String, Object> msg, String charset, Header[] headers) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;

        try {
            StringBuilder params = null;
            if (msg != null) {
                params = new StringBuilder();
                for (Map.Entry<String, Object> element : msg.entrySet()) {
                    params.append(element.getKey());
                    params.append("=");
                    params.append(URLEncoder.encode(element.getValue().toString(), "UTF-8"));
                    params.append("&");
                }
            }
            String urlString = params == null ? url : url + "?" + params.toString();
            HttpGet httpGet = new HttpGet(urlString);
            if (headers != null) {
                httpGet.setHeaders(headers);
            }
            httpResponse = httpClient.execute(httpGet);
            HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
                String rsp = new BufferedReader(new InputStreamReader(responseEntity.getContent())).lines().collect(Collectors.joining("\n"));
                return rsp;
            }
        } catch (Exception e) {
            log.error("主动请求-请求异常：", e);
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
        return null;
    }

    /**
     * 接口说明：get请求
     *
     * @param url     访问地址
     * @param msg     请求body
     * @param headers 请求头信息
     * @return
     */
    public static String get(String url, Map<String, Object> msg, Header[] headers) throws Exception {
        return get(url, msg, "UTF-8", headers);
    }

    /**
     * 接口说明：get请求（默认编码为utf-8）
     *
     * @param url 访问地址
     * @param msg 请求参数
     * @return
     */
    public static String get(String url, Map<String, Object> msg) throws Exception {
        return get(url, msg, "UTF-8", null);
    }

    /**
     * 接口说明：get请求（默认编码为utf-8）
     *
     * @param url 访问地址
     * @return
     */
    public static String get(String url) {
        try {
            return get(url, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
