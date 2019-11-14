package com.seeyoo.mps.controller;

import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.controller.request.SaveSystemSettingRequest;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.SearchVo;
import com.seeyoo.mps.model.SystemSetting;
import com.seeyoo.mps.service.SystemSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Wangj
 */
@Slf4j
@RestController
@Api(description = "系统设置管理接口")
@RequestMapping("/systemSetting")
@Transactional
public class SystemSettingController extends BaseController<SystemSetting, String> {

    @Autowired
    private SystemSettingService systemSettingService;

    @Override
    public SystemSettingService getService() {
        return systemSettingService;
    }

    @RequiresPermissions("setting:aliyunOss")
    @RequestMapping(value = "/getAliyunOssSetting", method = RequestMethod.GET)
    @ApiOperation(value = "获取OSS设置")
    public Result getAliyunOssSetting() {
        return new ResultUtil<>().setData(systemSettingService.getSetting("aliyun_oss", "{}"));
    }

    @RequiresPermissions("setting:aliyunOss")
    @RequestMapping(value = "/setAliyunOssSetting", method = RequestMethod.POST)
    @ApiOperation(value = "设置OSS")
    public Object setAliyunOssSetting(@RequestBody SaveSystemSettingRequest saveSystemSettingRequest) {
        return new ResultUtil().setSuccessMsg(systemSettingService.setSetting("aliyun_oss", saveSystemSettingRequest.getSetting()));
    }

    @RequiresPermissions("setting:base")
    @RequestMapping(value = "/getBaseSetting", method = RequestMethod.GET)
    @ApiOperation(value = "获取基本设置")
    public Result getBaseSetting() {
        HashMap<String, Object> settings = new HashMap<>();
        settings.put("adDuration", systemSettingService.getSetting("ad_duration", "15"));
        settings.put("connHeartbeat", systemSettingService.getSetting("conn_heartbeat", "30"));
        settings.put("serverUrl", systemSettingService.getSetting("server_url", "http://localhost:9999/"));

        return new ResultUtil<>().setData(settings);
    }

    @RequiresPermissions("setting:base")
    @RequestMapping(value = "/setAdDurationSetting", method = RequestMethod.POST)
    public Object setAdDurationSetting(@RequestBody SaveSystemSettingRequest saveSystemSettingRequest) {
        if (StrUtil.isEmpty(saveSystemSettingRequest.getSetting())) {
            return new ResultUtil().setErrorMsg("无效的参数");
        }
        return new ResultUtil().setSuccessMsg(systemSettingService.setSetting("ad_duration", saveSystemSettingRequest.getSetting()));
    }

    @RequiresPermissions("setting:base")
    @RequestMapping(value = "/setConnHeartbeatSetting", method = RequestMethod.POST)
    public Object setConnHeartbeatSetting(@RequestBody SaveSystemSettingRequest saveSystemSettingRequest) {
        if (StrUtil.isEmpty(saveSystemSettingRequest.getSetting())) {
            return new ResultUtil().setErrorMsg("无效的参数");
        }
        return new ResultUtil().setSuccessMsg(systemSettingService.setSetting("conn_heartbeat", saveSystemSettingRequest.getSetting()));
    }

    @RequiresPermissions("setting:base")
    @RequestMapping(value = "/setServerUrlSetting", method = RequestMethod.POST)
    public Object setServerUrlSetting(@RequestBody SaveSystemSettingRequest saveSystemSettingRequest) {
        if (StrUtil.isEmpty(saveSystemSettingRequest.getSetting())) {
            return new ResultUtil().setErrorMsg("无效的参数");
        }
        return new ResultUtil().setSuccessMsg(systemSettingService.setSetting("server_url", saveSystemSettingRequest.getSetting()));
    }

    @RequiresPermissions("setting:weather")
    @RequestMapping(value = "/getWeatherSetting", method = RequestMethod.GET)
    @ApiOperation(value = "获取天气设置")
    public Result getWeatherSetting() {
        return new ResultUtil<>().setData(systemSettingService.getSetting("weather", "{}"));
    }

    @RequiresPermissions("setting:weather")
    @RequestMapping(value = "/setWeatherSetting", method = RequestMethod.POST)
    @ApiOperation(value = "设置天气")
    public Object setWeatherSetting(@RequestBody SaveSystemSettingRequest saveSystemSettingRequest) {
        return new ResultUtil().setSuccessMsg(systemSettingService.setSetting("weather", saveSystemSettingRequest.getSetting()));
    }
}
