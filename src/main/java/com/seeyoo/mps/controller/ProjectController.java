package com.seeyoo.mps.controller;

import com.seeyoo.mps.controller.request.AuditProjectRequest;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.model.Project;
import com.seeyoo.mps.service.ProjectService;
import com.seeyoo.mps.service.ProjectTerminalService;
import com.seeyoo.mps.tool.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

/**
 * @author Wangj
 */
@Slf4j
@RestController
@Api(description = "方案管理接口")
@RequestMapping("/project")
@Transactional
public class ProjectController extends BaseController<Project, Long> {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectTerminalService projectTerminalService;

    @Override
    public ProjectService getService() {
        return projectService;
    }

    @RequiresPermissions("projectList")
    @RequestMapping(value = "/projectListData", method = RequestMethod.GET)
    @ApiOperation(value = "方案列表")
    public Object projectList(HttpSession session, Long mgroupId, String name, Short audit, String contractNo, String clientName, String salerName,
                              @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "descend") String sortOrder, @RequestParam(defaultValue = "createTime") String sortField) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil().setData(projectService.projectList(userId, mgroupId, name, audit, contractNo, clientName, salerName, pageNo, pageSize, sortOrder, sortField));
    }

    @RequiresPermissions("project:del")
    @RequestMapping(value = "/delProject/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除方案")
    public Object delProject(@PathVariable Long[] ids) {
        return projectService.delProject(ids);
    }

    @RequiresPermissions("project:audit")
    @RequestMapping(value = "/auditProject", method = RequestMethod.POST)
    @ApiOperation(value = "方案审核")
    public Object auditProject(HttpSession session, @RequestBody AuditProjectRequest auditProjectRequest) {
        long userId = (long) session.getAttribute("userId");
        return projectService.auditProject(userId, auditProjectRequest);
    }

    @RequiresPermissions("projectList")
    @RequestMapping(value = "/auditProjectRecords/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "方案审核列表")
    public Object auditProjectRecords(@PathVariable Long id) {
        return projectService.auditProjectRecords(id);
    }

    @RequiresPermissions("project:edit")
    @RequestMapping(value = "/saveProject", method = RequestMethod.POST)
    @ApiOperation(value = "保存方案")
    public Object saveProject(HttpSession session, @RequestBody String projectContent) {
        long userId = (long) session.getAttribute("userId");
        return projectService.saveProject(userId, projectContent);
    }

    @RequiresPermissions("project:edit")
    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "获取方案")
    public Object project(HttpSession session, @PathVariable Long id) {
        long userId = (long) session.getAttribute("userId");
        return projectService.project(userId, id);
    }

    @RequiresPermissions("project:edit")
    @RequestMapping(value = "/projectMedias/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "获取方案")
    public Object projectMedias(HttpSession session, @PathVariable Long id) {
        long userId = (long) session.getAttribute("userId");
        return projectService.projectMedias(userId, id);
    }

    @RequiresPermissions("project:edit")
    @RequestMapping(value = "/projectTerminals/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "获取方案")
    public Object projectTerminals(HttpSession session, @PathVariable Long id) {
        long userId = (long) session.getAttribute("userId");
        return projectTerminalService.projectTerminals(userId, id);
    }

    @RequiresPermissions("project:edit")
    @RequestMapping(value = "/uploadTerminals")
    @ApiOperation(value = "上传刊位点位")
    public Object uploadTerminals(HttpSession session, @RequestParam("file") MultipartFile file) {
        long userId = (long) session.getAttribute("userId");
        if (!file.isEmpty()) {
            try {
                return projectTerminalService.uploadTerminals(userId, file.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ResultUtil<>().setErrorMsg("无效的文件");
    }

    @RequiresPermissions("projectScheduling")
    @RequestMapping(value = "/projectSchedulingListData", method = RequestMethod.GET)
    @ApiOperation(value = "方案排期列表")
    public Object projectSchedulingListData(HttpSession session, Long tgroupId, String mac, String name, String start, String end,
                                            @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        long userId = (long) session.getAttribute("userId");
        return projectTerminalService.projectSchedulingList(userId, tgroupId, mac, name, start, end, pageNo, pageSize);
    }

    @RequiresPermissions("project:exportVacancy")
    @RequestMapping(value = "/exportVacancy", method = RequestMethod.GET)
    @ApiOperation(value = "导出空位表")
    public Object exportVacancy(HttpSession session, Long tgroupId, String start, String end, @RequestParam(defaultValue = "1") Integer count) {
        long userId = (long) session.getAttribute("userId");
        return projectTerminalService.exportVacancy(userId, tgroupId, start, end, count);
    }
}
