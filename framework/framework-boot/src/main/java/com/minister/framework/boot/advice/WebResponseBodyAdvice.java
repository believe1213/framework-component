package com.minister.framework.boot.advice;

import com.minister.component.utils.JacksonUtil;
import com.minister.framework.api.entity.ResponseDto;
import com.minister.framework.api.utils.ResponseDtoFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;
import java.util.Set;

/**
 * WebResponseBodyAdvice
 *
 * @author QIUCHANGQING620
 * @date 2020-02-21 07:01
 */
@Component
@ControllerAdvice
@Slf4j
public class WebResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Value("${framework-boot.framework-boot.response-body-advice-enable:true}")
    private Boolean responseBodyAdviceEnable;

    @Value("${framework-boot.response-body-advice-white-uri:swagger-resources,api-docs}")
    private Set<String> responseBodyAdviceWhiteUri;

    /**
     * 判断是否需要调用 beforeBodyWrite 方法 [即:在返回数据之前,是否需要对数据进行处理]
     */
    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Class<? extends HttpMessageConverter<?>> aClass) {
        return responseBodyAdviceEnable;
    }

    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter methodParameter, @NonNull MediaType mediaType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectConverterClass,
                                  @NonNull ServerHttpRequest serverHttpRequest, @NonNull ServerHttpResponse serverHttpResponse) {
        if (isWhiteUrl(serverHttpRequest)) {
            return body;
        }

        // 仅处理 MediaType=application/json 类型请求
        if (!MediaType.APPLICATION_JSON.equals(mediaType) &&
                !MediaType.APPLICATION_JSON_UTF8.equals(mediaType)) {
            log.debug("MediaType is " + mediaType + ", skip advice");
            return body;
        }

        if (body instanceof ResponseDto) {
            log.debug("body is ResponseDto, skip advice");
            return body;
        }
        String path = serverHttpRequest.getURI().getPath();
        if ("/error".equals(path)) {
            log.debug("request error, skip advice");
            try {
                int statusCode = ((ServletServerHttpResponse) serverHttpResponse).getServletResponse().getStatus();
                HttpStatus httpStatus = HttpStatus.resolve(statusCode);
                if (Objects.nonNull(httpStatus)) {
                    return ResponseDtoFactory.error(String.valueOf(statusCode), httpStatus.getReasonPhrase());
                }
            } catch (Exception ignored) {
            }
            return body;
        }

        log.debug("before response body advice : {}", JacksonUtil.bean2Json(body));
        ResponseDto<Object> responseDto = ResponseDtoFactory.success(body);
        log.debug("after response body advice : {}", JacksonUtil.bean2Json(responseDto));

        if (selectConverterClass.isAssignableFrom(StringHttpMessageConverter.class)) {
            return JacksonUtil.bean2Json(responseDto);
        }

        return responseDto;
    }

    private boolean isWhiteUrl(ServerHttpRequest request) {
        Set<String> whiteUrlList = responseBodyAdviceWhiteUri;

        for (String s : whiteUrlList) {
            if (request.getURI().getRawPath().contains(s)) {
                return true;
            }
        }

        return false;
    }

}
