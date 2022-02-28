package com.seckill.comment;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespBean {

    private long code;
    private String message;
    private Object obj;
    /**
     * 成功返回结果
     */
    public static RespBean success() {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), null);
    }
    /**
     * 成功返回结果
     *
     * @param obj
     */
    public static RespBean success(Object obj) {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), obj);
    }
    /**
     * 失败返回结果
     *
     * @param respBeanEnum
     * @return
     */
    public static RespBean error(RespBeanEnum respBeanEnum) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), null);
    }

    public static RespBean error() {
        return new RespBean(RespBeanEnum.FAILED.getCode(), RespBeanEnum.FAILED.getMessage(), null);
    }

    public static RespBean error(Object data) {
        return new RespBean(RespBeanEnum.FAILED.getCode(), RespBeanEnum.FAILED.getMessage(), data);
    }
}
