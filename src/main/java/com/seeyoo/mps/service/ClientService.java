package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Client;
import com.seeyoo.mps.model.Industry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.seeyoo.mps.vo.SearchVo;

import java.util.List;
import java.util.Map;

/**
 * 广告主接口
 *
 * @author GaoJunZhang
 */
public interface ClientService extends BaseService<Client, Long> {

    /**
     * 多条件分页获取
     *
     * @param searchVo
     * @param pageable
     * @return
     */
    Map<String, Object> findByCondition(String name, String salerName, Long industryId, Long userId, SearchVo searchVo, Pageable pageable);

    void updateIsDelete(Long id);
}