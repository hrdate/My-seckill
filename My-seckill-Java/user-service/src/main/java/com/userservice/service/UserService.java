package com.userservice.service;

import com.userservice.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.userservice.vo.LoginVo;
import comment.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-05-04
 */
public interface UserService extends IService<User> {

    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

}
