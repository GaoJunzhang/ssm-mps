package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Joiner;
import com.seeyoo.mps.bean.ProjectBean;
import com.seeyoo.mps.controller.request.AuditProjectRequest;
import com.seeyoo.mps.dao.*;
import com.seeyoo.mps.integration.mq.RabbitMqManager;
import com.seeyoo.mps.model.*;
import com.seeyoo.mps.service.*;
import com.seeyoo.mps.tool.OSSClientUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
import java.io.InputStream;
import java.util.*;

/**
 * 方案接口实现
 *
 * @author Wangj
 */
@Slf4j
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAuditRepository projectAuditRepository;

    @Autowired
    private ProjectMediaRepository projectMediaRepository;

    @Autowired
    private ProjectTerminalRepository projectTerminalRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MgroupService mgroupService;

    @Autowired
    private TgroupService tgroupService;

    @Autowired
    private RabbitMqManager rabbitMqManager;

    @Override
    public ProjectRepository getRepository() {
        return projectRepository;
    }

    @Override
    public Map<String, Object> projectList(Long userId, Long mgroupId, String name, Short audit, String contractNo, String clientName, String salerName, Integer page, Integer size, String sortOrder, String sortValue) {
        List<String> userMgroupCode = userService.userMgroupCodes(userId);
        final String mgroupCode = (mgroupId == null) ? "" : mgroupService.mgroupCode(mgroupId);
        Specification specification = new Specification<Project>() {
            @Override
            public Predicate toPredicate(Root<Project> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));

                if (mgroupId == null) {
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
                if (!StrUtil.isEmpty(clientName)) {
                    predicate.getExpressions().add(cb.like(r.get("client").get("name"), "%" + clientName + "%"));
                }
                if (!StrUtil.isEmpty(salerName)) {
                    predicate.getExpressions().add(cb.like(r.get("client").get("saler").get("name"), "%" + salerName + "%"));
                }
                if (!StrUtil.isEmpty(contractNo)) {
                    predicate.getExpressions().add(cb.like(r.get("contractNo"), "%" + contractNo + "%"));
                }
                return predicate;
            }
        };
        Page<Project> projects = projectRepository.findAll(specification, PageRequest.of(page > 0 ? page - 1 : page, size, sortOrder.equals("descend") ? Sort.Direction.DESC : Sort.Direction.ASC, sortValue));
        List<Map<String, Object>> projectsMap = new ArrayList<>();
        for (Project project : projects) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", project.getId());
            u.put("name", project.getName());
            u.put("audit", project.getAudit().ordinal());
            u.put("start", project.getValidStart() == null ? "" : DateUtil.format(project.getValidStart(), "yyyy-MM-dd"));
            u.put("end", project.getValidEnd() == null ? "" : DateUtil.format(project.getValidEnd(), "yyyy-MM-dd"));
            String cName = "";
            String sName = "";
            Client client = project.getClient();
            if (client != null) {
                cName = client.getName();
                Saler saler = client.getSaler();
                if (saler != null) {
                    sName = saler.getName();
                }
            }
            u.put("clientName", cName);
            u.put("salerName", sName);
            u.put("userName", project.getUser().getName());
            u.put("createTime", DateUtil.format(project.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            projectsMap.add(u);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", page);
        map.put("totalCount", projects.getTotalElements());
        map.put("data", projectsMap);
        return map;
    }

    @Override
    @Transactional
    public Result delProject(Long[] projectIds) {
        if (projectIds == null) {
            return new ResultUtil().setErrorMsg("请选择方案");
        }
        List<Project> projects = new ArrayList<>();
        for (Long id : projectIds) {
            Project project = projectRepository.findById(id).orElse(null);
            if (project == null) {
                return new ResultUtil().setErrorMsg("请选择正确方案");
            }

            project.setIsDelete((short) 1);
//            projects.add(project);
        }
//        projectRepository.saveAll(projects);

        for (Long id : projectIds) {
            projectTerminalReload(id);
        }
        return new ResultUtil<>().setData("");
    }

    @Override
    @Transactional
    public Result auditProject(Long userId, AuditProjectRequest auditProjectRequest) {
        if (auditProjectRequest == null || auditProjectRequest.getIds() == null) {
            return new ResultUtil().setErrorMsg("无效的参数");
        }
        Long[] ids = auditProjectRequest.getIds();
        User user = new User();
        user.setId(userId);
        for (Long id : ids) {
            Project project = projectRepository.findById(id).orElse(null);
            if (id == null)
                continue;

            ProjectAuditEnum audit = ProjectAuditEnum.AUDIT_FAILED;
            if (auditProjectRequest.getAudit().intValue() == 1) {
                audit = ProjectAuditEnum.AUDIT_OK;
            } else if (auditProjectRequest.getAudit().intValue() == 2) {
                audit = ProjectAuditEnum.AUDIT_CANCEL;
            }

            ProjectAuditEnum lastAudit = project.getAudit();
            if (lastAudit == audit)
                continue;

            ProjectAudit projectAudit = new ProjectAudit();
            projectAudit.setUser(user);
            projectAudit.setProject(project);
            projectAudit.setRemark(auditProjectRequest.getRemark());
            projectAudit.setAudit(audit);
            projectAuditRepository.save(projectAudit);

            project.setAudit(audit);
            projectRepository.save(project);

            if (lastAudit == ProjectAuditEnum.AUDIT_OK || audit == ProjectAuditEnum.AUDIT_OK) {
                projectTerminalReload(id);
            }
        }

        return new ResultUtil<>().setData("");
    }

    @Override
    public Result auditProjectRecords(Long id) {
        if (id == null) {
            return new ResultUtil().setErrorMsg("无效的参数");
        }
        List<ProjectAudit> projectAudits = projectAuditRepository.findAllByProjectIdOrderByCreateTimeDesc(id);
        List<HashMap<String, Object>> records = new ArrayList<>();
        for (ProjectAudit projectAudit : projectAudits) {
            HashMap<String, Object> record = new HashMap<>();
            record.put("audit", projectAudit.getAudit().ordinal());
            record.put("remark", StrUtil.nullToEmpty(projectAudit.getRemark()));
            record.put("userName", projectAudit.getUser().getName());
            record.put("createTime", DateUtil.format(projectAudit.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            records.add(record);
        }
        return new ResultUtil<>().setData(records);
    }

    @Transactional
    @Override
    public Result saveProject(Long userId, String projectContent) {
        JSONObject projectContentObj = new JSONObject(projectContent);
        Long projectId = projectContentObj.optLong("projectId", 0);
        String name = projectContentObj.optString("name");
        String contractNo = projectContentObj.optString("contractNo");
        Integer adCount = projectContentObj.optInt("adCount", 1);
        Long mgroupId = projectContentObj.optLong("mgroupId", 0);
        Long clientId = projectContentObj.optLong("clientId", 0);
        String start = projectContentObj.optString("start");
        String end = projectContentObj.optString("end");
        JSONArray terminals = projectContentObj.optJSONArray("terminals");
        JSONArray medias = projectContentObj.optJSONArray("medias");
        if (StrUtil.isEmpty(name)) {
            return new ResultUtil<>().setErrorMsg("请输入名称");
        }

        if (mgroupId <= 0) {
            return new ResultUtil<>().setErrorMsg("请选择媒体组");
        }

        if (clientId <= 0) {
            return new ResultUtil<>().setErrorMsg("请选择广告主");
        }
        if (StrUtil.isEmpty(start) || StrUtil.isEmpty(end)) {
            return new ResultUtil<>().setErrorMsg("请选择有效期");
        }
        if (medias == null || medias.length() <= 0) {
            return new ResultUtil<>().setErrorMsg("请添加方案素材");
        }
        if (terminals == null || terminals.length() <= 0) {
            return new ResultUtil<>().setErrorMsg("请添加投放终端");
        }
        Project project;
        ProjectAuditEnum lastAudit = ProjectAuditEnum.AUDIT_WAIT;
        if (projectId > 0) {
            project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return new ResultUtil<>().setErrorMsg("无效的方案");
            }
            lastAudit = project.getAudit();
            projectMediaRepository.deleteByProjectId(projectId);
            projectTerminalRepository.deleteByProjectId(projectId);
        } else {
            project = new Project();
        }
        Mgroup mgroup = new Mgroup();
        mgroup.setId(mgroupId);
        project.setMgroup(mgroup);
        Client client = new Client();
        client.setId(clientId);
        project.setClient(client);
        project.setName(name);
        project.setContractNo(contractNo);
        project.setAdCount(adCount);
        project.setAudit(ProjectAuditEnum.AUDIT_WAIT);
        project.setIsDelete((short) 0);
        User user = new User();
        user.setId(userId);
        project.setUser(user);
        project.setValidStart(DateUtil.parse(start, "yyyy-MM-dd").toTimestamp());
        project.setValidEnd(DateUtil.parse(end, "yyyy-MM-dd").toTimestamp());
        project.setTaboo(projectContentObj.optString("taboo"));
        project.setSpecial(projectContentObj.optString("special"));
        project.setRemark(projectContentObj.optString("remark"));
        projectRepository.save(project);

        List<ProjectMedia> projectMedias = new ArrayList<>();
        for (int i = 0; i < medias.length(); i++) {
            Long id = medias.optLong(i);
            ProjectMedia projectMedia = new ProjectMedia();
            projectMedia.setProject(project);
            Media media = new Media();
            media.setId(id);
            projectMedia.setMedia(media);
            projectMedias.add(projectMedia);
        }
        projectMediaRepository.saveAll(projectMedias);

        List<ProjectTerminal> projectTerminals = new ArrayList<>();
        for (int i = 0; i < terminals.length(); i++) {
            Long id = terminals.optLong(i);
            ProjectTerminal projectTerminal = new ProjectTerminal();
            projectTerminal.setProject(project);
            Terminal terminal = new Terminal();
            terminal.setId(id);
            projectTerminal.setTerminal(terminal);
            projectTerminals.add(projectTerminal);

            if (projectTerminals.size() > 300) {
                projectTerminalRepository.saveAll(projectTerminals);
                projectTerminals.clear();
            }
        }
        if (projectTerminals.size() > 0) {
            projectTerminalRepository.saveAll(projectTerminals);
        }

        if (lastAudit == ProjectAuditEnum.AUDIT_OK) {
            projectTerminalReload(project.getId());
        }
        return new ResultUtil<>().setData("");
    }

    @Override
    public Result project(Long userId, Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            return new ResultUtil().setErrorMsg("无效的方案");
        }
        HashMap<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("id", project.getId());
        projectInfo.put("name", project.getName());
        projectInfo.put("mgroupId", project.getMgroup().getId());
        projectInfo.put("clientId", project.getClient().getId());
        projectInfo.put("clientName", project.getClient().getName());
        projectInfo.put("contractNo", project.getContractNo());
        projectInfo.put("adCount", project.getAdCount());
        projectInfo.put("taboo", project.getTaboo());
        projectInfo.put("special", project.getSpecial());
        projectInfo.put("remark", project.getRemark());
        projectInfo.put("audit", project.getAudit().ordinal());
        projectInfo.put("start", project.getValidStart() == null ? "" : DateUtil.format(project.getValidStart(), "yyyy-MM-dd"));
        projectInfo.put("end", project.getValidEnd() == null ? "" : DateUtil.format(project.getValidEnd(), "yyyy-MM-dd"));

        return new ResultUtil<>().setData(projectInfo);
    }

    @Override
    public Result projectMedias(Long userId, Long id) {
        List<ProjectMedia> projectMedias = projectMediaRepository.findAllByProjectId(id);

        List<HashMap<String, Object>> medias = new ArrayList<>();
        for (ProjectMedia projectMedia : projectMedias) {
            HashMap<String, Object> media = new HashMap<>();
            Media m = projectMedia.getMedia();
            media.put("id", m.getId());
            media.put("name", m.getName());
            media.put("type", m.getType());
            media.put("path", OSSClientUtil.getAccessUrl() + "/" + m.getPath());
            media.put("localPath", m.getLocalPath());
            medias.add(media);
        }

        return new ResultUtil<>().setData(medias);
    }

    private void projectTerminalReload(Long id) {
        List<ProjectTerminal> projectTerminals = projectTerminalRepository.findAllByProjectId(id);
        for (ProjectTerminal projectTerminal : projectTerminals) {
            rabbitMqManager.sendTask(projectTerminal.getTerminal().getId());
        }
    }

    @Override
    @Cacheable(value = "project", key = "#id")
    public ProjectBean project(Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            ProjectBean projectBean = new ProjectBean();
            projectBean.setId(project.getId());
            projectBean.setName(project.getName());
            projectBean.setAdCount(project.getAdCount());
            projectBean.setValidStart(project.getValidStart());
            projectBean.setValidEnd(project.getValidEnd());
            projectBean.setAudit(project.getAudit());

            return projectBean;
        }

        return null;
    }
}