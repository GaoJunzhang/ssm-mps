package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Field;
import com.seeyoo.mps.vo.PageVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.seeyoo.mps.vo.SearchVo;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 自定义字段接口
 * @author GaoJunZhang
 */
public interface FieldService extends BaseService<Field,Long> {

    Map<String,Object> fieldList(Long terminalId, String fieldName, Pageable pageable);

    List<Field> findByEnName(String enName);

    List<Map<String,String>> findAllByIsDeleteOrderBySortAsc(Long terminalId);
}