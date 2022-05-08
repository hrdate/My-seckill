package com.userservice.controller;

import com.userservice.interceptor.AccessLimit;
import comment.RespBean;
import com.userservice.service.UserService;
import com.userservice.vo.LoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
@Controller
@Slf4j
@RequestMapping("/login")
public class LoginController {

    @Autowired
    UserService userService;

    @GetMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    @AccessLimit(second = 1, maxCount = 10,timeunit = TimeUnit.SECONDS,needLogin = false)
    @ResponseBody
    @RequestMapping("/doLogin")
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response){
        log.info("{}",loginVo);
        return userService.doLogin(loginVo,request,response);
    }
}
