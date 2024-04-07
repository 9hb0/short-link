package com.nageoffer.shortlink.admin.common.convention.result;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Result<T> implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 正确返回码
     */
    public static final String SUCCESS_CODE = "0";
    // 结果码
    private String code;
    // 结果信息
    private String message;
    // 结果数据
    private T data;
    //请求ID
    private String requestId;

    /**
     *
     */
    public boolean isSuccess() {
        return SUCCESS_CODE
                .equals(this.code);
    }

//    public Result(String code, String message, T data)
//    {
//        this.code = code;
//        this.message = message;
//        this.data = data;
//    }
}
