package com.seeyoo.mps.controller;

import com.seeyoo.mps.controller.request.LoginRequest;
import com.seeyoo.mps.model.Auth;
import com.seeyoo.mps.service.AuthService;
import com.seeyoo.mps.service.RoleAuthService;
import com.seeyoo.mps.tool.CommonUtil;
import com.seeyoo.mps.vo.router.VueRouter;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

@RestController
@Slf4j
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private RoleAuthService roleAuthService;

    @PostMapping(path = "/auth/login")
    public Map<String, Object> login(HttpServletRequest request, @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest.getAccount(), loginRequest.getPassword(), loginRequest.getCode(), CommonUtil.getClientIP(request));
    }

    @PostMapping(path = "/auth/logout")
    public Map<String, Object> login() {
        return authService.logout();
    }

    @GetMapping(path = "/noAuth")
    public Map<String, Object> noAuth() {
        return CommonUtil.defaultResponse(9999, "Login timeout");
    }

    @GetMapping(value = "/user/info")
    public Map<String, Object> getUserInfo() {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        return authService.getUserInfo((long) session.getAttribute("userId"));
    }

    @RequestMapping(value = "/user/getUserRouters", method = RequestMethod.GET)
    @ApiOperation(value = "获取可分配权限")
    public ArrayList<VueRouter<Auth>> getUserRouters() {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        return roleAuthService.getUserRouters((long) session.getAttribute("userId"));
    }
}
