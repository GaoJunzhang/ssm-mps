package com.seeyoo.mps.controller;

import com.seeyoo.mps.controller.request.SaveMgroupRequest;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.model.Mgroup;
import com.seeyoo.mps.service.MgroupService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

/**
 * @author Wangj
 */
@Slf4j
@RestController
@Api(tags = "媒体组管理接口")
@RequestMapping("/mgroup")
@Transactional
public class MgroupController {

    @Autowired
    private MgroupService mgroupService;

    @RequestMapping(value = "/mgroupListData", method = RequestMethod.GET)
    public Object mgroupListData(HttpSession session, Long id) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil<>().setData(mgroupService.mgroupListData(userId, id));
    }

    @RequestMapping(value = "/mgroupByName", method = RequestMethod.GET)
    public Object mgroupByName(HttpSession session, @RequestParam String name, @RequestParam Integer page) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil<>().setData(mgroupService.mgroupByName(userId, name, page));
    }

    @RequestMapping(value = "/mgroupById", method = RequestMethod.GET)
    public Object mgroupById(HttpSession session, @RequestParam Long id) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil<>().setData(mgroupService.mgroupById(userId, id));
    }

    @RequestMapping(value = "/saveMgroup", method = RequestMethod.POST)
    public Object saveMgroup(HttpSession session, @RequestBody SaveMgroupRequest saveMgroupRequest) {
        long userId = (long) session.getAttribute("userId");
        return mgroupService.saveMgroup(userId, saveMgroupRequest);
    }

    @RequestMapping(value = "/delMgroup/{mgroupId}", method = RequestMethod.DELETE)
    public Object saveMgroup(HttpSession session, @PathVariable Long mgroupId) {
        long userId = (long) session.getAttribute("userId");
        return mgroupService.delMgroup(userId, mgroupId);
    }

    @RequestMapping(value = "/getAuhtSelectData", method = RequestMethod.GET)
    public Object getAuhtSelectData(HttpSession session) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil<>().setData(mgroupService.findMgroupTree(userId));
    }
}
