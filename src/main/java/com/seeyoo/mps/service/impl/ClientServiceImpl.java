package com.seeyoo.mps.service.impl;

import com.seeyoo.mps.dao.ClientRepository;
import com.seeyoo.mps.model.Client;
import com.seeyoo.mps.model.Industry;
import com.seeyoo.mps.service.ClientService;
import com.seeyoo.mps.vo.SearchVo;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.*;
import java.lang.reflect.Field;

/**
 * 广告主接口实现
 *
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public ClientRepository getRepository() {
        return clientRepository;
    }

    @Override
    public Map<String, Object> findByCondition(String name,String salerName,Long industryId, Long userId, SearchVo searchVo, Pageable pageable) {

        Page<Client> clients = clientRepository.findAll(new Specification<Client>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<Client> r, CriteriaQuery<?> cq, CriteriaBuilder cb) {

                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                if (StrUtil.isNotEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                if (userId != null) {
                    predicate.getExpressions().add(cb.equal(r.get("user").get("id"), userId));
                }
                if (StrUtil.isNotEmpty(salerName)) {
                    predicate.getExpressions().add(cb.like(r.get("saler").get("name"), "%" + salerName + "%"));
                }
                if (industryId != null) {
                    predicate.getExpressions().add(cb.equal(r.get("industry").get("id"), industryId));
                }
                return predicate;
            }
        }, pageable);
        List<Map<String, Object>> industryMap = new ArrayList<>();
        for (Client obj : clients) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", obj.getId());
            u.put("industryId", obj.getIndustry().getId());
            u.put("industryName", obj.getIndustry().getName());
            u.put("name", obj.getName());
            u.put("userName", obj.getUser().getName());
            u.put("salerName", obj.getSaler().getName());
            u.put("salerId", obj.getSaler().getId());
            u.put("area", obj.getArea());
            u.put("position", obj.getPosition());
            u.put("contactType", obj.getContactType());
            u.put("contact", obj.getContact());
            u.put("shortName", obj.getShortName());
            u.put("createTime", DateUtil.format(obj.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            industryMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("totalCount", clients.getTotalElements());
        map.put("data", industryMap);
        return map;
    }

    public void updateIsDelete(Long id) {
        clientRepository.updateIsDelete(id);
    }
}