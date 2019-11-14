package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Role;
import com.seeyoo.mps.generator.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色数据处理层
 * @author GaoJunZhang
 */
public interface RoleRepository extends BaseRepository<Role,Long>  {

    public List<Role> findByRoleAndIsDelete(String role,Short isDelete);

    List<Role> findAllByIsDelete(Short isDelete);

    @Modifying
    @Transactional
    @Query(value = "update Role r set r.isDelete=1 where r.id=:id")
    void updateIsDelete(@Param("id") Long id);
}