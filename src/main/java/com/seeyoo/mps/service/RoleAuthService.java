package com.seeyoo.mps.service;

import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.bean.MenuBean;
import com.seeyoo.mps.dao.AuthRepository;
import com.seeyoo.mps.dao.RoleAuthRepository;
import com.seeyoo.mps.model.Auth;
import com.seeyoo.mps.model.Role;
import com.seeyoo.mps.model.RoleAuth;
import com.seeyoo.mps.tool.CommonUtil;
import com.seeyoo.mps.tool.TreeUtil;
import com.seeyoo.mps.vo.AuthVo;
import com.seeyoo.mps.vo.router.RouterMeta;
import com.seeyoo.mps.vo.router.VueRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RoleAuthService {
    @Autowired
    private RoleAuthRepository roleAuthRepository;

    @Autowired
    private AuthRepository authRepository;

    public List<HashMap<String, Object>> getRoleAuths(Long roleId) {
        List<HashMap<String, Object>> rolePermissions = new ArrayList<>();
        if (roleId == null)
            return rolePermissions;

        List<RoleAuth> roleAuths = roleAuthRepository.findAllByRoleId(roleId);

        for (RoleAuth roleAuth : roleAuths) {
            Auth auth = roleAuth.getAuth();

            if (auth.getType().intValue() == 1) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("permissionId", auth.getAction());
                map.put("permissionName", auth.getName());

                List<HashMap<String, Object>> actionMap = new ArrayList<>();
                for (RoleAuth roleAction : roleAuths) {
                    Auth action = roleAction.getAuth();
                    if (action.getType().intValue() == 1) {
                        continue;
                    }
                    if (action.getType().intValue() == 2
                            && action.getAuth().getId().intValue() == auth.getId().intValue()) {
                        HashMap<String, Object> a = new HashMap<>();
                        a.put("action", action.getAction());
                        actionMap.add(a);
                    }
                }
                map.put("actionEntitySet", actionMap);
                rolePermissions.add(map);
            }
        }
        return rolePermissions;
    }

    public List<AuthVo> RoleAuthsData(Long userId) {
        List<AuthVo> auths = new ArrayList<>();
        if (userId == null)
            return auths;
        List<Object[]> list = authRepository.getMenuByUserId(userId);
        List<MenuBean> menuBeans = null;
        try {
            menuBeans = CommonUtil.castEntity(list, MenuBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (MenuBean menuBean : menuBeans) {
            AuthVo authVo = new AuthVo();
            authVo.setKey(menuBean.getId() + "");
            authVo.setTitle(menuBean.getName());
            if(StrUtil.isNotEmpty(menuBean.getParentId())){
                authVo.setAuthId(Long.parseLong(menuBean.getParentId() + ""));
            } else {
                authVo.setAuthId(null);
            }
            authVo.setId(Long.parseLong(menuBean.getId() + ""));
            authVo.setAction(menuBean.getAction());
            authVo.setSort(menuBean.getSort());
            authVo.setIcon(menuBean.getIcon());
            authVo.setComponent(menuBean.getComponent());
            auths.add(authVo);
        }
        auths = getAuthTree(auths);
        return auths;
    }

    public List<AuthVo> findAllByRoleId(Long roleId) {
        List<RoleAuth> roleAuths = roleAuthRepository.findAllByRoleId(roleId);

        List<AuthVo> auths = new ArrayList<>();
        for (RoleAuth roleAuth : roleAuths) {
            AuthVo authVo = new AuthVo();
            Auth auth = roleAuth.getAuth();
            authVo.setKey(auth.getId() + "");
            authVo.setTitle(auth.getName());
            authVo.setAuthId(auth.getAuth() == null ? null : auth.getAuth().getId());
            authVo.setId(auth.getId());
            authVo.setAction(auth.getAction());
            authVo.setSort(auth.getSort());
            authVo.setIcon(auth.getIcon());
            authVo.setComponent(auth.getComponent());
            auths.add(authVo);
        }
        return auths;
    }

    public List<AuthVo> tradAuth(List<Auth> list) {
        List<AuthVo> authVos = new ArrayList<>();
        for (Auth auth : list) {
            AuthVo authVo = new AuthVo();
            authVo.setKey(auth.getId() + "");
            authVo.setTitle(auth.getName());
            authVo.setAuthId(auth.getAuth() == null ? null : auth.getAuth().getId());
            authVo.setId(auth.getId());
            authVo.setAction(auth.getAction());
            authVo.setSort(auth.getSort());
            authVo.setIcon(auth.getIcon());
            authVo.setComponent(auth.getComponent());
            authVos.add(authVo);
        }
        return authVos;
    }

    public List<AuthVo> allAuthsData() {
        List<Auth> auths = authRepository.findAllByAuthIsNullOrderBySortAsc();
        List<AuthVo> authVos1 = tradAuth(auths);
        for (AuthVo authVo : authVos1) {
            List<AuthVo> list = tradAuth(authRepository.findAllByAuthIdOrderBySortAsc(authVo.getId()));
            authVo.setChildren(list);
            for (AuthVo authVo1 : list) {
                List<AuthVo> list1 = tradAuth(authRepository.findAllByAuthIdOrderBySortAsc(authVo1.getId()));
                authVo1.setChildren(list1);
            }
        }
        return authVos1;
    }

    private List<AuthVo> getAuthTree(List<AuthVo> list) {
        Map<Long, AuthVo> dtoMap = new HashMap<>();
        for (AuthVo node : list) {
            dtoMap.put(node.getId(), node);
        }
        List<AuthVo> resultList = new ArrayList<>();
        for (Map.Entry<Long, AuthVo> entry : dtoMap.entrySet()) {
            AuthVo node = entry.getValue();
            if (node.getAuthId() == null) {
                // 如果是顶层节点，直接添加到结果集合中
                resultList.add(node);
            } else {
                // 如果不是顶层节点，找其父节点，并且添加到父节点的子节点集合中
                if (dtoMap.get(Long.valueOf(node.getAuthId())) != null) {
                    dtoMap.get(Long.valueOf(node.getAuthId())).getChildren().add(node);
                }
            }
        }
        return resultList;
    }

    public List<RoleAuth> saveRoleAuth(List<RoleAuth> roleAuths) {
        return roleAuthRepository.saveAll(roleAuths);
    }


    public int deleteAllByRole(Role role) {
        return roleAuthRepository.deleteAllByRole(role);
    }

    public ArrayList<VueRouter<Auth>> getUserRouters(Long userId) {
        List<VueRouter<Auth>> routes = new ArrayList<>();
        List<Object[]> list = authRepository.getMenuByUserId(userId);
        List<MenuBean> menuBeans = null;
        try {
            menuBeans = CommonUtil.castEntity(list, MenuBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        menuBeans.forEach(menu -> {
            VueRouter<Auth> route = new VueRouter<>();
            route.setId(menu.getId().toString());
            route.setParentId(menu.getParentId().toString());
//            route.setIcon(menu.getIcon());
            route.setPath("/"+menu.getAction());
            if (menu.getLev() == 1) {
                route.setRedirect(menu.getPath());
            }
            route.setComponent(menu.getComponent());
            route.setName(menu.getAction());
            route.setHidden(menu.getHidden() == 1?true:false);
            route.setMeta(new RouterMeta(true, menu.getName(), new String[]{menu.getAction()}, menu.getIcon(),menu.getPath()));
            routes.add(route);
        });
        return TreeUtil.buildVueRouter(routes);
    }

}
