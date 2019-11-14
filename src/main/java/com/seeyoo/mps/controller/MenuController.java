package com.seeyoo.mps.controller;

import com.seeyoo.mps.bean.AuthBean;
import com.seeyoo.mps.model.Auth;
import com.seeyoo.mps.service.MenuService;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by user on 2019/9/6.
 */
@Slf4j
@RestController
@Api(tags = "菜单管理")
@RequestMapping("/menu")
@Transactional
public class MenuController {
    @Autowired
    private MenuService menuService;

    @RequiresPermissions("authList")
    @RequestMapping(value = "/getById/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "id获取菜单")
    public Result<AuthBean> getById(@PathVariable Long id) {
        Auth auth = menuService.findById(id);
        AuthBean authBean = new AuthBean();
        authBean.setAction(auth.getAction());
        authBean.setId(auth.getId());
        authBean.setCode(auth.getCode());
        authBean.setHidden(auth.getHidden());
        authBean.setPath(auth.getPath());
        if (auth.getAuth() != null) {
            authBean.setParentId(auth.getAuth().getId());
            authBean.setParentName(auth.getAuth().getName());
            authBean.setParentCode(auth.getAuth().getCode());
        }
        authBean.setComponent(auth.getComponent());
        authBean.setIcon(auth.getIcon());
        authBean.setLev(auth.getLev());
        authBean.setSort(auth.getSort());
        authBean.setType(auth.getType());
        authBean.setName(auth.getName());
        return new ResultUtil<AuthBean>().setData(authBean);
    }

    @RequiresPermissions("authList")
    @RequestMapping(value = "/delAuth/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除菜单")
    public Result<String> delRole(@PathVariable String[] ids) {
        for (String id : ids) {
            Auth auth = new Auth();
            auth.setId(Long.parseLong(id));
            if (menuService.findByAuth(auth).size() > 0) {
                return new ResultUtil<String>().setErrorMsg("无法删除有子节点的菜单,请按照顺序，先选择子节点，后选择父节点进行删除");
            }
            menuService.delete(Long.parseLong(id));
        }
        return new ResultUtil<String>().setSuccessMsg("删除成功");
    }

    @RequiresPermissions("authList")
    @RequestMapping(value = "/saveAuth", method = RequestMethod.POST)
    @ApiOperation(value = "保存菜单")
    public Object saveAuth(Long id,
                                 @RequestParam(name = "name") String name,
                                 @RequestParam(name = "action") String action,
                                 @RequestParam(name = "type") Short type,
                                 @RequestParam(name = "lev") Integer lev,
                                 @RequestParam(name = "sort", required = true, defaultValue = "0") Integer sort,
                                 @RequestParam(name = "component") String component,
                                 @RequestParam(name = "icon") String icon,
                                 @RequestParam(name = "path") String path,
                                 Long parentId, Short hidden) {
        Auth auth = new Auth();
        if (id == null) {
            if (menuService.findByAction(action).size() > 0) {
                return new ResultUtil<Auth>().setErrorMsg("菜单action已经存在");
            }
            if (parentId != null) {
                Auth parentAuth = menuService.findById(parentId);
                if (parentAuth == null) {
                    return new ResultUtil<Auth>().setErrorMsg("父节点异常");
                }
                auth.setCode(getAuthCode(parentId, parentAuth.getCode()));
                auth.setAuth(parentAuth);
            } else {

                List<Auth> auths = menuService.findByAuthIsNull();
                auth.setCode(getNextCode(auths));
            }
        } else {
            auth = menuService.findById(id);
        }
        auth.setId(id);
        auth.setName(name);
        auth.setAction(action);
        auth.setType(type);
        auth.setLev(lev);
        auth.setSort(sort);
        auth.setIcon(icon);
        auth.setHidden(hidden);
        auth.setPath(path);
        auth.setComponent(component);
        menuService.save(auth);
        return new ResultUtil<>().setSuccessMsg("保存成功");
    }

    private String getAuthCode(Long parentId, String parentCode) {
        Auth auth = new Auth();
        auth.setId(parentId);
        List<Auth> list = menuService.findByAuth(auth);

        if (list.size() > 0) {
            return getNextCode(list);

        } else {
            return parentCode + "001";
        }
    }

    private String getNextCode(List<Auth> list) {
        int maxCode = 0;
        int tmpCode = 0;
        for (Auth auth : list) {
            tmpCode = Integer.parseInt(auth.getCode().substring(2, auth.getCode().length()));
            if (tmpCode > maxCode) {
                maxCode = tmpCode;
            }
        }
        maxCode++;
        return "00" + maxCode;
    }
}
