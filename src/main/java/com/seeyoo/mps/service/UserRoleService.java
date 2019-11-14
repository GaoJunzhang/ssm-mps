package com.seeyoo.mps.service;

import com.seeyoo.mps.dao.UserRoleRepository;
import com.seeyoo.mps.model.Role;
import com.seeyoo.mps.model.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class UserRoleService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired RoleAuthService roleAuthService;

    public Object getUserRoleAuths(long userId){
        HashMap<String, Object> roleAuth = new HashMap<>();
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        if(userRoles!=null&&userRoles.size()>0){
            UserRole userRole = userRoles.get(0);
            roleAuth.put("name", userRole.getRole().getName());
            roleAuth.put("permissions", roleAuthService.getRoleAuths(userRole.getRole().getId()));
        }

        return roleAuth;
    }

    public Role userRole(long userId){
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        if(userRoles!=null&&userRoles.size()>0){
            return userRoles.get(0).getRole();
        }
        return null;
    }
}
