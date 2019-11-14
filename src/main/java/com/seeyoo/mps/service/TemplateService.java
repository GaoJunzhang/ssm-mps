package com.seeyoo.mps.service;

import com.seeyoo.mps.controller.request.SaveTemplateRequest;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.seeyoo.mps.vo.SearchVo;

import java.util.List;
import java.util.Map;

/**
 * 模板接口
 *
 * @author Wangj
 */
public interface TemplateService extends BaseService<Template, Long> {

    Map<String, Object> templateList(Long userId, String name, Short screen, Integer page, Integer size, String sortOrder, String sortValue);

    Object saveTemplate(Long userId, SaveTemplateRequest saveTemplateRequest);

    Object delTemplate(Long userId, Long templateId);
}