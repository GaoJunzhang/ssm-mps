package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.seeyoo.mps.generator.base.BaseRepository;

import java.util.List;

/**
 * 任务数据处理层
 *
 * @author Wangj
 */
public interface TaskRepository extends BaseRepository<Task, Long> {
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);
}