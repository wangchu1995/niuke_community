package com.wangchu.config;

import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //1.授权逻辑
        http.authorizeRequests().antMatchers("/user/setting","/user/upload","/discuss/add","/comment/add/**",
                "/letter/**","/notice/**","like","follow","unfollow").hasAnyAuthority(CommunityConstant.AUTHORITY_USER,
                CommunityConstant.AUTHORITY_ADMIN,CommunityConstant.AUTHORITY_MODERATOR)
                .antMatchers("/discuss/top","/discuss/wonderful","/data/**").hasAnyAuthority(CommunityConstant.AUTHORITY_MODERATOR)
                .antMatchers("/discuss/delete","/actuator/**").hasAnyAuthority(CommunityConstant.AUTHORITY_ADMIN)
                .anyRequest().permitAll();


        //登录未通过or权限不够
        http.exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                String xRequestedWith = request.getHeader("x-requested-with");
                if("XMLHttpRequest".equalsIgnoreCase(xRequestedWith)){
                    //异步请求
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(CommonUtils.getJSONString(403,"用户未登录"));
                }else{
                    response.sendRedirect(request.getContextPath()+"/login");
                }
            }
        }).accessDeniedHandler(new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                String xRequestedWith = request.getHeader("x-requested-with");
                if("XMLHttpRequest".equalsIgnoreCase(xRequestedWith)){
                    //异步请求
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(CommonUtils.getJSONString(403,"权限不够"));
                }else{
                    response.sendRedirect(request.getContextPath()+"/denied");
                }
            }
        });

        http.logout().logoutUrl("/securitylogout");
        http.csrf().disable();

    }
}
