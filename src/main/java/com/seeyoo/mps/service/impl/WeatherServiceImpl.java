package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.controller.request.IdRequest;
import com.seeyoo.mps.dao.*;
import com.seeyoo.mps.model.*;
import com.seeyoo.mps.service.SystemSettingService;
import com.seeyoo.mps.service.WeatherService;
import com.seeyoo.mps.tool.HttpUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.*;

/**
 * 天气接口实现
 *
 * @author Wangj
 */
@Slf4j
@Service
@Transactional
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private UserWeatherRepository userWeatherRepository;

    @Autowired
    private SystemSettingService systemSettingService;

    @Override
    public WeatherRepository getRepository() {
        return weatherRepository;
    }

    @Transactional
    @Override
    public Result weatherCityData() {
        List<Weather> weathers = weatherRepository.findAll();

        weathers.sort(new Comparator<Weather>() {
            @Override
            public int compare(Weather o1, Weather o2) {
                if (!o1.getArea1().equals(o2.getArea1()))
                    return o1.getArea1().compareTo(o2.getArea1());
                if (!o1.getArea2().equals(o2.getArea2()))
                    return o1.getArea2().compareTo(o2.getArea2());
                return o1.getArea3().compareTo(o2.getArea3());
            }
        });
        Set<String> area1 = new HashSet<>();
        List<HashMap<String, Object>> citys = new ArrayList<>();
        for (Weather weather : weathers) {
            if (area1.contains(weather.getArea1()))
                continue;

            String nameA = weather.getArea1();
            HashMap<String, Object> areaA = new HashMap<>();
            areaA.put("id", nameA);
            areaA.put("city", nameA);
            Set<String> area2 = new HashSet<>();
            List<HashMap<String, Object>> childrenA = new ArrayList<>();
            for (Weather weather2 : weathers) {
                if (!weather2.getArea1().equals(nameA))
                    continue;
                if (area2.contains(weather2.getArea2()))
                    continue;
                String nameB = weather2.getArea2();
                HashMap<String, Object> areaB = new HashMap<>();
                areaB.put("id", nameB);
                areaB.put("city", nameB);
                List<HashMap<String, Object>> childrenB = new ArrayList<>();
                for (Weather weather3 : weathers) {
                    if (!weather3.getArea1().equals(nameA))
                        continue;
                    if (!weather3.getArea2().equals(nameB))
                        continue;
                    HashMap<String, Object> areaC = new HashMap<>();
                    areaC.put("id", weather3.getId());
                    areaC.put("city", weather3.getArea3());
                    childrenB.add(areaC);
                }
                areaB.put("children", childrenB);
                area2.add(nameB);
                childrenA.add(areaB);
            }
            areaA.put("children", childrenA);
            area1.add(nameA);
            citys.add(areaA);
        }
        return new ResultUtil<>().setData(citys);
    }

    @Override
    public Result refreshWeatherCity() {
        weatherRepository.deleteAll();
        try {
            String response = HttpUtil.httpRequestGet("http://api.k780.com/?app=weather.city&cou=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json", null, String.class);
            log.info("{}", response);
            if (StrUtil.isEmpty(response)) {
                new ResultUtil<>().setErrorMsg("无效的请求");
            }

            JSONObject res = new JSONObject(response);
            if (!res.optString("success").equals("1")) {
                new ResultUtil<>().setErrorMsg(res.optString("success"));
            }

            JSONObject result = res.optJSONObject("result");
            if (result == null) {
                new ResultUtil<>().setErrorMsg(res.optString("result is null"));
            }

            JSONObject citys = result.optJSONObject("datas");
            if (citys == null) {
                new ResultUtil<>().setErrorMsg(res.optString("datas is null"));
            }

            List<Weather> weathers = new ArrayList<>();
            Iterator<String> it = citys.keys();
            while (it.hasNext()) {
                String key = it.next();

                JSONObject city = citys.optJSONObject(key);
                Integer weaId = city.optInt("weaid");
                String cityName = city.optString("citynm");
                String cityId = city.optString("cityid");
                String area1 = city.optString("area_1");
                String area2 = city.optString("area_2");
                String area3 = city.optString("area_3");
                log.info("weaId:{}", weaId);
                if (StrUtil.isEmpty(area3)) {
                    area3 = cityName;
                }

                Weather weather = new Weather();
                weather.setArea1(area1);
                weather.setArea2(area2);
                if (area3.equals(cityName)) {
                    weather.setArea3(area3);
                } else {
                    weather.setArea3(cityName + area3);
                    log.info(cityName + area3);
                }
                weather.setCityName(cityName);
                weather.setWeatherId(weaId);
                weather.setCityId(cityId);
                weathers.add(weather);

                if (weathers.size() >= 100) {
                    weatherRepository.saveAll(weathers);
                    weathers.clear();
                }
            }
            if (weathers.size() > 0) {
                weatherRepository.saveAll(weathers);
            }
            return new ResultUtil<>().setData("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResultUtil<>().setErrorMsg("请求出错");
    }

    @Override
    @Transactional
    public Result addUserWeather(Long userId, IdRequest idRequest) {
        Long[] ids = idRequest.getIds();
        if (ids == null) {
            return new ResultUtil().setErrorMsg("无效的城市");
        }
        User user = new User();
        user.setId(userId);

        String nowapi = systemSettingService.getSetting("nowapi_key", "");
        if (StrUtil.isEmpty(nowapi)) {
            return new ResultUtil().setErrorMsg("未配置采集参数");
        }
        JSONObject nowApi = new JSONObject(nowapi);
        String appKey = nowApi.optString("key");
        String appSign = nowApi.optString("sign");

        if (StrUtil.isEmpty(appKey) || StrUtil.isEmpty(appSign)) {
            return new ResultUtil().setErrorMsg("未配置采集参数");
        }

        for (Long id : ids) {
            List<UserWeather> userWeathers = userWeatherRepository.findAllByUserIdAndWeatherId(userId, id);
            if (userWeathers.size() > 0) {
                continue;
            }
            UserWeather userWeather = new UserWeather();
            userWeather.setUser(user);
            Weather weather = weatherRepository.findById(id).orElse(null);
            try {
                String info = HttpUtil.httpRequestGet("http://api.k780.com/?app=weather.future&weaid=" + weather.getWeatherId() +
                        "&appkey=" + appKey + "&sign=" + appSign +
                        "&format=json", null, String.class);
                weather.setWeather(info);
                weather.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                weatherRepository.save(weather);
            } catch (Exception e) {
                e.printStackTrace();
            }
            userWeather.setWeather(weather);
            userWeatherRepository.save(userWeather);
        }

        return new ResultUtil<>().setData("");
    }

    @Override
    @Transactional
    public Result delUserWeather(String userCode, Long[] ids) {
        if (ids == null) {
            return new ResultUtil().setErrorMsg("请选择城市");
        }
        for (Long id : ids) {
            UserWeather userWeather = userWeatherRepository.findById(id).orElse(null);
            if (userWeather == null) {
                return new ResultUtil().setErrorMsg("请选择正确的城市");
            }
            List<UserWeather> userWeahters = userWeatherRepository.findAllByUserCodeAndWeatherId(userCode, userWeather.getWeather().getId());
            userWeatherRepository.deleteAll(userWeahters);
        }
        return new ResultUtil<>().setData("");
    }

    @Override
    public Map<String, Object> userWeatherList(String userCode, String name) {
        Specification specification = new Specification<UserWeather>() {
            @Override
            public Predicate toPredicate(Root<UserWeather> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.like(r.get("user").get("code"), userCode + "%"));


                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("weather").get("cityName"), "%" + name + "%"));
                }
                return predicate;
            }
        };
        Set<Long> weatherIds = new HashSet<>();
        List<UserWeather> userWeathers = userWeatherRepository.findAll(specification, new Sort(Sort.Direction.DESC, "createTime"));
        List<Map<String, Object>> weathersMap = new ArrayList<>();
        for (UserWeather userWeather : userWeathers) {
            if(weatherIds.contains(userWeather.getWeather().getId()))
                continue;
            weatherIds.add(userWeather.getWeather().getId());
            Map<String, Object> u = new HashMap<>();
            u.put("id", userWeather.getId());
            u.put("city", userWeather.getWeather().getCityName());
            u.put("info", getWeatherTemperature(userWeather.getWeather().getWeather()));
            u.put("updateTime", DateUtil.format(userWeather.getWeather().getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
            weathersMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("data", weathersMap);
        return map;
    }

    private String getWeatherTemperature(String weather) {
        try {
            if (!StrUtil.isEmpty(weather)) {
                JSONObject weatherObj = new JSONObject(weather);
                String success = weatherObj.optString("success", "0");
                if (success.equals("1")) {
                    JSONArray weathers = weatherObj.optJSONArray("result");
                    if (weathers.length() > 0) {
                        JSONObject today = weathers.optJSONObject(0);
                        return today.optString("weather") + "  " + today.optString("temperature");
                    }
                } else {
                    return weatherObj.optString("msg");
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return StrUtil.EMPTY;
    }
}