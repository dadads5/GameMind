package com.zuel.springtest.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一业务状态码
 *
 * <p>HTTP 状态码与业务码保持一致的数值，便于前端直接判断。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    PAYLOAD_TOO_LARGE(413, "上传文件过大"),
    TOO_MANY_REQUESTS(429, "操作过于频繁，请稍后再试"),

    INTERNAL_ERROR(500, "服务器内部错误"),
    AI_SERVICE_ERROR(502, "AI 服务调用失败"),
    ;

    private final int code;
    private final String message;
}
