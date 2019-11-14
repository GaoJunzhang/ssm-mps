package com.seeyoo.mps.service;

import com.seeyoo.mps.controller.request.AuditTaskRequest;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Task;
import com.seeyoo.mps.vo.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.seeyoo.mps.vo.SearchVo;

import java.util.List;
import java.util.Map;

/**
 * 任务接口
 *
 * @author Wangj
 */
public interface TaskService extends BaseService<Task, Long> {

    Map<String, Object> taskList(Long userId, Long mgroupId, String name, Short audit, Integer page, Integer size, String sortOrder, String sortValue);

    Result delTask(Long[] ids);

    Result auditTask(Long userId, AuditTaskRequest auditTaskRequest);

    Result auditTaskRecords(Long id);

    Result saveTask(Long userId, String taskContent);

    Result task(Long userId, Long id);
}