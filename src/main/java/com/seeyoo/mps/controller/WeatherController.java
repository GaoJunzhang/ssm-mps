package com.seeyoo.mps.controller;

import com.seeyoo.mps.controller.request.IdRequest;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.model.Weather;
import com.seeyoo.mps.service.WeatherService;
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
@Api(description = "天气接口")
@RequestMapping("/weather/*")
@Transactional
public class WeatherController extends BaseController<Weather, Long> {

    @Autowired
    private WeatherService weatherService;

    @Override
    public WeatherService getService() {
        return weatherService;
    }

    @RequestMapping(value = "/weatherCityData", method = RequestMethod.GET)
    @ApiOperation(value = "天气城市接口")
    public Object weatherCityData(HttpSession session) {
        return weatherService.weatherCityData();
    }

    @RequestMapping(value = "/refreshWeatherCity", method = RequestMethod.GET)
    @ApiOperation(value = "刷新天气城市")
    public Object refreshWeatherCity(HttpSession session) {
        return weatherService.refreshWeatherCity();
    }

    @RequiresPermissions("weatherList")
    @RequestMapping(value = "/weatherListData", method = RequestMethod.GET)
    @ApiOperation(value = "天气采集列表")
    public Object weatherListData(HttpSession session, String name) {
        String userCode = (String) session.getAttribute("userCode");
        return new ResultUtil().setData(weatherService.userWeatherList(userCode, name));
    }

    @RequiresPermissions("weather:del")
    @RequestMapping(value = "/delUserWeather/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "取消天气采集")
    public Object delUserWeather(HttpSession session, @PathVariable Long[] ids) {
        String userCode = (String) session.getAttribute("userCode");
        return weatherService.delUserWeather(userCode, ids);
    }

    @RequiresPermissions("weather:add")
    @RequestMapping(value = "/addUserWeather", method = RequestMethod.POST)
    public Object addUserWeather(HttpSession session, @RequestBody IdRequest idRequest) {
        long userId = (long) session.getAttribute("userId");
        return weatherService.addUserWeather(userId, idRequest);
    }
}
