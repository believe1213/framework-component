package com.minister.framework.boot.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * WebRequestBodyAdvice
 *
 * @author QIUCHANGQING620
 * @date 2020-02-26 17:32
 */
//@Component
//@ControllerAdvice
@Slf4j
public class WebRequestBodyAdvice implements RequestBodyAdvice {

    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return false;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        try {
            return new MyHttpInputMessage(httpInputMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return httpInputMessage;
        }
    }

    @Override
    public Object afterBodyRead(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;
    }

    @Override
    public Object handleEmptyBody(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;
    }

    public class MyHttpInputMessage implements HttpInputMessage {
        private HttpHeaders headers;
        private InputStream body;

        @SuppressWarnings("unchecked")
        public MyHttpInputMessage(HttpInputMessage inputMessage) throws Exception {
//            String oldBodyStr = IOUtils.toString(inputMessage.getBody(), "UTF-8");
//            String contextPath = request.getContextPath();
//            String uri = request.getRequestURI().substring(contextPath.length());
//
//            this.headers = inputMessage.getHeaders();
//            this.body = IOUtils.toInputStream(oldBodyStr, "UTF-8");
        }

        @Override
        public InputStream getBody() throws IOException {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }

}
