package com.seeyoo.mps.service.impl;

import com.seeyoo.mps.dao.SystemSettingRepository;
import com.seeyoo.mps.model.SystemSetting;
import com.seeyoo.mps.service.SystemSettingService;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统设置接口实现
 *
 * @author Wangj
 */
@Slf4j
@Service
@Transactional
public class SystemSettingServiceImpl implements SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Override
    public SystemSettingRepository getRepository() {
        return systemSettingRepository;
    }

    @CacheEvict(value = "system_setting", key = "#key")
    public String setSetting(String key, String setting) {
        SystemSetting systemSetting = new SystemSetting();
        systemSetting.setName(key);
        systemSetting.setSetting(setting);
        log.info("save setting:{}-->{}", key, setting);
        systemSettingRepository.save(systemSetting);
        return setting;
    }

    @Cacheable(value = "system_setting", key = "#key")
    public String getSetting(String key, String defaultSeting) {
        try {
            SystemSetting systemSetting = systemSettingRepository.findById(key).orElse(null);
            if (!StrUtil.isEmpty(systemSetting.getSetting())) {
                return systemSetting.getSetting();
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return defaultSeting;
    }
}