package com.seeyoo.mps.controller;

import com.seeyoo.mps.model.Client;
import com.seeyoo.mps.model.Industry;
import com.seeyoo.mps.model.Saler;
import com.seeyoo.mps.model.User;
import com.seeyoo.mps.service.ClientService;
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
@Api(tags = "广告主管理接口")
@RequestMapping("/client")
@Transactional
public class ClientController {

    @Autowired
    private ClientService clientService;

    @RequiresPermissions("clientList")
    @RequestMapping(value = "/getClientData", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Object getClientData(HttpSession session,
                                String name,
                                String salerName,
                                Long industryId,
                                @ModelAttribute SearchVo searchVo,
                                @ModelAttribute PageVo pageVo){
        long userId = (long) session.getAttribute("userId");
        return new ResultUtil<>().setData(clientService.findByCondition(name,salerName,industryId,userId, searchVo, PageUtil.initPage(pageVo)));
    }

    @RequiresPermissions("client:del")
    @RequestMapping(value = "/delClient/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除广告主")
    public Result<String> delClient(@PathVariable String[] ids) {
        for (String id : ids) {
            clientService.updateIsDelete(Long.parseLong(id));
        }
        return new ResultUtil<String>().setSuccessMsg("删除成功");
    }

    @RequiresPermissions("client:add")
    @RequestMapping(value = "/saveClient", method = RequestMethod.POST)
    @ApiOperation(value = "保存广告主")
    public Result<String> saveClient(HttpSession session, Long id,
                                     @RequestParam(name = "name", required = true) String name,
                                     @RequestParam(name = "industryId",required = true) long industryId,
                                     @RequestParam(name = "salerId", required = true) long salerId,
                                     @RequestParam(name = "shortName", required = true) String shortName,
                                     @RequestParam(name = "area") String area,
                                     @RequestParam(name = "contact", required = true) String contact,
                                     @RequestParam(name = "contactType", required = true) String contactType,
                                     String position) {
        Client client = null;
        if (id == null){
            client = new Client();
            client.setIsDelete((short)0);
            client.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }else {
            client = clientService.get(id);
            client.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        }
        client.setName(name);
        long userId = (long) session.getAttribute("userId");
        User user = new User();
        user.setId(userId);
        client.setUser(user);
        Industry industry = new Industry();
        industry.setId(industryId);
        client.setIndustry(industry);
        Saler saler = new Saler();
        saler.setId(salerId);
        client.setSaler(saler);
        client.setShortName(shortName);
        client.setArea(area);
        client.setContact(contact);
        client.setContactType(contactType);
        client.setPosition(position);
        try {
            clientService.save(client);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultUtil<String>().setErrorMsg("保存失败");
        }
        return new ResultUtil<String>().setSuccessMsg("保存成功");
    }

}
