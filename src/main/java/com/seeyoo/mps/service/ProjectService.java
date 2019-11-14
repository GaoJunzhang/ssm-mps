package com.seeyoo.mps.service;

import com.seeyoo.mps.bean.ProjectBean;
import com.seeyoo.mps.controller.request.AuditProjectRequest;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Project;
import com.seeyoo.mps.vo.Result;

import java.io.InputStream;
import java.util.Map;

/**
 * 方案接口
 *
 * @author Wangj
 */
public interface ProjectService extends BaseService<Project, Long> {

    Map<String, Object> projectList(Long userId, Long mgroupId, String name, Short audit, String contractNo, String clientName, String salerName, Integer page, Integer size, String sortOrder, String sortValue);

    Result delProject(Long[] ids);

    Result auditProject(Long userId, AuditProjectRequest auditProjectRequest);

    Result auditProjectRecords(Long id);

    Result saveProject(Long userId, String projectContent);

    Result project(Long userId, Long id);

    Result projectMedias(Long userId, Long id);

    ProjectBean project(Long id);
}