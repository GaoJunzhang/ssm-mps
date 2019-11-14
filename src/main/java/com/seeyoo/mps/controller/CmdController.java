package com.seeyoo.mps.controller;

import com.seeyoo.mps.controller.request.AuditTaskRequest;
import com.seeyoo.mps.controller.request.CmdRequest;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.model.Task;
import com.seeyoo.mps.service.CmdService;
import com.seeyoo.mps.service.TaskService;
import com.seeyoo.mps.tool.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author Wangj
 */
@Slf4j
@RestController
@Api(description = "任务管理接口")
@RequestMapping("/cmd")
public class CmdController {

    @Autowired
    private CmdService cmdService;

    @RequestMapping(value = "/cmdStatus", method = RequestMethod.GET)
    @ApiOperation(value = "获取命令发送状态")
    public Object taskList(String opNo) {
        return cmdService.cmdStatus(opNo);
    }

    @RequiresPermissions("terminal:reboot")
    @RequestMapping(value = "/reboot", method = RequestMethod.POST)
    @ApiOperation(value = "终端重启")
    public Object reboot(HttpSession session, @RequestBody CmdRequest cmdRequest) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.reboot(userId, cmdRequest);
    }

    @RequiresPermissions("terminal:checkTime")
    @RequestMapping(value = "/setTime", method = RequestMethod.POST)
    @ApiOperation(value = "终端校时")
    public Object setTime(HttpSession session, @RequestBody CmdRequest cmdRequest) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.setTime(userId, cmdRequest);
    }

    @RequiresPermissions("terminal:checkTime")
    @RequestMapping(value = "/getTime", method = RequestMethod.GET)
    @ApiOperation(value = "获取终端时间")
    public Object getTime(HttpSession session, Long terminalId) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.getTime(userId, terminalId);
    }

    @RequiresPermissions("terminal:onOff")
    @RequestMapping(value = "/setOnOff", method = RequestMethod.POST)
    @ApiOperation(value = "设置开关机")
    public Object setOnOff(HttpSession session, @RequestBody CmdRequest cmdRequest) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.setOnOff(userId, cmdRequest);
    }

    @RequiresPermissions("terminal:onOff")
    @RequestMapping(value = "/getOnOff", method = RequestMethod.GET)
    @ApiOperation(value = "获取开关机")
    public Object getOnOff(HttpSession session, Long terminalId) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.getOnOff(userId, terminalId);
    }

    @RequiresPermissions("terminal:server")
    @RequestMapping(value = "/setServer", method = RequestMethod.POST)
    @ApiOperation(value = "设置服务器")
    public Object setServer(HttpSession session, @RequestBody CmdRequest cmdRequest) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.setServer(userId, cmdRequest);
    }

    @RequiresPermissions("terminal:server")
    @RequestMapping(value = "/getServer", method = RequestMethod.GET)
    @ApiOperation(value = "获取服务器")
    public Object getServer(HttpSession session, Long terminalId) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.getServer(userId, terminalId);
    }

    @RequiresPermissions("terminal:sendTask")
    @RequestMapping(value = "/sendTask", method = RequestMethod.POST)
    @ApiOperation(value = "任务发布")
    public Object sendTask(HttpSession session, @RequestBody CmdRequest cmdRequest) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.sendTask(userId, cmdRequest);
    }

    @RequiresPermissions("terminal:sendDefaultTask")
    @RequestMapping(value = "/sendDefaultTask", method = RequestMethod.POST)
    @ApiOperation(value = "默认任务发布")
    public Object sendDefaultTask(HttpSession session, @RequestBody CmdRequest cmdRequest) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.sendDefaultTask(userId, cmdRequest);
    }

    @RequiresPermissions("terminal:city")
    @RequestMapping(value = "/setCity", method = RequestMethod.POST)
    @ApiOperation(value = "设置城市")
    public Object setCity(HttpSession session, @RequestBody CmdRequest cmdRequest) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.setCity(userId, cmdRequest);
    }

    @RequiresPermissions("terminal:city")
    @RequestMapping(value = "/getCity", method = RequestMethod.GET)
    @ApiOperation(value = "获取城市")
    public Object getCity(HttpSession session, Long terminalId) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.getCity(userId, terminalId);
    }

    @RequiresPermissions("terminal:upgrade")
    @RequestMapping(value = "/sendUpgrade", method = RequestMethod.POST)
    @ApiOperation(value = "终端升级")
    public Object sendUpgrade(HttpSession session, @RequestBody CmdRequest cmdRequest) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.sendUpgrade(userId, cmdRequest);
    }

    @RequiresPermissions("terminal:checkTask")
    @RequestMapping(value = "/checkTask", method = RequestMethod.GET)
    @ApiOperation(value = "任务检查")
    public Object checkTask(HttpSession session, Long terminalId) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.checkTask(userId, terminalId);
    }

    @RequiresPermissions("terminal:selfCmd")
    @RequestMapping(value = "/selfCmd", method = RequestMethod.GET)
    @ApiOperation(value = "自定义命令")
    public Object selfCmd(HttpSession session, Long terminalId, String cmd) {
        long userId = (long) session.getAttribute("userId");
        return cmdService.selfCmd(userId, terminalId, cmd);
    }
}
