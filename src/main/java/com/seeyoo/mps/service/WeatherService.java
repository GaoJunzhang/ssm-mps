package com.seeyoo.mps.service;

import com.seeyoo.mps.controller.request.AuditTaskRequest;
import com.seeyoo.mps.controller.request.IdRequest;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Task;
import com.seeyoo.mps.model.Weather;
import com.seeyoo.mps.vo.Result;

import java.util.Map;

/**
 * 天气接口
 *
 * @author Wangj
 */
public interface WeatherService extends BaseService<Weather, Long> {

    Result weatherCityData();

    Result refreshWeatherCity();

    Map<String, Object> userWeatherList(String userCode, String name);

    Result addUserWeather(Long userId, IdRequest idRequest);

    Result delUserWeather(String userCode, Long[] ids);
}