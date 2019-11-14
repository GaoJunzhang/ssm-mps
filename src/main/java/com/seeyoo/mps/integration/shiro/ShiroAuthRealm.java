package com.seeyoo.mps.integration.shiro;

import com.seeyoo.mps.dao.RoleAuthRepository;
import com.seeyoo.mps.dao.UserRoleRepository;
import com.seeyoo.mps.model.*;
import com.seeyoo.mps.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
public class ShiroAuthRealm extends AuthorizingRealm {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleAuthRepository roleAuthRepository;

    @Override
    public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        hashedCredentialsMatcher.setHashIterations(2);
        super.setCredentialsMatcher(hashedCredentialsMatcher);
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        String account = "" + principals.getPrimaryPrincipal();
        User user = authService.findByAccount(account);
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(user.getId());

        for (UserRole userRole : userRoles) {
            List<RoleAuth> roleAuths = roleAuthRepository.findAllByRoleId(userRole.getRole().getId());
            authorizationInfo.addRole(userRole.getRole().getRole());
            for (RoleAuth roleAuth : roleAuths) {
                authorizationInfo.addStringPermission(roleAuth.getAuth().getAction());
            }
        }
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        String account = (String) token.getPrincipal();
        User user = authService.findByAccount(account);
        if (user == null) {
            throw new AccountException("帐号或密码不正确!");
        }
        if (user.getStatus() != UserStatusEnum.ENABLE) {
            throw new DisabledAccountException("帐号被禁用!");
        }
        return new SimpleAuthenticationInfo(
                user.getAccount(),
                user.getPassword(),
                ByteSource.Util.bytes(user.getSalt()),
                getName());
    }
}
