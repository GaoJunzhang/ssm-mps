package com.seeyoo.mps.controller;

import com.seeyoo.mps.service.OperationLogService;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.SearchVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by user on 2019/11/11.
 */
@Slf4j
@RestController
@Api(tags = "媒体管理接口")
@RequestMapping("/operationLog")
@Transactional
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @RequiresPermissions("LogList")
    @RequestMapping(value = "/getLogData", method = RequestMethod.GET)
    @ApiOperation(value = "分页日志")
    public Object getLogData(String userName,String action, @ModelAttribute PageVo pageVo, @ModelAttribute SearchVo searchVo){
        return new ResultUtil<>().setData(operationLogService.logList(userName,action,searchVo, PageUtil.initPage(pageVo)));
    }
}
