package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.TaskAudit;

import java.util.List;

/**
 * 任务审核数据处理层
 *
 * @author Wangj
 */
public interface TaskAuditRepository extends BaseRepository<TaskAudit, Long> {
    List<TaskAudit> findAllByTaskIdOrderByCreateTimeDesc(Long taskId);
}