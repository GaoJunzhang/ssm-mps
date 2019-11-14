package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.SystemSetting;
import com.seeyoo.mps.vo.Result;

/**
 * 系统设置接口
 *
 * @author Wangj
 */
public interface SystemSettingService extends BaseService<SystemSetting, String> {

    String getSetting(String key, String defaultSeting);

    String setSetting(String key, String setting);
}