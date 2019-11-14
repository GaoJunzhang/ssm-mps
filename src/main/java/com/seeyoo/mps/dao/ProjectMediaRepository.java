package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.ProjectMedia;

import java.util.List;

/**
 * 方案媒体数据处理层
 *
 * @author Wangj
 */
public interface ProjectMediaRepository extends BaseRepository<ProjectMedia, Long> {

    void deleteByProjectId(Long id);

    List<ProjectMedia> findAllByProjectId(Long id);
}