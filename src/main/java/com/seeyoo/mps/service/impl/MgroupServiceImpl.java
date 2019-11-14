package com.seeyoo.mps.service.impl;

import com.seeyoo.mps.controller.request.SaveMgroupRequest;
import com.seeyoo.mps.dao.MgroupRepository;
import com.seeyoo.mps.dao.UserMgroupRepository;
import com.seeyoo.mps.model.Mgroup;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.model.UserMgroup;
import com.seeyoo.mps.service.MgroupService;
import com.seeyoo.mps.service.UserService;
import com.seeyoo.mps.tool.CommonUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.tool.TreeUtil;
import com.seeyoo.mps.vo.Result;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.vo.Tree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * 媒体组接口实现
 *
 * @author Wangj
 */
@Slf4j
@Service
@Transactional
public class MgroupServiceImpl implements MgroupService {

    @Autowired
    private MgroupRepository mgroupRepository;

    @Autowired
    private UserService userService;

    @Override
    public MgroupRepository getRepository() {
        return mgroupRepository;
    }

    @Autowired
    private UserMgroupRepository userMgroupRepository;

    @Override
    public List<HashMap<String, Object>> mgroupListData(Long userId, Long mgroupId) {
        List<HashMap<String, Object>> mgroupList = new ArrayList<>();
        List<Mgroup> mgroups = new ArrayList<>();
        if (mgroupId == null) {
            List<UserMgroup> userMgroups = userMgroupRepository.userMgroupsByUserAndName(userId);
            for (UserMgroup userMgroup : userMgroups) {
                mgroups.add(userMgroup.getMgroup());
            }
        } else {
            mgroups = mgroupRepository.mgroupsByIdAndName(mgroupId);
        }
        if (mgroups == null)
            mgroups = new ArrayList<>();

        for (Mgroup mgroup : mgroups) {
            HashMap<String, Object> m = new HashMap<>();
            m.put("key", mgroup.getId());
            m.put("title", mgroup.getName());
            m.put("share", mgroup.getShare());
            mgroupList.add(m);
        }
        return mgroupList;
    }

    @Override
    public HashMap<String, Object> mgroupByName(Long userId, String name, Integer page) {
        HashMap<String, Object> mgroup = new HashMap<>();
        List<String> userMgroupCode = userService.userMgroupCodes(userId);
        Specification specification = new Specification<Mgroup>() {
            @Override
            public Predicate toPredicate(Root<Mgroup> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));

                Predicate[] userMgroupPredicates = new Predicate[userMgroupCode.size()];
                for (int i = 0; i < userMgroupCode.size(); i++) {
                    userMgroupPredicates[i] = cb.like(r.get("code"), userMgroupCode.get(i) + "%");
                }
                predicate.getExpressions().add(cb.or(userMgroupPredicates));

                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                return predicate;
            }
        };
        Page<Mgroup> mgroups = mgroupRepository.findAll(specification, PageRequest.of(page > 0 ? page - 1 : page, 1, Sort.Direction.ASC, "name"));
        List<Long> allMgroups = new ArrayList<>();
        Long key = 0l;
        if (mgroups.getContent().size() > 0) {
            Mgroup m = mgroups.getContent().get(0);
            key = m.getId();
            boolean isAuth = true;

            while (isAuth && m.getMgroup() != null) {
                m = m.getMgroup();
                String mCode = m.getCode();
                isAuth = false;
                for (String code : userMgroupCode) {
                    if (mCode.startsWith(code)) {
                        isAuth = true;
                        allMgroups.add(m.getId());
                        break;
                    }
                }
            }
        }
        mgroup.put("key", key);
        mgroup.put("totalCount", mgroups.getTotalElements());
        mgroup.put("pageNo", page);
        mgroup.put("data", allMgroups);
        return mgroup;
    }

    @Override
    public HashMap<String, Object> mgroupById(Long userId, Long id) {
        HashMap<String, Object> mgroup = new HashMap<>();
        List<String> userMgroupCode = userService.userMgroupCodes(userId);
        Specification specification = new Specification<Mgroup>() {
            @Override
            public Predicate toPredicate(Root<Mgroup> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));

                Predicate[] userMgroupPredicates = new Predicate[userMgroupCode.size()];
                for (int i = 0; i < userMgroupCode.size(); i++) {
                    userMgroupPredicates[i] = cb.like(r.get("code"), userMgroupCode.get(i) + "%");
                }
                predicate.getExpressions().add(cb.or(userMgroupPredicates));
                predicate.getExpressions().add(cb.equal(r.get("id"), id));
                return predicate;
            }
        };
        Page<Mgroup> mgroups = mgroupRepository.findAll(specification, PageRequest.of(0, 1, Sort.Direction.ASC, "name"));
        List<Long> allMgroups = new ArrayList<>();
        Long key = 0l;
        if (mgroups.getContent().size() > 0) {
            Mgroup m = mgroups.getContent().get(0);
            key = m.getId();
            boolean isAuth = true;

            while (isAuth && m.getMgroup() != null) {
                m = m.getMgroup();
                String mCode = m.getCode();
                isAuth = false;
                for (String code : userMgroupCode) {
                    if (mCode.startsWith(code)) {
                        isAuth = true;
                        allMgroups.add(m.getId());
                        break;
                    }
                }
            }
        }
        mgroup.put("key", key);
        mgroup.put("data", allMgroups);
        return mgroup;
    }

    @Override
    public Result saveMgroup(Long userId, SaveMgroupRequest saveMgroupRequest) {
        Mgroup mgroup;
        if (saveMgroupRequest.getMgroupId() == null || saveMgroupRequest.getMgroupId().longValue() <= 0) {
            if (saveMgroupRequest.getParentId() == null || saveMgroupRequest.getParentId().longValue() <= 0) {
                return new ResultUtil().setErrorMsg("请选择上级媒体组");
            }
            Mgroup parent = mgroupRepository.findById(saveMgroupRequest.getParentId()).orElse(null);
            if (parent == null) {
                return new ResultUtil().setErrorMsg("请选择上级媒体组");
            }
            mgroup = new Mgroup();
            mgroup.setMgroup(parent);
            mgroup.setLev(parent.getLev() + 1);
            mgroup.setIsDelete((short) 0);
            String newCode = CommonUtil.generateNewCode(5, mgroupRepository.maxMgroupCode(parent.getId()));
            if (StrUtil.isEmpty(newCode)) {
                return new ResultUtil().setErrorMsg("超过最大值");
            }
            User user = new User();
            user.setId(userId);
            mgroup.setUser(user);
            mgroup.setCode(parent.getCode() + newCode);
        } else {
            mgroup = mgroupRepository.findById(saveMgroupRequest.getMgroupId()).orElse(null);
            if (mgroup == null) {
                return new ResultUtil().setErrorMsg("请选择正确媒体组");
            }
        }

        if (StrUtil.isEmpty(saveMgroupRequest.getName().trim())) {
            return new ResultUtil().setErrorMsg("名称不能为空");
        }
        mgroup.setName(saveMgroupRequest.getName());
        mgroup.setShare(saveMgroupRequest.getShare());
        mgroup.setRemark(saveMgroupRequest.getRemark());
        mgroupRepository.save(mgroup);

        return new ResultUtil<>().setData(mgroup.getMgroup().getId());
    }

    @Override
    public Result delMgroup(Long userId, Long mgroupId) {
        if (mgroupId == null) {
            return new ResultUtil().setErrorMsg("请选择正确媒体组");
        }
        if (mgroupId == 1) {
            return new ResultUtil().setErrorMsg("不能删除该媒体组");
        }
        Mgroup mgroup = mgroupRepository.findById(mgroupId).orElse(null);
        if (mgroup == null) {
            return new ResultUtil().setErrorMsg("请选择正确媒体组");
        }
        mgroup.setIsDelete((short) 1);
        mgroup.setDeleteTime(new Timestamp(System.currentTimeMillis()));
        mgroupRepository.save(mgroup);
        return new ResultUtil<>().setData(mgroup.getMgroup().getId());
    }


    @Override
    public Map<String, Object> findMgroupTree(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Mgroup> mgroups = mgroupRepository.findAllByUserIdAndIsDelete(userId, (short) 0);
            List<Tree<Mgroup>> trees = new ArrayList<>();
            buildTrees(trees, mgroups);
            Tree<Mgroup> deptTree = TreeUtil.build(trees);

            result.put("rows", deptTree);
            result.put("total", mgroups.size());
        } catch (Exception e) {
            log.error("获取媒体组列表失败", e);
            result.put("rows", null);
            result.put("total", 0);
        }
        return result;
    }

    private void buildTrees(List<Tree<Mgroup>> trees, List<Mgroup> mgroups) {
        mgroups.forEach(mgroup -> {
            Tree<Mgroup> tree = new Tree<>();
            tree.setId(mgroup.getId() + "");
            tree.setKey(tree.getId());
            if (mgroup.getMgroup() != null) {
                tree.setParentId(mgroup.getMgroup().getId() + "");
            } else {
                tree.setParentId(null);
            }
            tree.setText(mgroup.getName());
            tree.setCreateTime(mgroup.getCreateTime());
            tree.setModifyTime(mgroup.getUpdateTime());
            tree.setTitle(tree.getText());
            tree.setValue(tree.getId());
            trees.add(tree);
        });
    }

    @Cacheable(value = "mgroup_code", key = "#mgroupId")
    public String mgroupCode(Long mgroupId) {
        Mgroup mgroup = mgroupRepository.findById(mgroupId).orElse(null);
        if (mgroup != null) {
            return mgroup.getCode();
        }
        return "";
    }
}