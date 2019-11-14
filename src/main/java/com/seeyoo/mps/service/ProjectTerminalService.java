package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.ProjectTerminal;
import com.seeyoo.mps.vo.Result;
import io.swagger.models.auth.In;

import java.io.InputStream;

/**
 * 方案排期接口
 *
 * @author Wangj
 */
public interface ProjectTerminalService extends BaseService<ProjectTerminal, Long> {

    Result projectTerminals(Long userId, Long id);

    Result uploadTerminals(Long userId, InputStream inputStream);

    Result projectSchedulingList(Long userId, Long tgroupId, String mac, String name, String start, String end, Integer page, Integer size);

    Result exportVacancy(Long userId, Long tgroupId, String start, String end, int count);
}