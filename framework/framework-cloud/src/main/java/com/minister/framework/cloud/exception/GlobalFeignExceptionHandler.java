package com.minister.framework.cloud.exception;

import com.minister.component.utils.ExUtil;
import com.minister.component.utils.enums.FrameworkExEnum;
import com.minister.component.utils.enums.HttpCodeEnum;
import com.minister.framework.api.entity.ResponseDto;
import com.minister.framework.api.utils.ResponseDtoFactory;
import com.minister.framework.boot.exception.FeignException;
import com.netflix.client.ClientException;
import feign.RetryableException;
import feign.codec.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 常见feign异常调用处理
 *
 * @author QIUCHANGQING620
 * @date 2020-08-06 14:17
 */
@RestControllerAdvice
@Slf4j
public class GlobalFeignExceptionHandler {

    @ExceptionHandler(feign.FeignException.class)
    public ResponseDto<?> handlerFeignException(feign.FeignException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.FEIGN_EX);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), FrameworkExEnum.FEIGN_EX.getMsg()), ex);
        return responseDto;
    }

    @ExceptionHandler(RetryableException.class)
    protected ResponseDto<?> handlerRetryableException(RetryableException ex, HttpServletRequest request) {
        //  connect timed out executing GET http://demo/test/test
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.SERVICE_OFFLINE);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), FrameworkExEnum.SERVICE_OFFLINE.getMsg()));
        return responseDto;
    }

    @ExceptionHandler(ClientException.class)
    protected ResponseDto<?> handlerClientException(ClientException ex, HttpServletRequest request) {
        // Load balancer does not have available server for client: demo
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.SERVICE_NOT_ONLINE);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), FrameworkExEnum.SERVICE_NOT_ONLINE.getMsg()));
        return responseDto;
    }

    @ExceptionHandler(DecodeException.class)
    public ResponseDto<?> handlerBaseException(DecodeException ex, HttpServletRequest request) {
        // FeignException
        FeignException feignException = ExUtil.findExceptionFromStack(ex, FeignException.class);
        if (Objects.nonNull(feignException)) {
            ResponseDto<?> responseData = ResponseDtoFactory.error(feignException.getCode(), feignException.getMessage());

            log.warn(String.format("Exception uri [%s] code : %s, message : %s, traceId : %s", request.getRequestURI(), feignException.getCode(), feignException.getMessage(), feignException.getTraceId()));
            return responseData;
        }

        // ClientException
        ClientException clientException = ExUtil.findExceptionFromStack(ex, ClientException.class);
        if (Objects.nonNull(clientException)) {
            return handlerClientException(clientException, request);
        }

        // default
        ResponseDto<?> responseData = ResponseDtoFactory.error(HttpCodeEnum.InternalServerError, String.format("系统异常：%s,请稍候重试", ex.getMessage()));

        log.error(String.format("Exception uri [%s] system error", request.getRequestURI()), ex);
        return responseData;
    }

}
