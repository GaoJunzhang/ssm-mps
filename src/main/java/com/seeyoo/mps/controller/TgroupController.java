package com.seeyoo.mps.controller;

import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.model.Tgroup;
import com.seeyoo.mps.service.TgroupService;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(tags = "终端组管理接口")
@RequestMapping("/tgroup")
@Transactional
public class TgroupController {

    @Autowired
    private TgroupService tgroupService;

    @RequestMapping(value = "/tgroupData", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object tgroupData(String name,
                             @ModelAttribute SearchVo searchVo,
                             @ModelAttribute PageVo pageVo) {

        if (StrUtil.isEmpty(pageVo.getSortField())) {
            pageVo.setSortField("createTime");
            pageVo.setSortOrder("desc");
        }
        return new ResultUtil<>().setData(tgroupService.findByCondition(name, searchVo, PageUtil.initPage(pageVo)));
    }

    @RequestMapping(value = "/tgroupListData", method = RequestMethod.GET)
    public Object tgroupListData(Long id) {
        return new ResultUtil<>().setData(tgroupService.tgroupListData(id));
    }

    @RequestMapping(value = "/tgroupByName", method = RequestMethod.GET)
    @ApiOperation(value = "根据姓名查询终端组")
    public Object tgroupByName(@RequestParam String name, @RequestParam Integer page) {
        return new ResultUtil<>().setData(tgroupService.tgroupData(name, page));
    }

    @RequestMapping(value = "/saveTgroup", method = RequestMethod.POST)
    @ApiOperation(value = "保存终端组")
    public Object saveTgroup(HttpSession session, Long id,
                             Long tgroupId,
                             Double lng,
                             Double lat,
                             @RequestParam(name = "name", required = true) String name) {
        long userId = (long) session.getAttribute("userId");
        return tgroupService.saveTgroup(userId, id, tgroupId, name, lng, lat);
    }

    @ApiOperation(value = "根据id查询终端组")
    @GetMapping(value = "/getTgroupById/{id}")
    public Object getTgroupById(@PathVariable Long id) {
        Tgroup tgroup = tgroupService.get(id);
        Map<String, String> map = new HashMap<>();
        if (tgroup != null) {
            map.put("id", tgroup.getId() + "");
            map.put("name", tgroup.getName());
            map.put("lng", tgroup.getLng() == null ? "" : tgroup.getLng() + "");
            map.put("lat", tgroup.getLat() == null ? "" : tgroup.getLat() + "");
        }
        return new ResultUtil<>().setData(map);
    }

    @RequestMapping(value = "/tgroupById", method = RequestMethod.GET)
    @ApiOperation(value = "查询用户终端组")
    public Object tgroupById(HttpSession session, @RequestParam Long id) {
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil<>().setData(tgroupService.tgroupById(userId, id));
    }
}
