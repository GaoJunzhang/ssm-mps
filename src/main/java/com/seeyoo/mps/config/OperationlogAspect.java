package com.seeyoo.mps.config;

import cn.hutool.json.JSONUtil;
import com.seeyoo.mps.model.OperationLog;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.sql.Timestamp;

/**
 * Created by user on 2019/11/7.
 */
@Aspect
@Component
@Slf4j
public class OperationlogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Pointcut("execution(public * com.seeyoo.mps.controller..*.*(..))")
    public void Pointcut() {
    }
    @Around("Pointcut()")
    public Object Around(ProceedingJoinPoint pjp) throws Throwable {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Object object = pjp.proceed();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RequiresPermissions rp = method.getAnnotation(RequiresPermissions.class);
        if ((request.getMethod().equals("POST") || request.getMethod().equals("DELETE")) && rp != null){
            HttpSession session = request.getSession();
            long userId = (long) session.getAttribute("userId");
            OperationLog operationLog = new OperationLog();
            if (rp.value() != null && rp.value().length>0){
                operationLog.setAction(rp.value()[0]);
            } else {
                operationLog.setAction(pjp.getTarget().getClass().getName() + "." + signature.getName());
            }
            operationLog.setRequest(JSONUtil.toJsonStr(pjp.getArgs()));
            operationLog.setResponse(JSONUtil.toJsonStr(object));
            operationLog.setCreateTime(start);
            operationLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            operationLog.setStatus(response.getStatus());
            User user = new User();
            user.setId(userId);
            operationLog.setUser(user);
            operationLogService.save(operationLog);

        }
        return object;
    }
}
