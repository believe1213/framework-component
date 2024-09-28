package com.minister.framework.boot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * 请求日志拦截器
 *
 * @author QIUCHANGQING620
 * @date 2024/09/16
 */
@Slf4j
public class HttpRequestLogInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        response(response);

        return response;
    }

    private void request(HttpRequest request, byte[] body) {
        log.info("Request: {} {}", request.getMethod(), request.getURI());
        log.info("Request Headers: {}", request.getHeaders());
    }

    private void response(ClientHttpResponse response) throws IOException {
        log.info("Response Status: {} {}", response.getStatusCode(), response.getStatusText());
        log.info("Response Headers: {}", response.getHeaders());
    }

}
