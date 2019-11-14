package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.Client;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 广告主数据处理层
 * @author GaoJunZhang
 */
public interface ClientRepository extends BaseRepository<Client,Long>  {

    @Modifying
    @Transactional
    @Query(value = "update Client t set t.isDelete=1 where t.id=:id")
    void updateIsDelete(@Param("id") Long id);
}