package com.seeyoo.mps.controller;

import com.seeyoo.mps.controller.request.SaveTemplateRequest;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.SearchVo;
import com.seeyoo.mps.model.Template;
import com.seeyoo.mps.service.TemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

/**
 * @author Wangj
 */
@Slf4j
@RestController
@Api(description = "模板管理接口")
@RequestMapping("/template")
@Transactional
public class TemplateController extends BaseController<Template, Long> {

    @Autowired
    private TemplateService templateService;

    @Override
    public TemplateService getService() {
        return templateService;
    }

    @RequiresPermissions("taskEdit")
    @RequestMapping(value = "/templateListData", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object templateList(HttpSession session, String name, Short screen,
                               @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(defaultValue = "descend") String sortOrder, @RequestParam(defaultValue = "createTime") String sortField) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil().setData(templateService.templateList(userId, name, screen, pageNo, pageSize, sortOrder, sortField));
    }

    @RequiresPermissions("template:add")
    @RequestMapping(value = "/saveTemplate", method = RequestMethod.POST)
    public Object saveTemplate(HttpSession session, @RequestBody SaveTemplateRequest saveTemplateRequest) {
        long userId = (long) session.getAttribute("userId");
        return templateService.saveTemplate(userId, saveTemplateRequest);
    }

    @RequiresPermissions("template:del")
    @RequestMapping(value = "/delTemplate/{templateId}", method = RequestMethod.DELETE)
    public Object delTemplate(HttpSession session, @PathVariable Long templateId) {
        long userId = (long) session.getAttribute("userId");
        return templateService.delTemplate(userId, templateId);
    }
}
