package com.seeyoo.mps.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.FieldRepository;
import com.seeyoo.mps.dao.TerminalFieldRepository;
import com.seeyoo.mps.model.Field;
import com.seeyoo.mps.model.TerminalField;
import com.seeyoo.mps.service.FieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义字段接口实现
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class FieldServiceImpl implements FieldService {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private TerminalFieldRepository terminalFieldRepository;

    @Override
    public FieldRepository getRepository() {
        return fieldRepository;
    }

    @Override
    public Map<String,Object> fieldList(Long terminalId,String fieldName, Pageable pageable){

        Specification specification = new Specification<Field>() {
            @Override
            public Predicate toPredicate(Root<Field> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                predicate.getExpressions().add(cb.equal(r.get("isDelete"), 0));
                if (StrUtil.isNotEmpty(fieldName)) {
                    predicate.getExpressions().add(cb.like(r.get("fieldName"), "%" + fieldName + "%"));
                }
                return predicate;
            }
        };
        Page<Field> fields = fieldRepository.findAll(specification, pageable);
        List<Map<String, Object>> fieldMap = new ArrayList<>();
        for (Field field: fields){
            Map<String, Object> f = new HashMap<>();
            f.put("id",field.getId());
            f.put("fieldName", field.getFieldName());
            f.put("enName", field.getEnName());
            f.put("sort", field.getSort());
            f.put("createTime", DateUtil.format(field.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            fieldMap.add(f);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pageNo", pageable.getPageNumber());
        map.put("totalCount", fields.getTotalElements());
        map.put("data", fieldMap);
        return map;
    }

    public List<Field> findByEnName(String enName){
        return fieldRepository.findByEnNameAndIsDelete(enName,(short)0);
    }

    public List<Map<String,String>> findAllByIsDeleteOrderBySortAsc(Long terminalId) {
        List<TerminalField> terminalFields = terminalFieldRepository.findAllByTerminalIdOrderByCreateTimeDesc(terminalId);
        List<Field> fields = fieldRepository.findAllByIsDeleteOrderBySortAsc((short)0);
        List<Map<String,String>> maps = new ArrayList<>(fields.size());
        for (Field field : fields){
            Map<String,String> map = new HashMap<>();
            String id = "";
            String fieldContent = "";
            map.put("fieldId", field.getId()+"");
            map.put("fieldName", field.getFieldName());
            map.put("enName", field.getEnName());
            for (TerminalField terminalField : terminalFields){
                if (field.getId() == terminalField.getField().getId()){
                    id = terminalField.getId()+"";
                    fieldContent = terminalField.getFieldContent();
                    break;
                }
            }
            map.put("id", id);
            map.put("fieldContent", fieldContent);
            maps.add(map);
        }
        return maps;
    }
}