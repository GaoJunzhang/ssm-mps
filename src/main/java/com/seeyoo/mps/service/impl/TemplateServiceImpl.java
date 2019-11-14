package com.seeyoo.mps.service.impl;

import com.seeyoo.mps.controller.request.SaveTemplateRequest;
import com.seeyoo.mps.dao.TemplateRepository;
import com.seeyoo.mps.model.Template;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.TemplateService;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.SearchVo;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.*;

/**
 * 模板接口实现
 *
 * @author Wangj
 */
@Slf4j
@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Override
    public TemplateRepository getRepository() {
        return templateRepository;
    }

    @Override
    public Map<String, Object> templateList(Long userId, String name, Short screen, Integer page, Integer size, String sortOrder, String sortValue) {
        Specification specification = new Specification<Template>() {
            @Override
            public Predicate toPredicate(Root<Template> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                predicate.getExpressions().add(cb.equal(r.get("user").get("id"), userId));
                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                if (screen != null) {
                    if (screen.intValue() == 0) {
                        predicate.getExpressions().add(cb.equal(r.get("width"), 1920));
                    } else if (screen.intValue() == 1) {
                        predicate.getExpressions().add(cb.equal(r.get("width"), 1080));
                    }

                }
                return predicate;
            }
        };
        Page<Template> templates = templateRepository.findAll(specification, PageRequest.of(page > 0 ? page - 1 : page, size, sortOrder.equals("descend") ? Sort.Direction.DESC : Sort.Direction.ASC, sortValue));
        List<Map<String, Object>> templatesMap = new ArrayList<>();
        for (Template template : templates) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", template.getId());
            u.put("name", template.getName());
            u.put("width", template.getWidth());
            u.put("height", template.getHeight());
            u.put("areas", template.getAreas());
            u.put("userName", template.getUser().getName());
            u.put("createTime", DateUtil.format(template.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            templatesMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", page);
        map.put("totalCount", templates.getTotalElements());
        map.put("data", templatesMap);
        return map;
    }

    @Override
    public Result saveTemplate(Long userId, SaveTemplateRequest saveTemplateRequest) {
        Template template;
        if (saveTemplateRequest.getTemplateId() != null && saveTemplateRequest.getTemplateId().longValue() > 0) {
            template = templateRepository.findById(saveTemplateRequest.getTemplateId()).orElse(null);
            if (template == null) {
                return new ResultUtil().setErrorMsg("无效的参数");
            }
        } else {
            template = new Template();
        }
        template.setName(saveTemplateRequest.getName());
        template.setWidth(saveTemplateRequest.getWidth());
        template.setHeight(saveTemplateRequest.getHeight());
        template.setAreas(saveTemplateRequest.getAreas());
        template.setIsDelete((short) 0);
        User user = new User();
        user.setId(userId);
        template.setUser(user);
        templateRepository.save(template);
        return new ResultUtil<>().setData(template.getId());
    }

    @Override
    public Result delTemplate(Long userId, Long templateId) {
        if (templateId == null) {
            return new ResultUtil().setErrorMsg("无效的参数");
        }
        Template template = templateRepository.findById(templateId).orElse(null);
        if (template == null) {
            return new ResultUtil().setErrorMsg("请选择正确模板");
        }
        template.setIsDelete((short) 1);
        templateRepository.save(template);
        return new ResultUtil<>().setData(template.getId());
    }
}