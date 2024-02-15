package com.minister.component.mybatis.exception;

import com.minister.component.utils.enums.FrameworkExEnum;
import com.minister.framework.api.entity.ResponseDto;
import com.minister.framework.api.utils.ResponseDtoFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

/**
 * MyBatisPlus全局异常处理
 *
 * @author QIUCHANGQING620
 * @date 2020-03-07 17:57
 */
@RestControllerAdvice
@Slf4j
public class GlobalMyBatisPlusExceptionHandler {

    @ExceptionHandler(SQLException.class)
    public ResponseDto<?> handlerSQLException(SQLException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.SQL_EX);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), FrameworkExEnum.SQL_EX.getMsg()), ex);
        return responseDto;
    }

    @ExceptionHandler(PersistenceException.class)
    protected ResponseDto<?> handlerPersistenceException(PersistenceException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.SQL_EX);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), FrameworkExEnum.SQL_EX.getMsg()), ex);
        return responseDto;
    }

    @ExceptionHandler(MyBatisSystemException.class)
    protected ResponseDto<?> handlerMyBatisSystemException(MyBatisSystemException ex, HttpServletRequest request) {
        if (ex.getCause() instanceof PersistenceException) {
            return this.handlerPersistenceException((PersistenceException) ex.getCause(), request);
        }
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.SQL_EX);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), FrameworkExEnum.SQL_EX.getMsg()), ex);
        return responseDto;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseDto<?> handlerDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        ResponseDto<?> responseDto = ResponseDtoFactory.error(FrameworkExEnum.SQL_EX);

        log.error(String.format("Exception uri [%s] %s", request.getRequestURI(), FrameworkExEnum.SQL_EX.getMsg()), ex);
        return responseDto;
    }

}
