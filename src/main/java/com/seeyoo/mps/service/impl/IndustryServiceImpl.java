package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.IndustryRepository;
import com.seeyoo.mps.model.Industry;
import com.seeyoo.mps.service.IndustryService;
import com.seeyoo.mps.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行业接口实现
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class IndustryServiceImpl implements IndustryService {

    @Autowired
    private IndustryRepository industryRepository;

    @Override
    public IndustryRepository getRepository() {
        return industryRepository;
    }

    @Override
    public Map<String, Object> findByCondition(Industry industry, SearchVo searchVo, Pageable pageable) {

        Page<Industry> industries =  industryRepository.findAll(new Specification<Industry>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<Industry> r, CriteriaQuery<?> cq, CriteriaBuilder cb) {

                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                if (!StrUtil.isEmpty(industry.getName())) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + industry.getName() + "%"));
                }
                return predicate;
            }
        }, pageable);
        List<Map<String, Object>> industryMap = new ArrayList<>();
        for (Industry industryObj : industries) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", industryObj.getId());
            u.put("name", industryObj.getName());
            u.put("userName", industryObj.getUser().getName());
            u.put("createTime", DateUtil.format(industryObj.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            industryMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("totalCount", industries.getTotalElements());
        map.put("data", industryMap);
        return map;
    }

    public void updateIsDelete(Long id) {
        industryRepository.updateIsDelete(id);
    }


    public List<Map<String, Object>> findAllByIsDeleteAndUserId(short isDelete, long userId) {
        List<Industry> industries = industryRepository.findAllByIsDeleteAndUserId(isDelete, userId);
        List<Map<String, Object>> industryMap = new ArrayList<>();
        for (Industry industryObj : industries) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", industryObj.getId());
            u.put("name", industryObj.getName());
            industryMap.add(u);
        }
        return industryMap;
    }
}