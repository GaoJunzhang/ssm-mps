package com.seeyoo.mps.controller;

import com.seeyoo.mps.bean.TerminalBean;
import com.seeyoo.mps.model.Terminal;
import com.seeyoo.mps.service.TerminalService;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.SearchVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(description = "终端管理接口")
@RequestMapping("/terminal")
@Transactional
public class TerminalController {

    @Autowired
    private TerminalService terminalService;

    @RequestMapping(value = "/terminalData", method = RequestMethod.GET)
    @RequiresPermissions("TerminalList")
    @ApiOperation(value = "分页获取终端信息")
    public Object terminalData(@ModelAttribute TerminalBean terminalBean,
                               @ModelAttribute PageVo pageVo) {

        Map page = terminalService.terminalList(terminalBean, PageUtil.initPage(pageVo));
        return new ResultUtil<>().setData(page);
    }

    @PostMapping(value = "/rename")
    @RequiresPermissions("terminal:rename")
    @ApiOperation(value = "终端重命名")
    public Result<String> rename(@RequestParam(name = "id", required = true) Long id, @RequestParam(name = "name", required = true) String name) {
        if (terminalService.rename(id,name)!=1){
            return new ResultUtil<String>().setErrorMsg("保存失败");
        }
        return new ResultUtil<String>().setSuccessMsg("操作成功");
    }

    @RequiresPermissions("terminal:tgroup")
    @PostMapping(value = "/updateTgroup")
    @ApiOperation(value = "终端分组")
    public Result<String> updateTgroup(@RequestParam(name = "ids", required = true) String[] ids, @RequestParam(name = "tgroupId", required = true) Long tgroupId) {
        for (String id : ids) {
            terminalService.updateTgroupById(tgroupId,Long.parseLong(id));
        }
        return new ResultUtil<String>().setSuccessMsg("操作成功");
    }

    @RequiresPermissions("terminal:excel")
    @RequestMapping(value = "/exportTerminal", method = RequestMethod.GET)
    @ApiOperation(value = "导出终端信息")
    public Object exportTerminal(@ModelAttribute TerminalBean terminalBean) {
        return terminalService.exportVacancy(terminalBean);
    }
}
