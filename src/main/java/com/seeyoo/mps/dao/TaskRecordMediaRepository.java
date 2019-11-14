package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.TaskRecordMedia;

import java.util.List;

/**
 * 任务记录数据处理层
 *
 * @author Wangj
 */
public interface TaskRecordMediaRepository extends BaseRepository<TaskRecordMedia, Long> {
    List<TaskRecordMedia> findAllByTaskRecordId(Long id);
}