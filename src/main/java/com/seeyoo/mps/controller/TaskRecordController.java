package com.seeyoo.mps.controller;

import com.seeyoo.mps.controller.request.AuditTaskRequest;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.model.Task;
import com.seeyoo.mps.model.TaskRecord;
import com.seeyoo.mps.service.TaskRecordService;
import com.seeyoo.mps.service.TaskService;
import com.seeyoo.mps.tool.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author Wangj
 */
@Slf4j
@RestController
@Api(description = "任务记录管理接口")
@RequestMapping("/taskRecord")
@Transactional
public class TaskRecordController extends BaseController<TaskRecord, Long> {

    @Autowired
    private TaskRecordService taskRecordService;

    @Override
    public TaskRecordService getService() {
        return taskRecordService;
    }

    @RequiresPermissions("taskRecordList")
    @RequestMapping(value = "/taskRecordListData", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object taskRecordList(HttpSession session, String name, String userName, Short type,
                                 @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(defaultValue = "descend") String sortOrder, @RequestParam(defaultValue = "createTime") String sortField) {
        long userId = (long) session.getAttribute("userId");
        String userCode = (String) session.getAttribute("userCode");
        return new ResultUtil().setData(taskRecordService.taskRecordList(userId, userCode, name, userName, type, pageNo, pageSize, sortOrder, sortField));
    }

    @RequiresPermissions("taskRecordList")
    @RequestMapping(value = "/taskRecordTerminals", method = RequestMethod.GET)
    @ApiOperation(value = "发布终端列表")
    public Object taskRecordTerminals(HttpSession session, Long id, String name,
                                      @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(defaultValue = "asc") String sortOrder, @RequestParam(defaultValue = "id") String sortField) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil().setData(taskRecordService.taskRecordTerminals(userId, id, name, pageNo, pageSize, sortOrder, sortField));
    }

    @RequiresPermissions("taskRecordList")
    @RequestMapping(value = "/taskRecordMedias", method = RequestMethod.GET)
    @ApiOperation(value = "发布媒体列表")
    public Object taskRecordMedias(HttpSession session, Long id, String name) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil().setData(taskRecordService.taskRecordMedias(userId, id, name));
    }

}
