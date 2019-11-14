package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.seeyoo.mps.dao.OperationLogRepository;
import com.seeyoo.mps.model.OperationLog;
import com.seeyoo.mps.service.OperationLogService;
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
import java.util.*;

/**
 * 操作日志接口实现
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Override
    public OperationLogRepository getRepository() {
        return operationLogRepository;
    }

    @Override
    public Map<String, Object> logList(String userName,String action, SearchVo searchVo, Pageable pageable) {

        Page<OperationLog> operationLogs = operationLogRepository.findAll(new Specification<OperationLog>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<OperationLog> r, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                if (!StrUtil.isEmpty(userName)) {
                    predicate.getExpressions().add(cb.like(r.get("user").get("name"), "%" + userName + "%"));
                }
                if (StrUtil.isNotEmpty(action)){
                    predicate.getExpressions().add(cb.like(r.get("action"),"%"+action+"%"));
                }
                if (StrUtil.isNotEmpty(searchVo.getStartDate()) && StrUtil.isNotEmpty(searchVo.getEndDate())){
                    predicate.getExpressions().add(cb.between(r.get("createTime"), DateUtil.parse(searchVo.getStartDate()),DateUtil.parse(searchVo.getEndDate())));
                }
                return predicate;
            }
        }, pageable);
        List<Map<String, Object>> logMap = new ArrayList<>();
        for (OperationLog obj : operationLogs) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", obj.getId());
            u.put("userName", obj.getUser().getName());
            u.put("action", obj.getAction());
            u.put("status", obj.getStatus());
            u.put("request", JSONUtil.parseArray(obj.getRequest()));
            u.put("response", JSONUtil.parse(obj.getResponse()));
            u.put("createTime", DateUtil.format(obj.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            u.put("updateTime", DateUtil.format(obj.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            logMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("totalCount", operationLogs.getTotalElements());
        map.put("data", logMap);
        return map;
    }
}