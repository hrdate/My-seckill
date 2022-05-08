package com.payservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID 手机号码
     */
    private Long id;

    private String nickname;

    /**
     * MD5二次加密
     */
    private String password;

    private String slat;

    /**
     * 头像
     */
    private String head;

    /**
     * 注册时间
     */
    public Date registerDate;

    /**
     * 最后一次登录时间
     */
    public Date lastLoginDate;

    /**
     * 登录次数
     */
    private Integer loginCount;


}
