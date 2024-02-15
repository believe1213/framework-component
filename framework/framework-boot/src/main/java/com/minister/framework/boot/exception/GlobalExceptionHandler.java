package com.minister.framework.boot.exception;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.minister.component.utils.ExUtil;
import com.minister.component.utils.constants.Constants;
import com.minister.component.utils.enums.FrameworkExEnum;
import com.minister.component.utils.enums.HttpCodeEnum;
import com.minister.component.utils.exception.BaseException;
import com.minister.framework.api.entity.ResponseDto;
import com.minister.framework.api.utils.ResponseDtoFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 17:57
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String NOT_READ_EXCEPTION_STR = "Could not read document:";

    /**
     * 请求方式异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseDto<?> handlerHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(HttpCodeEnum.MethodMotAllowed);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), HttpCodeEnum.MethodMotAllowed.getMsg()));
        return responseDto;
    }

    /**
     * 媒体类型异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseDto<?> handlerHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        MediaType contentType = ex.getContentType();
        String msg = Objects.nonNull(contentType) ? String.format("请求类型(Content-Type) [%s] 与实际接口的请求类型不匹配.", contentType) : HttpCodeEnum.NotAcceptable.getMsg();
        ResponseDto<?> responseDto = ResponseDtoFactory.error(HttpCodeEnum.NotAcceptable, msg);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), msg));
        return responseDto;
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseDto<?> handlerMissingRequestHeaderException(MissingRequestHeaderException ex, HttpServletRequest request) {
        String msg = String.format("缺少必须的 [%s] 类型请求头 [%s]", ex.getParameter().getNestedParameterType().getSimpleName(), ex.getHeaderName());
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.MISSING_HEADER_EX, msg);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), msg));
        return responseDto;
    }

    /**
     * 缺少入参异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseDto<?> handlerMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String msg = String.format("缺少必须的 [%s] 类型参数 [%s]", ex.getParameterType(), ex.getParameterName());
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.MISSING_PARAM_EX, msg);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), msg));
        return responseDto;
    }

    /**
     * 请求中必须至少包含一个有效文件异常
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseDto<?> handlerMissingServletRequestPartException(MissingServletRequestPartException ex, HttpServletRequest request) {
        String msg = FrameworkExEnum.MISSING_PART_EX.getMsg();
        if (StringUtils.isNotBlank(ex.getRequestPartName())) {
            msg = String.format("缺少必须的 [文件] 类型参数 [%s]", ex.getRequestPartName());
        }
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.MISSING_PART_EX, msg);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), msg));
        return responseDto;
    }

    /**
     * 获取文件异常
     */
    @ExceptionHandler(MultipartException.class)
    protected ResponseDto<?> handlerMultipartException(MultipartException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.MULTI_PART_EX);

        log.warn(String.format("Exception uri [%s] %s : %s", request.getRequestURI(), responseDto.getMsg(), ex.getMessage()));
        return responseDto;
    }

    @ExceptionHandler(ServletException.class)
    protected ResponseDto<?> handlerServletException(ServletException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto;
        String msg = "UT010016: Not a multi part request";
        if (msg.equalsIgnoreCase(ex.getMessage())) {
            responseDto = ResponseDtoFactory.error(FrameworkExEnum.MISSING_PART_EX);
        } else {
            responseDto = ResponseDtoFactory.error(HttpCodeEnum.InternalServerError, ex.getMessage());
        }

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), responseDto.getMsg()), ex);
        return responseDto;
    }

    /**
     * 入参解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseDto<?> handlerHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String msg = FrameworkExEnum.PARAM_READ_EX.getMsg();
        if (ex.getCause() instanceof MismatchedInputException) {
            MismatchedInputException cause = (MismatchedInputException) ex.getCause();
            msg = "无法解析参数 ： " +
                    cause.getPath().stream()
                            .map(JsonMappingException.Reference::getFieldName)
                            .collect(Collectors.joining(StrPool.COMMA + " "));
        }
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.PARAM_READ_EX, msg);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), msg));
        return responseDto;
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseDto<?> handlerConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String msg = FrameworkExEnum.PARAM_VALID_EX.getMsg();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (CollectionUtils.isNotEmpty(violations)) {
            msg = "参数校验失败 ： " +
                    violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(Constants.SEMICOLON + " "));
        }
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.PARAM_VALID_EX, msg);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), msg));
        return responseDto;
    }

    /**
     * 参数字段类型异常
     */
    @ExceptionHandler(BindException.class)
    protected ResponseDto<?> handlerBindException(BindException ex, HttpServletRequest request) {
        String msg = FrameworkExEnum.PARAM_BIND_EX.getMsg();
        BindingResult bindingResult = ex.getBindingResult();
        if (bindingResult.hasErrors()) {
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            if (CollectionUtils.isNotEmpty(fieldErrors)) {
                msg = "参数字段类型不匹配 ： " +
                        fieldErrors.stream()
                                .map(fieldError -> fieldError.getObjectName() + StrPool.DOT + fieldError.getField() + " -> " + fieldError.getRejectedValue())
                                .distinct()
                                .collect(Collectors.joining(Constants.SEMICOLON + " "));
            }
        }
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.PARAM_BIND_EX, msg);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), msg));
        return responseDto;
    }

    /**
     * 方法参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseDto<?> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = FrameworkExEnum.METHOD_ARG_VALID_EX.getMsg();
        BindingResult bindingResult = ex.getBindingResult();
        if (bindingResult.hasErrors()) {
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            if (CollectionUtils.isNotEmpty(fieldErrors)) {
                msg = "方法参数校验失败 ： " +
                        fieldErrors.stream()
                                .map(o -> String.format("arguments [%s], message [%s]", ArrayUtil.join(o.getArguments(), StrUtil.COMMA), o.getDefaultMessage()))
                                .distinct()
                                .collect(Collectors.joining(Constants.SEMICOLON + " "));
            }
        }
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.METHOD_ARG_VALID_EX, msg);

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), msg));
        return responseDto;
    }

    /**
     * 参数字段类型异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseDto<?> handlerMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String msg = String.format("参数字段类型不匹配 ： %s -> %s", ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getName());
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.METHOD_ARG_TYPE_EX, msg);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), msg), ex);
        return responseDto;
    }

    /**
     * 无效状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseDto<?> handlerIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.ILLEGAL_STATE_EX);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), responseDto.getMsg()), ex);
        return responseDto;
    }

    /**
     * 无效参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseDto<?> handlerIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.ILLEGAL_ARGUMENT_EX);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), responseDto.getMsg()), ex);
        return responseDto;
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    protected ResponseDto<?> handlerNullPointerException(NullPointerException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.NULL_POINT_EX);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), responseDto.getMsg()), ex);
        return responseDto;
    }

    @ExceptionHandler(BaseException.class)
    protected ResponseDto<?> handlerBaseException(BaseException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(ex.getCode(), ex.getMessage());

        log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), responseDto.getMsg()));
        return responseDto;
    }

    @ExceptionHandler(Throwable.class)
    protected ResponseDto<?> handlerThrowable(Throwable ex, HttpServletRequest request) {
        // 从堆栈中依次查找 : FeignException -> RemoteCallException -> CommonException -> BaseException
        FeignException feignException = ExUtil.findExceptionFromStack(ex, FeignException.class);
        if (Objects.nonNull(feignException)) {
            ResponseDto<?> responseData = ResponseDtoFactory.error(feignException.getCode(), feignException.getMessage());

            log.warn(String.format("Exception uri [%s] code : %s, message : %s, traceId : %s", request.getRequestURI(), feignException.getCode(), feignException.getMessage(), feignException.getTraceId()));
            return responseData;
        }

        BaseException baseException = ExUtil.findExceptionFromStack(ex, RemoteCallException.class);
        if (baseException == null) {
            baseException = ExUtil.findExceptionFromStack(ex, CommonException.class);
        }
        if (baseException == null) {
            baseException = ExUtil.findExceptionFromStack(ex, BaseException.class);
        }

        if (Objects.nonNull(baseException)) {
            ResponseDto<?> responseDto = ResponseDtoFactory.error(baseException.getCode(), baseException.getMessage());

            log.warn(String.format("Exception uri [%s] %s", request.getRequestURI(), responseDto.getMsg()));
            return responseDto;
        }

        ResponseDto<?> responseDto = ResponseDtoFactory.error(HttpCodeEnum.InternalServerError);

        log.error(String.format("Exception uri [%s] system error", request.getRequestURI()), ex);
        return responseDto;
    }

}
