package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.Tgroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 终端组数据处理层
 *
 * @author GaoJunZhang
 */
public interface TgroupRepository extends BaseRepository<Tgroup, Long> {

    List<Tgroup> findAllByIsDelete(Short isDelete);

    List<Tgroup> findAllByTgroupIdAndIsDelete(@Param("tgroupId") Long tgroupId, @Param("isDelete") Short isDelete);

    @Query(value = "SELECT t.code FROM tgroup t WHERE t.tgroup_id = ?1 ORDER BY t.code DESC LIMIT 1", nativeQuery = true)
    String maxTgroupCode(Long tgroupId);
}