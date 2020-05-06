package com.wangchu.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.wangchu.service.*.*(..))")
    public void pointcut(){
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        // 用户[1.2.3.4]，在[xxx]时间，访问了[com.nowcoder.community.service.xx()].
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String ip = request.getRemoteHost();
        String time = new SimpleDateFormat("YY-MM-dd HH:mm:ss").format(new Date());
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,time,className+methodName));
    }
}
