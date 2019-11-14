package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Mgroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.seeyoo.mps.generator.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 媒体组数据处理层
 *
 * @author Wangj
 */
public interface MgroupRepository extends BaseRepository<Mgroup, Long> {

    @Query("from Mgroup m where m.mgroup.id = ?1 and m.isDelete = 0 order by m.name asc")
    List<Mgroup> mgroupsByIdAndName(Long mgroupId);

    @Query(value = "SELECT m.code FROM mgroup m WHERE m.mgroup_id = ?1 ORDER BY m.code DESC LIMIT 1", nativeQuery = true)
    String maxMgroupCode(Long mgroupId);

    Page<Mgroup> findAll(Specification<Mgroup> spec, Pageable pageable);

    List<Mgroup> findAllByUserIdAndIsDelete(@Param("userId") Long userId,@Param("isDelete") short isDelete);
}