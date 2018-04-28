package com.ciicsh.gto.afsupportcenter.housefund.messageservice.host.configuration;

import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.utils.LogApiUtil;
import com.ciicsh.gto.afsupportcenter.housefund.fundservice.business.utils.LogMessage;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class LoggerAspect {

    @Autowired
    private LogApiUtil logApi;

    @Pointcut("execution (* com.ciicsh.gto.afsupportcenter.housefund.messageservice.host.message..*.*(..))")
    public void messagePointcut() {
    }

    @AfterThrowing(value = "messagePointcut()", throwing = "e")
    public void doAfterThrow(JoinPoint joinPoint, Throwable e) {
        //获取类名
        String className = joinPoint.getTarget().getClass().getSimpleName();
        //获取方法名
        String methodName = joinPoint.getSignature().getName();
        String msg = String.format("%1$s.%2$s:%3$s",className ,methodName , e.toString());
        logApi.error(LogMessage.create().setTitle(className + "." + methodName).setContent(msg));
    }
}