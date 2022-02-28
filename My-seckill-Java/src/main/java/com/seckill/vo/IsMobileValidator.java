package com.seckill.vo;

import com.seckill.utils.ValidatorUtil;
import com.seckill.validator.IsMobile;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 手机号码校验规则
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

	private boolean required = false;  //自定义属性，判断是否强制要求有判断

	@Override
	public void initialize(IsMobile constraintAnnotation) {
		//获取注解required属性
		required = constraintAnnotation.required();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (required){
			return ValidatorUtil.isMobile(value);
		}else {
			if (StringUtils.isEmpty(value)){
				return true;
			}else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}
}