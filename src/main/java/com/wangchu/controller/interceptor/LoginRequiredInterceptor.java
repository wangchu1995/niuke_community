package com.wangchu.controller.interceptor;

import com.wangchu.annotation.LoginRequired;
import com.wangchu.dal.entity.User;
import com.wangchu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = hostHolder.getUsers();

        if(handler instanceof HandlerMethod){
            //判断为方法,允许转型
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired annotation = method.getAnnotation(LoginRequired.class);
            if(annotation!=null&&user==null){
                //判断是拦截的方法，且没有登录
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }
}
