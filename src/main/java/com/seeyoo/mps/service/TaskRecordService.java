package com.seeyoo.mps.service;

import com.seeyoo.mps.bean.SimpleTerminalBean;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.TaskRecord;
import com.seeyoo.mps.model.TaskRecordEnum;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 任务记录接口
 *
 * @author Wangj
 */
public interface TaskRecordService extends BaseService<TaskRecord, Long> {

    Map<String, Object> taskRecordList(Long userId, String userCode, String name, String userName, Short type, Integer page, Integer size, String sortOrder, String sortValue);

    Map<String, Object> taskRecordTerminals(Long userId, Long taskRecordId, String name, Integer page, Integer size, String sortOrder, String sortValue);

    Map<String, Object> taskRecordMedias(Long userId, Long taskRecordId, String name);

    void saveTaskRecord(Long userId, TaskRecordEnum type, Long id, String name, Timestamp start, Timestamp end, List<SimpleTerminalBean> terminals);
}