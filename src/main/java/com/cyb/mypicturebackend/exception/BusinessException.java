package com.cyb.mypicturebackend.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 *
 * @author cyb
 * @date 2025/01/07 14:26
 **/
@Getter
public class BusinessException extends RuntimeException {

    private int code;

    public BusinessException(int code) {
        this.code = code;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), message);
    }

}

