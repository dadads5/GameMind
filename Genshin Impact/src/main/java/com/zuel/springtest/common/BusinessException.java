package com.zuel.springtest.common;

import lombok.Getter;

/**
 * 业务异常
 *
 * <p>业务层中一切可预期的失败都应抛出此异常，由 {@link GlobalExceptionHandler} 统一转换为
 * {@link Result}，Controller 中不再出现 try-catch。
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
