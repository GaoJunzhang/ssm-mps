package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.UserMgroup;
import com.seeyoo.mps.model.UserTgroup;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 用户媒体组数据处理层
 *
 * @author Wangj
 */
public interface UserTgroupRepository extends BaseRepository<UserTgroup, Long> {

    @Query("from UserTgroup ut where ut.user.id = ?1 and ut.tgroup.isDelete = 0 order by ut.tgroup.name asc")
    List<UserTgroup> userTgroupsByUserAndName(Long userId);

    void deleteByUserId(Long id);
}