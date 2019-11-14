package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.ProjectTerminal;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * 方案终端数据处理层
 *
 * @author Wangj
 */
public interface ProjectTerminalRepository extends BaseRepository<ProjectTerminal, Long> {

    void deleteByProjectId(Long id);

    List<ProjectTerminal> findAllByProjectId(Long id);

    @Query("from ProjectTerminal pt where pt.terminal.id = ?1 and pt.project.isDelete = 0 " +
            "and (pt.project.audit = 2 or pt.project.audit = 3) " +
            "and ((pt.project.validStart between ?2 and ?3) or (pt.project.validEnd between ?2 and ?3) or (pt.project.validStart <= ?2 and pt.project.validEnd >= ?3))")
    List<ProjectTerminal> findByTerminalAndDate(Long terminalId, Date start, Date end);

}