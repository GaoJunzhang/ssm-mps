package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Media;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import com.seeyoo.mps.generator.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 媒体数据处理层
 * @author GaoJunZhang
 */
public interface MediaRepository extends BaseRepository<Media,Long>  {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update media set path=:path , duration=:duration, remark=:remark,width=:width,height=:height where id=:id",nativeQuery = true)
    int updateMediaPathAndDuration(@Param("id") Long id,@Param("path") String path,@Param("duration") int duration,@Param("remark") String remark,@Param("width") int width,@Param("height") int height);

    List<Media> findAllByMd5(@Param("md5") String md5);

    @Modifying
    @Transactional
    @Query(value = "update media m set m.audit=:audit where m.id in (:ids)", nativeQuery = true)
    int updateAuditByIds(@Param("audit") short audit,@Param("ids") Collection<Long> ids);
}