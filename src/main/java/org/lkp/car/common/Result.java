package org.lkp.car.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一接口返回结果封装类
 * @param <T> 数据类型
 */
@Data
@ApiModel(description = "统一响应结果封装")
public class Result<T> implements Serializable {

    /**
     * 响应码，200为成功
     */
    @ApiModelProperty(value = "状态码", example = "200")
    private Integer code;
    
    /**
     * 响应消息
     */
    @ApiModelProperty(value = "响应消息", example = "成功")
    private String msg;
    
    /**
     * 响应数据
     */
    @ApiModelProperty(value = "响应数据内容")
    private T data;

    /**
     * 快捷返回成功结果（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 快捷返回成功结果（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(data);
        return result;
    }

    /**
     * 快捷返回错误结果（仅消息）
     */
    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    /**
     * 快捷返回错误结果（带响应码和消息）
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
