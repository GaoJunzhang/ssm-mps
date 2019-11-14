package com.seeyoo.mps.controller;

import com.seeyoo.mps.controller.request.ChangePasswordRequest;
import com.seeyoo.mps.controller.request.IdRequest;
import com.seeyoo.mps.controller.request.SaveUserRequest;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.RoleService;
import com.seeyoo.mps.service.UserService;
import com.seeyoo.mps.tool.CommonUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user/*")
public class UserController extends BaseController<User, Long> {
    @Autowired
    private UserService userService;

    @Override
    public UserService getService() {
        return userService;
    }

    @RequiresPermissions("userList")
    @GetMapping(path = "/userListData")
    public Result<Map<String, Object>> userList(HttpSession session, String name, String account, Short status,
                                                @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(defaultValue = "descend") String sortOrder, @RequestParam(defaultValue = "createTime") String sortField) {
        long userId = (long) session.getAttribute("userId");
        String userCode = (String) session.getAttribute("userCode");
        return new ResultUtil().setData(userService.userList(userId, userCode, name, account, status, pageNo, pageSize, sortOrder, sortField));
    }

    @RequiresPermissions("user:add")
    @RequestMapping(value = "/saveUser", method = RequestMethod.POST)
    public Object saveUser(HttpSession session, @RequestBody SaveUserRequest saveUserRequest) {
        long userId = (long) session.getAttribute("userId");
        return userService.saveUser(userId, saveUserRequest);
    }

    @RequiresPermissions("user:resetPassword")
    @GetMapping(path = "/resetPassword")
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ApiOperation(value = "重置用户密码")
    public Object resetPassword(@RequestBody IdRequest ids) {
        log.info("{}", ids);
        return userService.resetPassword(ids.getIds());
    }

    @RequiresPermissions("user:del")
    @RequestMapping(value = "/delUser/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除用户")
    public Object delUser(@PathVariable Long[] ids) {
        return userService.delUser(ids);
    }

    @RequiresPermissions("user:edit")
    @RequestMapping(value = "/userDetail/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "获取用户信息")
    public Object user(HttpSession session, @PathVariable Long id) {
        long userId = (long) session.getAttribute("userId");
        return userService.user(userId, id);
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    @ApiOperation(value = "获取用户信息")
    public Object changePassword(HttpSession session, @RequestBody ChangePasswordRequest request) {
        long userId = (long) session.getAttribute("userId");
        return userService.changePassword(userId, request);
    }
}
