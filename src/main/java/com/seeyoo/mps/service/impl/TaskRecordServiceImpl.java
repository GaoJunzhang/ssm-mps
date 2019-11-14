package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.bean.SimpleTerminalBean;
import com.seeyoo.mps.controller.request.AuditTaskRequest;
import com.seeyoo.mps.dao.*;
import com.seeyoo.mps.model.*;
import com.seeyoo.mps.service.MgroupService;
import com.seeyoo.mps.service.TaskRecordService;
import com.seeyoo.mps.service.TaskService;
import com.seeyoo.mps.service.UserService;
import com.seeyoo.mps.tool.OSSClientUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
 * 任务接口实现
 *
 * @author Wangj
 */
@Slf4j
@Service
@Transactional
public class TaskRecordServiceImpl implements TaskRecordService {

    @Autowired
    private TaskRecordRepository taskRecordRepository;

    @Autowired
    private TaskMediaRepository taskMediaRepository;

    @Autowired
    private TaskRecordMediaRepository taskRecordMediaRepository;

    @Autowired
    private TaskRecordTerminalRepository taskRecordTerminalRepository;

    @Autowired
    private UserService userService;

    @Override
    public TaskRecordRepository getRepository() {
        return taskRecordRepository;
    }

    @Override
    public Map<String, Object> taskRecordList(Long userId, String userCode, String name, String userName, Short type, Integer page, Integer size, String sortOrder, String sortValue) {
        Specification specification = new Specification<TaskRecord>() {
            @Override
            public Predicate toPredicate(Root<TaskRecord> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.like(r.get("user").get("code"), userCode + "%"));
                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                if (!StrUtil.isEmpty(userName)) {
                    predicate.getExpressions().add(cb.like(r.get("user").get("name"), "%" + userName + "%"));
                }
                if (type != null) {
                    predicate.getExpressions().add(cb.equal(r.get("type"), type));
                }
                return predicate;
            }
        };
        Page<TaskRecord> taskRecords = taskRecordRepository.findAll(specification, PageRequest.of(page > 0 ? page - 1 : page, size, sortOrder.equals("descend") ? Sort.Direction.DESC : Sort.Direction.ASC, sortValue));
        List<Map<String, Object>> taskRecordsMap = new ArrayList<>();
        for (TaskRecord taskRecord : taskRecords) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", taskRecord.getId());
            u.put("name", taskRecord.getName());
            u.put("type", taskRecord.getType().ordinal());
            u.put("start", taskRecord.getStart() == null ? "" : DateUtil.format(taskRecord.getStart(), "yyyy-MM-dd HH:mm:ss"));
            u.put("end", taskRecord.getEnd() == null ? "" : DateUtil.format(taskRecord.getEnd(), "yyyy-MM-dd HH:mm:ss"));
            u.put("userName", taskRecord.getUser().getName());
            u.put("createTime", DateUtil.format(taskRecord.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            u.put("terminalCnt", taskRecord.getTerminalCnt());
            taskRecordsMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", page);
        map.put("totalCount", taskRecords.getTotalElements());
        map.put("data", taskRecordsMap);
        return map;
    }

    @Override
    public Map<String, Object> taskRecordTerminals(Long userId, Long taskRecordId, String name, Integer page, Integer size, String sortOrder, String sortValue) {
        Specification specification = new Specification<TaskRecordTerminal>() {
            @Override
            public Predicate toPredicate(Root<TaskRecordTerminal> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                if (taskRecordId != null) {
                    predicate.getExpressions().add(cb.equal(r.get("taskRecord").get("id"), taskRecordId));
                }
                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("terminal").get("name"), "%" + name + "%"));
                }
                return predicate;
            }
        };
        Page<TaskRecordTerminal> taskRecordTerminals = taskRecordTerminalRepository.findAll(specification, PageRequest.of(page > 0 ? page - 1 : page, size, sortOrder.equals("descend") ? Sort.Direction.DESC : Sort.Direction.ASC, sortValue));
        List<Map<String, Object>> taskRecordTerminalsMap = new ArrayList<>();
        for (TaskRecordTerminal taskRecordTerminal : taskRecordTerminals) {
            Map<String, Object> u = new HashMap<>();
            Terminal terminal = taskRecordTerminal.getTerminal();
            u.put("name", terminal.getName());
            u.put("tgroupName", terminal.getTgroup().getName());
            u.put("mac", terminal.getMac());
            u.put("status", taskRecordTerminal.getStatus());
            taskRecordTerminalsMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", page);
        map.put("totalCount", taskRecordTerminals.getTotalElements());
        map.put("data", taskRecordTerminalsMap);
        return map;
    }

    @Override
    public Map<String, Object> taskRecordMedias(Long userId, Long taskRecordId, String name) {
        List<TaskRecordMedia> taskRecordMedias = taskRecordMediaRepository.findAllByTaskRecordId(taskRecordId);
        List<Map<String, Object>> taskRecordMediasMap = new ArrayList<>();
        for (TaskRecordMedia taskRecordMedia : taskRecordMedias) {
            Map<String, Object> u = new HashMap<>();
            Media media = taskRecordMedia.getMedia();
            u.put("id", media.getId());
            u.put("type", media.getType());
            u.put("name", media.getName());
            u.put("path", OSSClientUtil.getAccessUrl() + "/" + media.getPath());
            u.put("localPath", media.getLocalPath());
            u.put("userName", media.getUser().getName());
            taskRecordMediasMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("data", taskRecordMediasMap);
        return map;
    }

    @Override
    @Transactional
    public void saveTaskRecord(Long userId, TaskRecordEnum type, Long id, String name, Timestamp start, Timestamp end, List<SimpleTerminalBean> terminals) {
        TaskRecord taskRecord = new TaskRecord();
        taskRecord.setName(name);
        taskRecord.setType(type);
        taskRecord.setStart(start);
        taskRecord.setEnd(end);
        taskRecord.setTerminalCnt(terminals.size());
        User user = new User();
        user.setId(userId);
        taskRecord.setUser(user);
        taskRecordRepository.save(taskRecord);

        List<TaskMedia> taskMedias = taskMediaRepository.findAllByTaskId(id);
        List<TaskRecordMedia> taskRecordMedias = new ArrayList<>();
        for (TaskMedia taskMedia : taskMedias) {
            TaskRecordMedia taskRecordMedia = new TaskRecordMedia();
            taskRecordMedia.setMedia(taskMedia.getMedia());
            taskRecordMedia.setTaskRecord(taskRecord);
            taskRecordMedias.add(taskRecordMedia);
        }
        taskRecordMediaRepository.saveAll(taskRecordMedias);

        List<TaskRecordTerminal> taskRecordTerminals = new ArrayList<>();
        for (SimpleTerminalBean simpleTerminalBean : terminals) {
            TaskRecordTerminal taskRecordTerminal = new TaskRecordTerminal();
            Terminal terminal = new Terminal();
            terminal.setId(simpleTerminalBean.getId());
            taskRecordTerminal.setTerminal(terminal);
            taskRecordTerminal.setTaskRecord(taskRecord);
            taskRecordTerminal.setStatus((short) 0);
            taskRecordTerminals.add(taskRecordTerminal);
        }
        taskRecordTerminalRepository.saveAll(taskRecordTerminals);
    }


}