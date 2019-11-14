package com.seeyoo.mps.controller;

import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.generator.base.BaseController;
import com.seeyoo.mps.model.Industry;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.SearchVo;
import com.seeyoo.mps.model.Saler;
import com.seeyoo.mps.service.SalerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(description = "销售员管理接口")
@RequestMapping("/saler")
@Transactional
public class SalerController {

    @Autowired
    private SalerService salerService;

    @RequiresPermissions("saleList")
    @RequestMapping(value = "/getSalerData", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object getSalerData(@ModelAttribute Saler saler,
                                                            @ModelAttribute SearchVo searchVo,
                                                            @ModelAttribute PageVo pageVo){

        if (StrUtil.isEmpty(pageVo.getSortField())) {
            pageVo.setSortField("createTime");
            pageVo.setSortOrder("desc");
        }
        return new ResultUtil<>().setData(salerService.findByCondition(saler, searchVo, PageUtil.initPage(pageVo)));
    }

    @RequiresPermissions("saler:del")
    @RequestMapping(value = "/delSaler/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除")
    public Result<String> delSaler(@PathVariable String[] ids) {
        for (String id : ids) {
            salerService.updateIsDelete(Long.parseLong(id));
        }
        return new ResultUtil<String>().setSuccessMsg("删除成功");
    }

    @RequiresPermissions("saler:add")
    @RequestMapping(value = "/saveSaler", method = RequestMethod.POST)
    @ApiOperation(value = "保存")
    public Result<String> saveSaler(HttpSession session, Long id,
                                    @RequestParam(name = "name", required = true) String name,
                                    @RequestParam(name = "sex", required = true) Short sex,
                                    @RequestParam(name = "tel",required = true) String tel) {
        Saler saler = null;
        if (id == null){
            saler = new Saler();
            saler.setIsDelete((short)0);
            saler.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }else {
            saler = salerService.get(id);
        }
        saler.setName(name);
        saler.setSex(sex);
        saler.setTel(tel);
        long userId = (long) session.getAttribute("userId");
        User user = new User();
        user.setId(userId);
        saler.setUser(user);
        salerService.save(saler);
        return new ResultUtil<String>().setSuccessMsg("保存成功");
    }
}
