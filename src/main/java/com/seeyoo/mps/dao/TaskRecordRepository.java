package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.TaskRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * 任务记录数据处理层
 *
 * @author Wangj
 */
public interface TaskRecordRepository extends BaseRepository<TaskRecord, Long> {
    Page<TaskRecord> findAll(Specification<TaskRecord> spec, Pageable pageable);
}