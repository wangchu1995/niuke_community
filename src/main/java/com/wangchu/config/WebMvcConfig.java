package com.wangchu.config;

import com.wangchu.controller.interceptor.AlphaInterceptor;
import com.wangchu.controller.interceptor.LoginRequiredInterceptor;
import com.wangchu.controller.interceptor.LoginTicketInterceptor;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg").addPathPatterns("/register","/login");
        registry.addInterceptor(loginTicketInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(loginRequiredInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
