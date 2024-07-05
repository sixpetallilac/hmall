package com.hmall.common.interceptor;


import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1获取登录用户信息
        String userinfo = request.getHeader("user-info");
        //2判断是否获取了用户，有则存入threadlocal
        if (StrUtil.isNotBlank(userinfo)){
            UserContext.setUser(Long.valueOf(userinfo));
        }
        //3放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
