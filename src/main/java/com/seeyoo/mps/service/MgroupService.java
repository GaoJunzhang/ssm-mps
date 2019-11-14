package com.seeyoo.mps.service;

import com.seeyoo.mps.controller.request.SaveMgroupRequest;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Mgroup;
import com.seeyoo.mps.vo.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 媒体组接口
 *
 * @author Wangj
 */
public interface MgroupService extends BaseService<Mgroup, Long> {

    List<HashMap<String, Object>> mgroupListData(Long userId, Long mgroupId);

    HashMap<String, Object> mgroupByName(Long userId, String name, Integer page);

    HashMap<String, Object> mgroupById(Long userId, Long id);

    Result saveMgroup(Long userId, SaveMgroupRequest saveMgroupRequest);

    Result delMgroup(Long userId, Long mgroupId);

    Map<String, Object> findMgroupTree(Long userId);

    String mgroupCode(Long mgroupId);
}