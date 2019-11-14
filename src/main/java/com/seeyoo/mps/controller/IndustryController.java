package com.seeyoo.mps.controller;

import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.model.Industry;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.IndustryService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(tags = "行业管理接口")
@RequestMapping("/industry")
@Transactional
public class IndustryController {

    @Autowired
    private IndustryService industryService;

    @RequiresPermissions("industryList")
    @RequestMapping(value = "/getIndustryData", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object  getIndustryData(@ModelAttribute Industry industry,
                                                       @ModelAttribute SearchVo searchVo,
                                                       @ModelAttribute PageVo pageVo){
        if (StrUtil.isEmpty(pageVo.getSortField())) {
            pageVo.setSortField("createTime");
            pageVo.setSortOrder("desc");
        }
        return new ResultUtil<>().setData(industryService.findByCondition(industry, searchVo, PageUtil.initPage(pageVo)));
    }

    @RequiresPermissions("industry:del")
    @RequestMapping(value = "/delIndustry/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除行业")
    public Result<String> delIndustry(@PathVariable String[] ids) {
        for (String id : ids) {
            industryService.updateIsDelete(Long.parseLong(id));
        }
        return new ResultUtil<String>().setSuccessMsg("删除成功");
    }

    @RequiresPermissions("industry:add")
    @RequestMapping(value = "/saveIndustry", method = RequestMethod.POST)
    @ApiOperation(value = "保存行业")
    public Result<String> saveIndustry(HttpSession session,Long id,@RequestParam String name) {
        Industry industry = null;
        if (id == null){
            industry = new Industry();
            industry.setIsDelete((short)0);
            industry.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }else {
            industry = industryService.get(id);
        }
        industry.setName(name);
        long userId = (long) session.getAttribute("userId");
        User user = new User();
        user.setId(userId);
        industry.setUser(user);
        industryService.save(industry);
        return new ResultUtil<String>().setSuccessMsg("保存成功");
    }


    @RequiresPermissions("industryList")
    @RequestMapping(value = "/getIndustryList", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object getIndustryList(HttpSession session){
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil<>().setData(industryService.findAllByIsDeleteAndUserId((short)0,userId));
    }
}
