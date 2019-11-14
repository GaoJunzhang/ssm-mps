package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.ProjectAudit;

import java.util.List;

/**
 * 方案审核数据处理层
 *
 * @author Wangj
 */
public interface ProjectAuditRepository extends BaseRepository<ProjectAudit, Long> {
    List<ProjectAudit> findAllByProjectIdOrderByCreateTimeDesc(Long projectId);
}