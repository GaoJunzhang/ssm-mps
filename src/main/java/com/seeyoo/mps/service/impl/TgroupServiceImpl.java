package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.TgroupRepository;
import com.seeyoo.mps.model.Tgroup;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.TgroupService;
import com.seeyoo.mps.service.UserService;
import com.seeyoo.mps.tool.CommonUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 终端组接口实现
 *
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class TgroupServiceImpl implements TgroupService {

    @Autowired
    private TgroupRepository tgroupRepository;

    @Autowired
    private UserService userService;

    @Override
    public TgroupRepository getRepository() {
        return tgroupRepository;
    }

    public Map<String, Object> findByCondition(String name, SearchVo searchVo, Pageable pageable) {
        Page<Tgroup> tgroups = tgroupRepository.findAll(new Specification<Tgroup>() {
            @Override
            public Predicate toPredicate(Root<Tgroup> r, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                return predicate;
            }
        }, pageable);
        List<Map<String, Object>> industryMap = new ArrayList<>();
        for (Tgroup obj : tgroups) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", obj.getId());
            u.put("name", obj.getName());
            u.put("createTime", DateUtil.format(obj.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            industryMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("totalCount", tgroups.getTotalElements());
        map.put("data", industryMap);
        return map;
    }

    @Override
    public List<HashMap<String, Object>> tgroupListData(Long tgroupId) {
        List<HashMap<String, Object>> mgroupList = new ArrayList<>();
        List<Tgroup> tgroups = tgroupRepository.findAllByTgroupIdAndIsDelete(tgroupId, (short) 0);
        if (tgroups == null)
            tgroups = new ArrayList<>();
        for (Tgroup tgroup : tgroups) {
            HashMap<String, Object> m = new HashMap<>();
            m.put("key", tgroup.getId());
            m.put("title", tgroup.getName());
            mgroupList.add(m);
        }
        return mgroupList;
    }

    @Override
    public HashMap<String, Object> tgroupData(String name, Integer page) {

        Page<Tgroup> tgroups = tgroupRepository.findAll(new Specification<Tgroup>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<Tgroup> r, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                return predicate;
            }
        }, PageRequest.of(page > 0 ? page - 1 : page, 1, Sort.Direction.ASC, "name"));
        List<Long> allTgroups = new ArrayList<>();
        Long key = 0l;
        HashMap<String, Object> tgroup = new HashMap<>();
        if (tgroups.getContent().size() > 0) {
            Tgroup t = tgroups.getContent().get(0);
            key = t.getId();
            boolean isAuth = true;
            while (isAuth && t.getTgroup() != null) {
                t = t.getTgroup();
                allTgroups.add(t.getId());
            }
        }
        tgroup.put("key", key);
        tgroup.put("totalCount", tgroups.getTotalElements());
        tgroup.put("pageNo", page);
        tgroup.put("data", allTgroups);
        return tgroup;
    }

    @Override
    public Object saveTgroup(Long userId, Long id, Long tgroupId, String name, Double lng, Double lat) {
        Tgroup tgroup = null;
        if (id == null) {
            if (tgroupId == null) {
                return new ResultUtil().setErrorMsg("上级终端不能为空！");
            }
            tgroup = new Tgroup();

            Tgroup tgroup1 = tgroupRepository.findById(tgroupId).orElse(null);
            tgroup.setLev(tgroup1.getLev() + 1);
            tgroup.setTgroup(tgroup1);
            tgroup.setIsDelete((short) 0);
            tgroup.setCreateTime(new Timestamp(System.currentTimeMillis()));
            String newCode = CommonUtil.generateNewCode(5, tgroupRepository.maxTgroupCode(tgroupId));
            if (StrUtil.isEmpty(newCode)) {
                return new ResultUtil().setErrorMsg("超过最大值");
            }
            tgroup.setCode(tgroup1.getCode() + newCode);
            User user = new User();
            user.setId(userId);
            tgroup.setUser(user);
        } else {
            tgroup = tgroupRepository.findById(id).orElse(null);
            tgroup.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        }
        tgroup.setName(name);
        tgroup.setLng(lng);
        tgroup.setLat(lat);
        tgroupRepository.save(tgroup);
        return new ResultUtil<>().setData(tgroup.getTgroup().getId());
    }

    @Override
    public HashMap<String, Object> tgroupById(Long userId, Long id) {
        HashMap<String, Object> tgroup = new HashMap<>();
        List<String> userTgroupCode = userService.userTgroupCodes(userId);
        Specification specification = new Specification<Tgroup>() {
            @Override
            public Predicate toPredicate(Root<Tgroup> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));

                Predicate[] userMgroupPredicates = new Predicate[userTgroupCode.size()];
                for (int i = 0; i < userTgroupCode.size(); i++) {
                    userMgroupPredicates[i] = cb.like(r.get("code"), userTgroupCode.get(i) + "%");
                }
                predicate.getExpressions().add(cb.or(userMgroupPredicates));
                predicate.getExpressions().add(cb.equal(r.get("id"), id));
                return predicate;
            }
        };
        Page<Tgroup> tgroups = tgroupRepository.findAll(specification, PageRequest.of(0, 1, Sort.Direction.ASC, "name"));
        List<Long> allTgroups = new ArrayList<>();
        Long key = 0l;
        if (tgroups.getContent().size() > 0) {
            Tgroup t = tgroups.getContent().get(0);
            key = t.getId();
            boolean isAuth = true;

            while (isAuth && t.getTgroup() != null) {
                t = t.getTgroup();
                String mCode = t.getCode();
                isAuth = false;
                for (String code : userTgroupCode) {
                    if (mCode.startsWith(code)) {
                        isAuth = true;
                        allTgroups.add(t.getId());
                        break;
                    }
                }
            }
        }
        tgroup.put("key", key);
        tgroup.put("data", allTgroups);
        return tgroup;
    }

    @Cacheable(value = "tgroup_code", key = "#tgroupId")
    public String tgroupCode(Long tgroupId) {
        Tgroup tgroup = tgroupRepository.findById(tgroupId).orElse(null);
        if (tgroup != null) {
            return tgroup.getCode();
        }
        return "";
    }
}