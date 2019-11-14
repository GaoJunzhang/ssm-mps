package com.seeyoo.mps.service;

import cn.hutool.core.util.StrUtil;
import com.google.code.kaptcha.Constants;
import com.seeyoo.mps.dao.UserRepository;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.tool.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleAuthService roleAuthService;

    public User findByAccount(String account) {
        if (StrUtil.isEmpty(account))
            return null;

        List<User> userList = userRepository.findAllByAccountAndIsDelete(account, (short) 0);
        if (userList != null && userList.size() > 0) {
            return userList.get(0);
        }
        return null;
    }

    public Map<String, Object> login(String account, String password, String code, String ip) {
        if (StrUtil.isEmpty(account) || StrUtil.isEmpty(password)) {
            return CommonUtil.defaultResponse(1, "用户名为空");
        }
        Subject currentUser = SecurityUtils.getSubject();
        String errMsg = "unknown";

        UsernamePasswordToken token = new UsernamePasswordToken(account, password);
        try {
            Session session = currentUser.getSession();
            String kaptcha = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
            session.removeAttribute(Constants.KAPTCHA_SESSION_KEY);
            if (!StrUtil.isEmpty(kaptcha) && kaptcha.equals(code) && !StrUtil.isEmpty(code)) {
                currentUser.login(token);
                User user = findByAccount(account);
                session.setAttribute("account", user.getAccount());
                session.setAttribute("name", user.getName());
                session.setAttribute("userId", user.getId());
                session.setAttribute("userCode", user.getCode());
                Map<String, Object> res = CommonUtil.defaultResponse(0, "");
                res.put("token", currentUser.getSession().getId());

                userRepository.updateUserLogin(user.getId(), ip);
                return res;
            }
            errMsg = "验证码错误";
        } catch (UnknownAccountException e) {
            errMsg = "账号不存在";
        } catch (IncorrectCredentialsException e) {
            errMsg = "密码不正确";
        } catch (AuthenticationException e) {
            errMsg = "用户验证失败";
        }

        return CommonUtil.defaultResponse(1, errMsg);
    }

    public Map<String, Object> logout() {
        SecurityUtils.getSubject().logout();
        return CommonUtil.defaultResponse(0, "");
    }

    @Transactional
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return CommonUtil.defaultResponse(0, "无效的用户");
        }

        Map<String, Object> map = CommonUtil.defaultResponse(0, "");
        map.put("name", user.getName());
        map.put("avatar", user.getAvatar());
        try {
            map.put("role", userRoleService.getUserRoleAuths(userId));
            map.put("menus",roleAuthService.getUserRouters(userId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("getUserInfo:{}",map);
        return map;
    }
}
