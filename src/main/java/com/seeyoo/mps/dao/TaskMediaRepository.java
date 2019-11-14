package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.TaskMedia;

import java.util.List;

/**
 * 任务数据处理层
 *
 * @author Wangj
 */
public interface TaskMediaRepository extends BaseRepository<TaskMedia, Long> {

    List<TaskMedia> findAllByTaskId(Long taskId);

    void deleteByTaskId(Long id);
}