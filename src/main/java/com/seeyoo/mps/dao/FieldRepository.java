package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import com.seeyoo.mps.generator.base.BaseRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 自定义字段数据处理层
 * @author GaoJunZhang
 */
public interface FieldRepository extends BaseRepository<Field,Long>  {

    List<Field> findByEnNameAndIsDelete(@Param("enName") String enName ,@Param("isDelete") Short isDelete);

    List<Field> findAllByIsDeleteOrderBySortAsc(@Param("isDelete") Short isDelete);
}