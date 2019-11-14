package com.seeyoo.mps.controller;

import com.seeyoo.mps.model.Field;
import com.seeyoo.mps.model.Terminal;
import com.seeyoo.mps.model.TerminalField;
import com.seeyoo.mps.service.FieldService;
import com.seeyoo.mps.service.TerminalFieldService;
import com.seeyoo.mps.tool.PageUtil;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.PageVo;
import com.seeyoo.mps.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author GaoJunZhang
 */
@Slf4j
@RestController
@Api(tags = "自定义字段管理接口")
@RequestMapping("/field")
@Transactional
public class FieldController {

    @Autowired
    private FieldService fieldService;

    @Autowired
    private TerminalFieldService terminalFieldService;

    @RequestMapping(value = "/fieldListData", method = RequestMethod.GET)
    @ApiOperation(value = "分页获取字段")
    public Object fieldListData(String fieldName,
                                @RequestParam(name = "terminalId", required = true) Long terminalId,
                                @ModelAttribute PageVo pageVo) {

        return new ResultUtil<>().setData(fieldService.fieldList(terminalId, fieldName, PageUtil.initPage(pageVo)));
    }

    @RequestMapping(value = "/delField/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "批量删除")
    public Result<String> delField(@PathVariable String[] ids) {
        for (String id : ids) {
            fieldService.delete(Long.parseLong(id));
        }
        return new ResultUtil<String>().setSuccessMsg("删除成功");
    }

    @RequestMapping(value = "/saveField", method = RequestMethod.POST)
    @ApiOperation(value = "保存")
    public Result<String> saveField(Long id, @RequestParam(name = "fieldName", required = true) String fieldName, @RequestParam(name = "enName", required = true) String enName,Integer sort) {
        Field field = null;
        if (id == null) {
            if (fieldService.findByEnName(enName).size() > 0) {
                return new ResultUtil<String>().setErrorMsg("字段英文名已存在");
            }
            field = new Field();
            field.setCreateTime(new Timestamp(System.currentTimeMillis()));
            field.setIsDelete((short) 0);
        } else {
            field = fieldService.get(id);
        }
        field.setEnName(enName);
        field.setFieldName(fieldName);
        field.setSort(sort);
        fieldService.save(field);
        return new ResultUtil<String>().setSuccessMsg("保存成功");
    }

    @RequestMapping(value = "/saveTerField", method = RequestMethod.POST)
    @ApiOperation(value = "保存终端字段")
    public Result<String> saveTerField(@RequestParam(name = "terminalId", required = true) Long terminalId,
                                       @RequestParam(name = "fieldIds", required = true) String[] fieldIds) {
        List<TerminalField> terminalFields = new ArrayList<>();
        for (String fieldId : fieldIds) {
            TerminalField terminalField = new TerminalField();
            Field field = new Field();
            field.setId(Long.parseLong(fieldId));
            Terminal terminal = new Terminal();
            terminal.setId(terminalId);
            terminalField.setField(field);
            terminalField.setTerminal(terminal);
            terminalField.setCreateTime(new Timestamp(System.currentTimeMillis()));
            terminalFields.add(terminalField);
        }
        terminalFieldService.saveOrUpdateAll(terminalFields);
        return new ResultUtil<String>().setSuccessMsg("保存成功");
    }

    @RequestMapping(value = "/fieldList", method = RequestMethod.GET)
    @ApiOperation(value = "自定义字段列表")
    public Object fieldList(@RequestParam(name = "terminalId") Long terminalId ) {
        return new ResultUtil<>().setData(fieldService.findAllByIsDeleteOrderBySortAsc(terminalId));
    }
}
