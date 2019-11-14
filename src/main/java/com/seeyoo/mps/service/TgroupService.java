package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Tgroup;
import com.seeyoo.mps.vo.SearchVo;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 终端组接口
 *
 * @author GaoJunZhang
 */
public interface TgroupService extends BaseService<Tgroup, Long> {

    Map<String, Object> findByCondition(String name, SearchVo searchVo, Pageable pageable);

    List<HashMap<String, Object>> tgroupListData(Long tgroupId);

    HashMap<String, Object> tgroupData(String name, Integer page);

    Object saveTgroup(Long userId, Long id, Long tgroupId, String name, Double lng, Double lat);

    String tgroupCode(Long tgroupId);

    HashMap<String, Object> tgroupById(Long userId, Long id);
}