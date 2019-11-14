package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.seeyoo.mps.dao.RoleRepository;
import com.seeyoo.mps.model.OperationLog;
import com.seeyoo.mps.model.Role;
import com.seeyoo.mps.service.RoleService;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色接口实现
 *
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public RoleRepository getRepository() {
        return roleRepository;
    }

    @Override
    public Map<String, Object> roleData(Role role, SearchVo searchVo, Pageable pageable){
        Page<Role> roles = roleRepository.findAll(new Specification<Role>() {
            @Override
            public Predicate toPredicate(Root<Role> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                if (!StrUtil.isEmpty(role.getName())) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + role.getName() + "%"));
                }
                return predicate;
            }
        }, pageable);
        List<Map<String, Object>> logMap = new ArrayList<>();
        for (Role obj : roles) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", obj.getId());
            u.put("name", obj.getName());
            u.put("role", obj.getRole());
            u.put("description",obj.getDescription());
            u.put("isDelete",obj.getIsDelete());
            u.put("createTime", DateUtil.format(obj.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            logMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("totalCount", roles.getTotalElements());
        map.put("data", logMap);
        return map;
    }

    public List<Role> findAllByRole(String role) {
        return roleRepository.findByRoleAndIsDelete(role, (short) 0);
    }

    public void updateIsDelete(Long id) {
        roleRepository.updateIsDelete(id);
    }

    public List<Role> findAllByIsDelete(Short isDelete) {
        return roleRepository.findAllByIsDelete(isDelete);
    }
}