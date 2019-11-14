package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.SalerRepository;
import com.seeyoo.mps.model.Saler;
import com.seeyoo.mps.service.SalerService;
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
 * 销售员接口实现
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class SalerServiceImpl implements SalerService {

    @Autowired
    private SalerRepository salerRepository;

    @Override
    public SalerRepository getRepository() {
        return salerRepository;
    }

    @Override
    public Map<String, Object> findByCondition(Saler saler, SearchVo searchVo, Pageable pageable) {

        Page<Saler> salers = salerRepository.findAll(new Specification<Saler>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<Saler> r, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                if (!StrUtil.isEmpty(saler.getName())) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + saler.getName() + "%"));
                }
                return predicate;
            }
        }, pageable);
        List<Map<String, Object>> industryMap = new ArrayList<>();
        for (Saler obj : salers) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", obj.getId());
            u.put("name", obj.getName());
            u.put("userName", obj.getUser().getName());
            u.put("sex", obj.getSex());
            u.put("tel", obj.getTel());
            u.put("createTime", DateUtil.format(obj.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            industryMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("totalCount", salers.getTotalElements());
        map.put("data", industryMap);
        return map;
    }
    public void updateIsDelete(Long id) {
        salerRepository.updateIsDelete(id);
    }
}