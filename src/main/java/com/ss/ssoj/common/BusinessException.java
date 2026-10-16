package com.ss.ssoj.common;

/**
 * 业务异常：由全局异常处理器统一转换为响应体。
 * code 取 400 参数错误、401 未登录、403 无权限、404 数据不存在，其余业务错误使用 1000 以上。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(1000, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
