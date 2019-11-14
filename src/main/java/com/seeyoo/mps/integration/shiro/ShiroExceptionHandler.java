package com.seeyoo.mps.integration.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ShiroExceptionHandler implements HandlerExceptionResolver {

    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception ex) {
        ModelAndView mv = new ModelAndView();
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        log.info("{}:{}", httpServletRequest.getRequestURI(), ex.getMessage());
        if (ex instanceof UnauthenticatedException) {
            mv.addObject("code", 9997);
            mv.addObject("message", "Token error");
        } else if (ex instanceof UnauthorizedException) {
            mv.addObject("code", 9998);
            mv.addObject("message", "Permission denied");
        } else {
            mv.addObject("code", 9996);
            mv.addObject("message", ex.getMessage());
        }
        ex.printStackTrace();
        mv.setView(view);
        return mv;
    }
}
