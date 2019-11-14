package com.seeyoo.mps.controller;

import com.seeyoo.mps.bean.TerminalExpandBean;
import com.seeyoo.mps.model.TerminalExpand;
import com.seeyoo.mps.service.TerminalExpandService;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.SearchVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(tags = "终端扩展管理接口")
@RequestMapping("/terminalExpand")
@Transactional
public class TerminalExpandController {

    @Autowired
    private TerminalExpandService terminalExpandService;

    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object getByCondition(@ModelAttribute TerminalExpand terminalExpand,
                                 @ModelAttribute SearchVo searchVo,
                                 @ModelAttribute PageVo pageVo) {

        Page<TerminalExpand> page = terminalExpandService.findByCondition(terminalExpand, searchVo, PageUtil.initPage(pageVo));
        return new ResultUtil<Page<TerminalExpand>>().setData(page);
    }

    @GetMapping(value = "/terminalExpandByTid/{id}")
    @ApiOperation(value = "获取终端扩展信息")
    public Object terminalExpandByTid(@PathVariable Long id) {
        return new ResultUtil<>().setData(terminalExpandService.terminalExpandByTid(id));
    }

    @PostMapping(value = "/saveTerminalExpand")
    public Object saveTerminalExpand(@ModelAttribute TerminalExpandBean terminalExpandBean, String fields) {
        return new ResultUtil<>().setData(terminalExpandService.saveTerminalExpand(terminalExpandBean, fields));
    }
}
