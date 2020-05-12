package com.wangchu.controller.interceptor;

import com.wangchu.dal.entity.User;
import com.wangchu.service.MessageService;
import com.wangchu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;
    @Autowired
    MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //控制器之后，模板之前
        User user = hostHolder.getUsers();
        if(user!=null&&modelAndView!=null){
            modelAndView.addObject("totalUnread",messageService.findTopicUnreadNum(user.getId(),null)+messageService.countUnread(user.getId(),null));
        }
    }
}
