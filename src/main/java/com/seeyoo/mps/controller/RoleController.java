package com.seeyoo.mps.controller;

import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.model.Auth;
import com.seeyoo.mps.model.Role;
import com.seeyoo.mps.model.RoleAuth;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.RoleAuthService;
import com.seeyoo.mps.service.RoleService;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.AuthVo;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.SearchVo;
import com.seeyoo.mps.vo.router.VueRouter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(tags = "角色管理接口")
@RequestMapping("/role")
@Transactional
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleAuthService roleAuthService;

    @RequiresPermissions("roleList")
    @RequestMapping(value = "/getRoleData", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object getByCondition(@ModelAttribute Role role,
                                                      @ModelAttribute SearchVo searchVo,
                                                      @ModelAttribute PageVo pageVo) {
        if (StrUtil.isEmpty(pageVo.getSortField())) {
            pageVo.setSortField("createTime");
            pageVo.setSortOrder("desc");
        }
        return new ResultUtil<>().setData(roleService.roleData(role, searchVo, PageUtil.initPage(pageVo)));
    }

    @RequiresPermissions("role:add")
    @RequestMapping(value = "/saveRole", method = RequestMethod.POST)
    @ApiOperation(value = "保存角色")
    public Result<String> saveRole(HttpSession session, Long id, String role, String name, String description, Short isDelete, String[] authIds) {
        List<Role> roleList = roleService.findAllByRole(role);
        if (roleList.size() > 0 && !id.equals(roleList.get(0).getId())) {
            return new ResultUtil<String>().setErrorMsg("唯一识别码已被使用");
        }
        long userId = (long) session.getAttribute("userId");
        User user = new User();
        user.setId(userId);
        Role role1 = new Role();
        if (id != null) {
            role1.setId(id);
            role1.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            role1.setIsDelete(isDelete);
        } else {
            role1.setIsDelete((short) 0);
            role1.setCreateTime(new Timestamp((System.currentTimeMillis())));
        }
        role1.setRole(role);
        role1.setName(name);
        role1.setDescription(description);
        role1.setUser(user);
        Role role2 = roleService.save(role1);
        roleAuthService.deleteAllByRole(role2);//删除所有权限
        //添加新权限
        if (authIds.length > 0) {
            List<RoleAuth> roleAuths = new ArrayList<>(authIds.length);
            for (String authId : authIds) {
                RoleAuth roleAuth = new RoleAuth();
                Auth auth = new Auth();
                auth.setId(Long.parseLong(authId));
                roleAuth.setAuth(auth);
                roleAuth.setRole(role2);
                roleAuth.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                roleAuth.setCreateTime(new Timestamp(System.currentTimeMillis()));
                roleAuths.add(roleAuth);
            }
            roleAuthService.saveRoleAuth(roleAuths);
        }
        return new ResultUtil<String>().setSuccessMsg("保存成功");
    }

    @RequiresPermissions("role:del")
    @RequestMapping(value = "/delRole/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除角色")
    public Result<String> delRole(@PathVariable String[] ids) {
        for (String id : ids) {
            roleService.updateIsDelete(Long.parseLong(id));
        }
        return new ResultUtil<String>().setSuccessMsg("删除成功");
    }

    @RequestMapping(value = "/getRoleAuth", method = RequestMethod.GET)
    @ApiOperation(value = "获取角色权限")
    public Map<String, Object> getRoleAuth(@RequestParam(name = "roleId", required = true) Long roleId) {
        List<AuthVo> list = roleAuthService.findAllByRoleId(roleId);
        Map<String, Object> map = new HashMap<>();
        map.put("rolePermission", list);
        return map;
    }

    @RequestMapping(value = "/getPermission", method = RequestMethod.GET)
    @ApiOperation(value = "获取可分配权限")
    public Map<String, Object> getPermission(HttpSession session) {
        long userId = (long) session.getAttribute("userId");
        String userCode = (String) session.getAttribute("userCode");
        List<AuthVo> myAuth = new ArrayList<>();
        if (userCode.equals("0000")) {

            myAuth = roleAuthService.allAuthsData();
        } else {
            myAuth = roleAuthService.RoleAuthsData(userId);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("permission", myAuth);
        return map;
    }

    @RequestMapping(value = "/getUserRouters", method = RequestMethod.GET)
    @ApiOperation(value = "获取可分配权限")
    public ArrayList<VueRouter<Auth>> getUserRouters(HttpSession session) {
        long userId = (long) session.getAttribute("userId");
        return roleAuthService.getUserRouters(userId);
    }

    @RequestMapping(value = "/allRoles", method = RequestMethod.GET)
    @ApiOperation(value = "获取全部角色")
    public Object allRoles() {
        List<Map<String, Object>> allRoles = new ArrayList<>();
        List<Role> roleList = roleService.findAllByIsDelete((short) 0);
        for (Role role : roleList){
            Map<String,Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("name", role.getName());
            map.put("role", role.getRole());
            allRoles.add(map);
        }
        return allRoles;
    }
}
