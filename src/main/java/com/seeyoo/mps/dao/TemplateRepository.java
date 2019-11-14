package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import com.seeyoo.mps.generator.base.BaseRepository;

import java.util.List;

/**
 * 模板数据处理层
 *
 * @author Wangj
 */
public interface TemplateRepository extends BaseRepository<Template, Long> {

}