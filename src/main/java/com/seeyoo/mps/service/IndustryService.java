package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Industry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.seeyoo.mps.vo.SearchVo;

import java.util.List;
import java.util.Map;

/**
 * 行业接口
 * @author GaoJunZhang
 */
public interface IndustryService extends BaseService<Industry,Long> {

    /**
    * 多条件分页获取
    * @param industry
    * @param searchVo
    * @param pageable
    * @return
    */
    Map<String, Object> findByCondition(Industry industry, SearchVo searchVo, Pageable pageable);

    void updateIsDelete(Long id);


    List<Map<String, Object>> findAllByIsDeleteAndUserId(short isDelete, long userId);
}