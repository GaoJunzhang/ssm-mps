package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Media;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 媒体接口
 * @author GaoJunZhang
 */
public interface MediaService extends BaseService<Media,Long> {


    Map<String, Object> pageMediaData(Long mgroupId, String name, Short audit, String[] types, long userId, Pageable pageable);

    List<Media> findAllByMd5(String md5);

    int updateAuditByIds(short audit, String[] ids);

}