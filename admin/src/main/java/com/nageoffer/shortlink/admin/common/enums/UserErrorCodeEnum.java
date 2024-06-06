package com.nageoffer.shortlink.admin.common.enums;

import com.nageoffer.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {

    USER_NULL("B000200", "用户记录不存在"),

    USER_NAME_EXIST("B000201", "用户名已存在"),

    USER_EXIST("B000202", "用户已存在"),

    USER_SAVE_ERROR("B000203", "用户保存失败");



    /**
     * 用户不存在
     */
//    USER_NOT_EXIST("10001", "用户不存在"),
//    /**
//     * 用户已存在
//     */
//    USER_ALREADY_EXIST(10002, "用户已存在"),
//    /**
//     * 用户名或密码错误
//     */
//    USER_NAME_OR_PASSWORD_ERROR(10003, "用户名或密码错误"),

    private final String code;
    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
