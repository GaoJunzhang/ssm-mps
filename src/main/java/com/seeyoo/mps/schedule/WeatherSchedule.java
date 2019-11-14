package com.seeyoo.mps.schedule;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.UserWeatherRepository;
import com.seeyoo.mps.dao.WeatherRepository;
import com.seeyoo.mps.model.UserWeather;
import com.seeyoo.mps.model.Weather;
import com.seeyoo.mps.service.SystemSettingService;
import com.seeyoo.mps.tool.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@EnableScheduling
public class WeatherSchedule implements SchedulingConfigurer {
    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private UserWeatherRepository userWeatherRepository;

    @Autowired
    private WeatherRepository weatherRepository;

    @Value("${weather.enable}")
    private boolean weatherEnable;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                log.info("check weather:{}", DateUtil.now());
                if (!weatherEnable) {
                    return;
                }
                String key = systemSettingService.getSetting("weather", "");
                if (StrUtil.isEmpty(key))
                    return;
                JSONObject nowApi = new JSONObject(key);
                String appKey = nowApi.optString("key");
                String appSign = nowApi.optString("sign");
                String time = nowApi.optString("time");

                if (StrUtil.isEmpty(appKey) || StrUtil.isEmpty(appSign) || StrUtil.isEmpty(time)) {
                    log.info("nowapi param is empty");
                    return;
                }
                Set<Long> weatherIds = new HashSet<>();
                List<UserWeather> userWeathers = userWeatherRepository.findAll();
                for (UserWeather userWeather : userWeathers) {
                    Weather weather = userWeather.getWeather();
                    if (weatherIds.contains(weather.getId()))
                        continue;
                    weatherIds.add(weather.getId());
                    try {
                        String info = HttpUtil.httpRequestGet("http://api.k780.com/?app=weather.future&weaid=" + weather.getWeatherId() +
                                "&appkey=" + appKey + "&sign=" + appSign +
                                "&format=json", null, String.class);
                        log.info(info);
                        weather.setWeather(info);
                        weather.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                        weatherRepository.save(weather);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                String cron = "0 * * * * ?";
                log.info("nextExecutionTime");
                try {
                    String weather = systemSettingService.getSetting("weather", "");
                    if (!StrUtil.isEmpty(weather)) {
                        JSONObject nowApi = new JSONObject(weather);
                        String appKey = nowApi.optString("key");
                        String appSign = nowApi.optString("sign");
                        String time = nowApi.optString("time");

                        if (!StrUtil.isEmpty(appKey) && !StrUtil.isEmpty(appSign) && !StrUtil.isEmpty(time)) {
                            String[] times = time.split(":");
                            if (times.length == 2) {
                                int hour = Integer.valueOf(times[0]);
                                int min = Integer.valueOf(times[1]);
                                if (hour >= 0 && hour < 24 && min >= 0 && min < 60) {
                                    cron = "0 " + min + " " + hour + " * * ?";
                                }
                            }
                        }
                    }

                } catch (Exception e) {

                }
                log.info("cron:{}", cron);
                CronTrigger trigger = new CronTrigger(cron);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                log.info("next Date:{}", DateUtil.format(nextExec, "yyyy-MM-dd HH:mm:ss"));
                return nextExec;
            }
        });
    }
}
