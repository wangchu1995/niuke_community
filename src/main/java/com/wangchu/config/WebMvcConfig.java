package com.wangchu.config;

import com.wangchu.controller.interceptor.AlphaInterceptor;
import com.wangchu.controller.interceptor.LoginRequiredInterceptor;
import com.wangchu.controller.interceptor.LoginTicketInterceptor;
import com.wangchu.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    AlphaInterceptor alphaInterceptor;
    @Autowired
    LoginTicketInterceptor loginTicketInterceptor;
    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;
    @Autowired
    MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor).excludePathPatterns("/**/*.html","/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg").addPathPatterns("/register", "/login");
        registry.addInterceptor(loginTicketInterceptor).excludePathPatterns("/**/*.html","/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(loginRequiredInterceptor).excludePathPatterns("/**/*.html","/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(messageInterceptor).excludePathPatterns("/**/*.html","/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
