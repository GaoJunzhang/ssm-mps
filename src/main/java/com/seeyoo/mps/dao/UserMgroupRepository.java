package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.Mgroup;
import com.seeyoo.mps.model.UserMgroup;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 用户媒体组数据处理层
 *
 * @author Wangj
 */
public interface UserMgroupRepository extends BaseRepository<UserMgroup, Long> {

    @Query("from UserMgroup um where um.user.id = ?1 and um.mgroup.isDelete = 0 order by um.mgroup.name asc")
    List<UserMgroup> userMgroupsByUserAndName(Long userId);

    void deleteByUserId(Long id);
}