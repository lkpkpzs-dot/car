package org.lkp.car.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.warn("业务处理失败: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据库唯一索引冲突: {}", e.getMessage());
        String message = e.getMessage();
        if (message != null && message.contains("uk_credit_code")) {
            return Result.error(400, "该统一社会信用代码已提交过申请，请联系管理员或使用其他代码");
        }
        return Result.error(400, "数据重复，请检查您的输入");
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Void> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.warn("数据库完整性约束冲突: {}", e.getMessage());
        String message = e.getMessage();
        if (message != null && message.contains("uk_credit_code")) {
            return Result.error(400, "该统一社会信用代码已提交过申请，请联系管理员或使用其他代码");
        }
        return Result.error(400, "数据重复，请检查您的输入");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.error(400, "缺少必要参数：" + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(400, "参数格式不正确：" + e.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotRead(HttpMessageNotReadableException e) {
        return Result.error(400, "请求参数格式不正确，请检查JSON内容");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
