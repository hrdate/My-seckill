package com.userservice.vo;

import com.userservice.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 登录参数
 */
@Data
public class LoginVo {
	@NotNull(message = "手机号码不能为空")
	@IsMobile
	private String mobile;

	@NotNull(message = "密码不能为空")
	@Length(min = 32)
	private String password;

}