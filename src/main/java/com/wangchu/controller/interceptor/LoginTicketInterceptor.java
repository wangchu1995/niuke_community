package com.wangchu.controller.interceptor;

import com.wangchu.dal.entity.LoginTicket;
import com.wangchu.dal.entity.User;
import com.wangchu.service.UserService;
import com.wangchu.util.CookieUtil;
import com.wangchu.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;
    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        if (!StringUtils.isBlank(ticket)){
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //凭证有效性和过期
            if(loginTicket!=null&&loginTicket.getStatus()==0&&loginTicket.getExpired().after(new Date())){
                User user = userService.selectUserById(loginTicket.getUserId());
                hostHolder.setUsers(user);

                //4.登录成功，将凭证数据给Security一份
                Authentication authentication = new UsernamePasswordAuthenticationToken(user,user.getPassword(),userService.getAuthorites(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }




        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUsers();
        if(user!=null&&modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
