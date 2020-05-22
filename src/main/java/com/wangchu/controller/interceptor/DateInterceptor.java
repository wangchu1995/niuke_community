package com.wangchu.controller.interceptor;

import com.wangchu.dal.entity.User;
import com.wangchu.service.DataService;
import com.wangchu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DateInterceptor implements HandlerInterceptor {
    @Autowired
    DataService dataService;
    @Autowired
    HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = hostHolder.getUsers();
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);
        if(user!=null){
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
