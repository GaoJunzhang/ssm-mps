package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Saler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.seeyoo.mps.vo.SearchVo;

import java.util.List;
import java.util.Map;

/**
 * 销售员接口
 * @author GaoJunZhang
 */
public interface SalerService extends BaseService<Saler,Long> {

    /**
    * 多条件分页获取
    * @param saler
    * @param searchVo
    * @param pageable
    * @return
    */
    Map<String, Object> findByCondition(Saler saler, SearchVo searchVo, Pageable pageable);

    void updateIsDelete(Long id);
}