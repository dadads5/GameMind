package com.zuel.springtest.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一响应体
 *
 * <pre>
 * {
 *   "success": true,
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... },
 *   "timestamp": 1735689600000
 * }
 * </pre>
 *
 * <p>说明：{@code success} 是由 {@code code} 派生的兼容字段，用于兼容已有前端代码，
 * 待前端全面迁移到 {@code code} 判断后可移除。
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private int code;
    private String message;
    private T data;
    private long timestamp;

    public Result() {
    }

    public Result(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // ---------------- 成功 ----------------

    public static <T> Result<T> success() {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, ResultCode.SUCCESS.getCode(), message, data);
    }

    // ---------------- 失败 ----------------

    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(false, resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(false, resultCode.getCode(), message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(false, code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(false, ResultCode.BAD_REQUEST.getCode(), message, null);
    }
}
