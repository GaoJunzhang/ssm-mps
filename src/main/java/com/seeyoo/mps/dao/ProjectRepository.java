package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * 任务数据处理层
 *
 * @author Wangj
 */
public interface ProjectRepository extends BaseRepository<Project, Long> {
    Page<Project> findAll(Specification<Project> spec, Pageable pageable);

    @Query(value = "from Project p where p.isDelete = 0 and (p.audit = 2 or p.audit = 3) and p.validStart <= :date and p.validEnd >= :date")
    List<Project> projectsByDate(@Param("date") Date date);
}