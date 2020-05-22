package com.wangchu.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

//    @Pointcut("execution(* com.wangchu.service.*.*(..))")
//    public void pointcut(){
//    }
//
//    @Before("pointcut()")
//    public void before(JoinPoint joinPoint){
//        // 用户[1.2.3.4]，在[xxx]时间，访问了[com.nowcoder.community.service.xx()].
//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        //当不是通过controller调用service，而是消息队列调用service时，是没有请求数据的
//        if(requestAttributes==null) return;
//        HttpServletRequest request = requestAttributes.getRequest();
//        String ip = request.getRemoteHost();
//        String time = new SimpleDateFormat("YY-MM-dd HH:mm:ss").format(new Date());
//        String className = joinPoint.getSignature().getDeclaringType().getName();
//        String methodName = joinPoint.getSignature().getName();
//        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,time,className+methodName));
//    }
}
