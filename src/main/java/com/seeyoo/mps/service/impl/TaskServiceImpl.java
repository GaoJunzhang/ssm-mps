package com.seeyoo.mps.service.impl;

import com.seeyoo.mps.controller.request.AuditTaskRequest;
import com.seeyoo.mps.dao.TaskAuditRepository;
import com.seeyoo.mps.dao.TaskContentRepository;
import com.seeyoo.mps.dao.TaskMediaRepository;
import com.seeyoo.mps.dao.TaskRepository;
import com.seeyoo.mps.model.*;
import com.seeyoo.mps.service.MgroupService;
import com.seeyoo.mps.service.TaskService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.service.UserService;
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

import javax.persistence.criteria.*;
import java.util.*;

/**
 * 任务接口实现
 *
 * @author Wangj
 */
@Slf4j
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAuditRepository taskAuditRepository;

    @Autowired
    private TaskContentRepository taskContentRepository;

    @Autowired
    private TaskMediaRepository taskMediaRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MgroupService mgroupService;

    @Override
    public TaskRepository getRepository() {
        return taskRepository;
    }

    @Override
    public Map<String, Object> taskList(Long userId, Long mgroupId, String name, Short audit, Integer page, Integer size, String sortOrder, String sortValue) {
        List<String> userMgroupCode = userService.userMgroupCodes(userId);
        final String mgroupCode = (mgroupId == null) ? "" : mgroupService.mgroupCode(mgroupId);
        Specification specification = new Specification<Task>() {
            @Override
            public Predicate toPredicate(Root<Task> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));

                if (StrUtil.isEmpty(mgroupCode)) {
                    Predicate[] userMgroupPredicates = new Predicate[userMgroupCode.size()];
                    for (int i = 0; i < userMgroupCode.size(); i++) {
                        userMgroupPredicates[i] = cb.like(r.get("mgroup").get("code"), userMgroupCode.get(i) + "%");
                    }
                    predicate.getExpressions().add(cb.or(userMgroupPredicates));
                } else {
                    predicate.getExpressions().add(cb.like(r.get("mgroup").get("code"), mgroupCode + "%"));
                }


                if (!StrUtil.isEmpty(name)) {
                    predicate.getExpressions().add(cb.like(r.get("name"), "%" + name + "%"));
                }
                if (audit != null) {
                    predicate.getExpressions().add(cb.equal(r.get("audit"), audit));
                }
                return predicate;
            }
        };
        Page<Task> tasks = taskRepository.findAll(specification, PageRequest.of(page > 0 ? page - 1 : page, size, sortOrder.equals("descend") ? Sort.Direction.DESC : Sort.Direction.ASC, sortValue));
        List<Map<String, Object>> tasksMap = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", task.getId());
            u.put("name", task.getName());
            u.put("audit", task.getAudit().ordinal());
            u.put("start", task.getStart() == null ? "" : DateUtil.format(task.getStart(), "yyyy-MM-dd HH:mm:ss"));
            u.put("end", task.getEnd() == null ? "" : DateUtil.format(task.getEnd(), "yyyy-MM-dd HH:mm:ss"));
            u.put("userName", task.getUser().getName());
            u.put("createTime", DateUtil.format(task.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            tasksMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", page);
        map.put("totalCount", tasks.getTotalElements());
        map.put("data", tasksMap);
        return map;
    }

    @Override
    @Transactional
    public Result delTask(Long[] taskIds) {
        if (taskIds == null) {
            return new ResultUtil().setErrorMsg("请选择任务");
        }
        List<Task> tasks = new ArrayList<>();
        for (Long id : taskIds) {
            Task task = taskRepository.findById(id).orElse(null);
            if (task == null) {
                return new ResultUtil().setErrorMsg("请选择正确任务");
            }

            task.setIsDelete((short) 1);
        }
        return new ResultUtil<>().setData("");
    }

    @Override
    @Transactional
    public Result auditTask(Long userId, AuditTaskRequest auditTaskRequest) {
        if (auditTaskRequest == null || auditTaskRequest.getIds() == null) {
            return new ResultUtil().setErrorMsg("无效的参数");
        }
        Long[] ids = auditTaskRequest.getIds();
        User user = new User();
        user.setId(userId);
        for (Long id : ids) {
            Task task = taskRepository.findById(id).orElse(null);
            if (id == null)
                continue;

            TaskAuditEnum audit = auditTaskRequest.getAudit().intValue() == 1 ? TaskAuditEnum.AUDIT_OK : TaskAuditEnum.AUDIT_FAILED;
            if (task.getAudit() == audit)
                continue;

            TaskAudit taskAudit = new TaskAudit();
            taskAudit.setUser(user);
            taskAudit.setTask(task);
            taskAudit.setRemark(auditTaskRequest.getRemark());
            taskAudit.setAudit(audit);
            taskAuditRepository.save(taskAudit);

            task.setAudit(audit);
            taskRepository.save(task);
        }

        return new ResultUtil<>().setData("");
    }

    @Override
    public Result auditTaskRecords(Long id) {
        if (id == null) {
            return new ResultUtil().setErrorMsg("无效的参数");
        }
        List<TaskAudit> taskAudits = taskAuditRepository.findAllByTaskIdOrderByCreateTimeDesc(id);
        List<HashMap<String, Object>> records = new ArrayList<>();
        for (TaskAudit taskAudit : taskAudits) {
            HashMap<String, Object> record = new HashMap<>();
            record.put("audit", taskAudit.getAudit().ordinal());
            record.put("remark", StrUtil.nullToEmpty(taskAudit.getRemark()));
            record.put("userName", taskAudit.getUser().getName());
            record.put("createTime", DateUtil.format(taskAudit.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            records.add(record);
        }
        return new ResultUtil<>().setData(records);
    }

    @Transactional
    @Override
    public Result saveTask(Long userId, String taskContent) {
        JSONObject taskContentObj = new JSONObject(taskContent);
        Long taskId = taskContentObj.optLong("taskId");
        String taskName = taskContentObj.optString("name");
        Long mgroupId = taskContentObj.optLong("mgroupId", 0);
        JSONObject screen = taskContentObj.optJSONObject("screen");
        JSONArray content = taskContentObj.optJSONArray("content");
        String start = taskContentObj.optString("start");
        String end = taskContentObj.optString("end");

        if (screen == null) {
            screen = new JSONObject("{}");
        }
        int width = screen.optInt("w", 1920);
        int height = screen.optInt("h", 1080);
        if (StrUtil.isEmpty(taskName)) {
            return new ResultUtil<>().setErrorMsg("请输入名称");
        }

        if (mgroupId <= 0) {
            return new ResultUtil<>().setErrorMsg("请选择媒体组");
        }

        if (content.toString().length() <= 0) {
            return new ResultUtil<>().setErrorMsg("任务内容错误");
        }

        if (content.length() >= 60000) {
            return new ResultUtil<>().setErrorMsg("任务内容过多");
        }
        Task task;
        if (taskId > 0) {
            task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                return new ResultUtil<>().setErrorMsg("无效的任务");
            }
        } else {
            task = new Task();
        }
        task.setName(taskName);
        task.setAudit(TaskAuditEnum.AUDIT_WAIT);
        task.setIsDelete((short) 0);
        task.setWidth(width);
        task.setHeight(height);
        Mgroup mgroup = new Mgroup();
        mgroup.setId(mgroupId);
        task.setMgroup(mgroup);
        User user = new User();
        user.setId(userId);
        task.setUser(user);
        task.setType(TaskTypeEnum.DEFAULT);
        task.setStart(StrUtil.isEmpty(start) ? null : DateUtil.parse(start, "yyyy-MM-dd HH:mm:ss").toTimestamp());
        task.setEnd(StrUtil.isEmpty(end) ? null : DateUtil.parse(end, "yyyy-MM-dd HH:mm:ss").toTimestamp());
        taskRepository.save(task);

        TaskContent tc = new TaskContent();
        tc.setTaskId(task.getId());
        tc.setContent(content.toString());
        taskContentRepository.save(tc);

        List<TaskMedia> taskMedias = new ArrayList<>();
        for (int i = 0; i < content.length(); i++) {
            JSONObject section = content.optJSONObject(i);
            JSONArray templates = section.optJSONArray("t");
            for (int j = 0; j < templates.length(); j++) {
                JSONObject template = templates.optJSONObject(j);
                Long bgImgId = template.optJSONObject("b").optLong("id", 0);
                if (bgImgId > 0) {
                    Media media = new Media();
                    media.setId(bgImgId);
                    taskMedias.add(new TaskMedia(null, task, media, null, null));
                }

                JSONArray areas = template.optJSONArray("a");
                for (int k = 0; k < areas.length(); k++) {
                    JSONObject area = areas.optJSONObject(k);
                    String type = area.optString("type", "unknown");
                    switch (type) {
                        case "Mix":
                        case "Pic":
                        case "Txt":
                            JSONArray playlist = area.optJSONObject("prop").optJSONArray("playlist");
                            for (int l = 0; l < playlist.length(); l++) {
                                JSONObject item = playlist.optJSONObject(l);
                                String st = item.optString("start");
                                String et = item.optString("end");
                                Media media = new Media();
                                media.setId(item.optLong("id"));
                                taskMedias.add(new TaskMedia(null, task, media,
                                        StrUtil.isEmpty(st) ? null : DateUtil.parse(st, "yyyy-MM-dd HH:mm:ss").toTimestamp(),
                                        StrUtil.isEmpty(et) ? null : DateUtil.parse(et, "yyyy-MM-dd HH:mm:ss").toTimestamp())
                                );
                            }
                            break;
                        case "Logo":
                            break;
                    }
                }
            }
        }
        taskMediaRepository.deleteByTaskId(task.getId());
        taskMediaRepository.saveAll(taskMedias);

        TaskAudit taskAudit = new TaskAudit();
        taskAudit.setUser(user);
        taskAudit.setTask(task);
        taskAudit.setRemark("");
        taskAudit.setAudit(TaskAuditEnum.AUDIT_WAIT);
        taskAuditRepository.save(taskAudit);
        return new ResultUtil<>().setData("");
    }

    @Override
    public Result task(Long userId, Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return new ResultUtil().setErrorMsg("无效的任务");
        }
        HashMap<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("id", task.getId());
        taskInfo.put("name", task.getName());
        taskInfo.put("mgroupId", task.getMgroup().getId());
        taskInfo.put("start", task.getStart() == null ? "" : DateUtil.format(task.getStart(), "yyyy-MM-dd HH:mm:ss"));
        taskInfo.put("end", task.getEnd() == null ? "" : DateUtil.format(task.getEnd(), "yyyy-MM-dd HH:mm:ss"));

        HashMap<String, Object> screen = new HashMap<>();
        screen.put("w", task.getStart());
        screen.put("h", task.getEnd());
        taskInfo.put("screen", screen);

        TaskContent taskContent = taskContentRepository.findById(id).orElse(null);
        if (taskContent != null) {
            taskInfo.put("content", taskContent.getContent());
        }

        return new ResultUtil<>().setData(taskInfo);
    }
}