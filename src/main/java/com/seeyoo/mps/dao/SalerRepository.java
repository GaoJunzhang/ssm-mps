package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Saler;
import org.springframework.data.jpa.repository.JpaRepository;
import com.seeyoo.mps.generator.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 销售员数据处理层
 * @author GaoJunZhang
 */
public interface SalerRepository extends BaseRepository<Saler,Long>  {
    @Modifying
    @Transactional
    @Query(value = "update Saler t set t.isDelete=1 where t.id=:id")
    void updateIsDelete(@Param("id") Long id);
}