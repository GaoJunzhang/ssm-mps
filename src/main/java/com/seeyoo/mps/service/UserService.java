package com.seeyoo.mps.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.controller.request.ChangePasswordRequest;
import com.seeyoo.mps.controller.request.SaveUserRequest;
import com.seeyoo.mps.dao.UserMgroupRepository;
import com.seeyoo.mps.dao.UserRepository;
import com.seeyoo.mps.dao.UserRoleRepository;
import com.seeyoo.mps.dao.UserTgroupRepository;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.*;
import com.seeyoo.mps.tool.CommonUtil;
import com.seeyoo.mps.tool.MD5Util;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

@Service
@Slf4j
public class UserService implements BaseService<User, Long> {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserMgroupRepository userMgroupRepository;

    @Autowired
    private UserTgroupRepository userTgroupRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }

    public Map<String, Object> userList(long userId, String userCode, String name, String account, Short status, Integer page, Integer size, String sortOrder, String sortValue) {
        Sort sort = new Sort(sortOrder.equals("descend") ? Sort.Direction.DESC : Sort.Direction.ASC, sortValue);
        Pageable pageable = new PageRequest(page > 0 ? page - 1 : page, size, sort);

        Specification specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.notEqual(r.get("id"), userId));
                predicate.getExpressions().add(cb.like(r.get("code"), userCode + "%"));
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                if (!StrUtil.isEmpty(account)) {
                    predicate.getExpressions().add(cb.like(r.get("account"), "%" + account + "%"));
                }

                if (status != null) {
                    predicate.getExpressions().add(cb.equal(r.get("status"), status));
                }
                return predicate;
            }
        };

        Page<User> users = userRepository.findAll(specification, pageable);
        List<Map<String, Object>> usersMap = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", user.getId());
            u.put("name", user.getName());
            u.put("account", user.getAccount());
            u.put("status", user.getStatus().ordinal());
            Role role = userRoleService.userRole(user.getId());
            u.put("roleName", role == null ? "" : role.getName());
            u.put("loginTime", user.getLoginTime() == null ? "" : DateUtil.format(user.getLoginTime(), "yyyy-MM-dd HH:mm:ss"));
            u.put("createTime", DateUtil.format(user.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            u.put("avatar", user.getAvatar());
            usersMap.add(u);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", page);
        map.put("totalCount", users.getTotalElements());
        map.put("data", usersMap);
        return map;
    }

    @Cacheable(value = "user_mgroup", key = "#userId")
    public List<String> userMgroupCodes(Long userId) {
        List<UserMgroup> userMgroups = userMgroupRepository.userMgroupsByUserAndName(userId);
        List<String> userMgroupCode = new ArrayList<>();
        for (UserMgroup userMgroup : userMgroups) {
            userMgroupCode.add(userMgroup.getMgroup().getCode());
        }

        return userMgroupCode;
    }

    @Cacheable(value = "user_tgroup", key = "#userId")
    public List<String> userTgroupCodes(Long userId) {
        List<UserTgroup> userTgroups = userTgroupRepository.userTgroupsByUserAndName(userId);
        List<String> userTgroupCode = new ArrayList<>();
        for (UserTgroup userTgroup : userTgroups) {
            userTgroupCode.add(userTgroup.getTgroup().getCode());
        }

        return userTgroupCode;
    }

    @Transactional
    public Result saveUser(Long userId, SaveUserRequest saveUserRequest) {
        User parentUser = userRepository.findById(userId).orElse(null);
        if (parentUser == null) {
            return new ResultUtil<>().setErrorMsg("无效的请求");
        }
        String account = saveUserRequest.getAccount();
        if (StrUtil.isEmpty(account)) {
            return new ResultUtil<>().setErrorMsg("无效的账号");
        }
        List<User> users = userRepository.findAllByAccountAndIsDelete(account, (short) 0);
        if (users != null) {
            for (User user : users) {
                if (saveUserRequest.getUserId() == null)
                    return new ResultUtil<>().setErrorMsg("账号已存在");
                if (user.getId().longValue() != saveUserRequest.getUserId()) {
                    return new ResultUtil<>().setErrorMsg("账号已存在");
                }
            }
        }
        User user;
        if (saveUserRequest.getUserId() != null && saveUserRequest.getUserId().longValue() > 0) {
            user = userRepository.findById(saveUserRequest.getUserId()).orElse(null);
            if (user == null) {
                return new ResultUtil<>().setErrorMsg("无效的用户");
            }
        } else {
            user = new User();
            user.setSalt(CommonUtil.getRandomString(32).toLowerCase());
            SimpleHash hash = new SimpleHash("md5", MD5Util.getMD5String("123456").toLowerCase(),
                    user.getSalt(), 2);
            user.setPassword(hash.toHex());
            user.setCode(parentUser.getCode() + CommonUtil.generateNewCode(4, userRepository.maxUserCode(userId)));
        }
        user.setName(saveUserRequest.getName());
        user.setAccount(saveUserRequest.getAccount());
        user.setType(UserEnum.DEFAULT);
        user.setStatus(saveUserRequest.getStatus().intValue() == 1 ? UserStatusEnum.ENABLE : UserStatusEnum.DISABLE);
        user.setValidStartDate(saveUserRequest.getStart() == null ? null : DateUtil.parse(saveUserRequest.getStart(), "yyyy-MM-dd").toSqlDate());
        user.setValidEndDate(saveUserRequest.getEnd() == null ? null : DateUtil.parse(saveUserRequest.getEnd(), "yyyy-MM-dd").toSqlDate());
        user.setIsDelete((short) 0);
        user.setAvatar(saveUserRequest.getAvatar());
        user.setUser(parentUser);
        userRepository.save(user);

        userRoleRepository.deleteByUserId(user.getId());
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        Role role = new Role();
        role.setId(saveUserRequest.getRoleId());
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        userMgroupRepository.deleteByUserId(user.getId());
        if (saveUserRequest.getMgroupIds() != null) {
            Long[] mgroups = saveUserRequest.getMgroupIds();
            for (Long id : mgroups) {
                UserMgroup userMgroup = new UserMgroup();
                Mgroup mgroup = new Mgroup();
                mgroup.setId(id);
                userMgroup.setMgroup(mgroup);
                userMgroup.setUser(user);
                userMgroupRepository.save(userMgroup);
            }
        }

        userTgroupRepository.deleteByUserId(user.getId());
        if (saveUserRequest.getTgroupIds() != null) {
            Long[] tgroups = saveUserRequest.getTgroupIds();
            for (Long id : tgroups) {
                UserTgroup userTgroup = new UserTgroup();
                Tgroup tgroup = new Tgroup();
                tgroup.setId(id);
                userTgroup.setTgroup(tgroup);
                userTgroup.setUser(user);
                userTgroupRepository.save(userTgroup);
            }
        }

        return new ResultUtil<>().setData("");
    }

    @Transactional
    public Result resetPassword(Long[] userIds) {
        if (userIds == null) {
            return new ResultUtil().setErrorMsg("请选择用户");
        }
        List<User> users = new ArrayList<>();
        for (Long id : userIds) {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return new ResultUtil().setErrorMsg("请选择正确用户");
            }

            SimpleHash hash = new SimpleHash("md5", MD5Util.getMD5String("123456").toLowerCase(),
                    user.getSalt(), 2);
            user.setPassword(hash.toHex());
        }
        return new ResultUtil<>().setData("");
    }

    @Transactional
    public Result delUser(Long[] userIds) {
        if (userIds == null) {
            return new ResultUtil().setErrorMsg("请选择用户");
        }
        List<User> users = new ArrayList<>();
        for (Long id : userIds) {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return new ResultUtil().setErrorMsg("请选择正确用户");
            }

            user.setIsDelete((short) 1);
//            users.add(user);
        }
//        userRepository.saveAll(users);
        return new ResultUtil<>().setData("");
    }

    @Transactional
    public Result user(Long userId, Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResultUtil().setErrorMsg("无效的用户");
        }
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("account", user.getAccount());
        userInfo.put("start", user.getValidStartDate() == null ? "" : DateUtil.format(user.getValidStartDate(), "yyyy-MM-dd"));
        userInfo.put("end", user.getValidEndDate() == null ? "" : DateUtil.format(user.getValidEndDate(), "yyyy-MM-dd"));
        userInfo.put("status", user.getStatus().ordinal());
        userInfo.put("avatar", user.getAvatar());
        List<UserRole> userRole = userRoleRepository.findAllByUserId(id);
        if (userRole.size() > 0) {
            userInfo.put("roleId", userRole.get(0).getRole().getId());
            userInfo.put("roleName", userRole.get(0).getRole().getName());
        }

        List<HashMap<String, Object>> mgroups = new ArrayList<>();
        List<UserMgroup> userMgroups = userMgroupRepository.userMgroupsByUserAndName(user.getId());
        for (UserMgroup userMgroup : userMgroups) {
            HashMap<String, Object> mgroup = new HashMap<>();
            mgroup.put("value", userMgroup.getMgroup().getId());
            mgroup.put("label", userMgroup.getMgroup().getName());
            mgroups.add(mgroup);
        }
        userInfo.put("mgroup", mgroups);

        List<HashMap<String, Object>> tgroups = new ArrayList<>();
        List<UserTgroup> userTgroups = userTgroupRepository.userTgroupsByUserAndName(user.getId());
        for (UserTgroup userTgroup : userTgroups) {
            HashMap<String, Object> tgroup = new HashMap<>();
            tgroup.put("value", userTgroup.getTgroup().getId());
            tgroup.put("label", userTgroup.getTgroup().getName());
            tgroups.add(tgroup);
        }
        userInfo.put("tgroup", tgroups);

        return new ResultUtil<>().setData(userInfo);
    }

    @Transactional
    public Result changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ResultUtil().setErrorMsg("无效的用户");
        }
        if (StrUtil.isEmpty(request.getOldPassword()) || StrUtil.isEmpty(request.getNewPassword())) {
            return new ResultUtil().setErrorMsg("无效的密码");
        }
        log.info("{},{}", request.getOldPassword(), request.getNewPassword());
        SimpleHash hash = new SimpleHash("md5", request.getOldPassword().toLowerCase(),
                user.getSalt(), 2);
        log.info("{},{}", hash.toHex(), user.getPassword());
        if (hash.toHex().equals(user.getPassword())) {
            SimpleHash newHash = new SimpleHash("md5", request.getNewPassword().toLowerCase(),
                    user.getSalt(), 2);
            user.setPassword(newHash.toHex());
            return new ResultUtil<>().setData("");
        }
        return new ResultUtil().setErrorMsg("原密码错误");
    }
}
