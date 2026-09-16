package com.zuel.springtest.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>统一把异常转换为 {@link Result}，Controller / Service 中不再写 try-catch 拼装响应。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：可预期的失败，按业务码返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常 [{}]：{}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @Valid 校验请求体失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + "：" + error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return Result.fail(ResultCode.BAD_REQUEST, message.isEmpty() ? "参数校验失败" : message);
    }

    /** @Validated 校验方法参数失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    /** 请求体缺失或格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "请求体格式错误或缺失");
    }

    /** 缺少必填的查询参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数：{}", e.getParameterName());
        return Result.fail(ResultCode.BAD_REQUEST, "缺少请求参数：" + e.getParameterName());
    }

    /** 参数类型转换失败，例如 /posts/abc 无法转成 Long */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误：{} = {}", e.getName(), e.getValue());
        return Result.fail(ResultCode.BAD_REQUEST, "参数格式错误：" + e.getName());
    }

    /** 上传文件超出限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("上传文件超出限制：{}", e.getMessage());
        return Result.fail(ResultCode.PAYLOAD_TOO_LARGE);
    }

    /** 兜底：未预期的异常，不向客户端暴露内部细节 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
