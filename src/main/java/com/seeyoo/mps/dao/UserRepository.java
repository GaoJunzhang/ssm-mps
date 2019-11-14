package com.seeyoo.mps.dao;

import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

public interface UserRepository extends BaseRepository<User,Long> {

    List<User> findAllByAccountAndIsDelete(String account, Short isDelete);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update `user` u set u.login_ip = :ip , u.login_time = NOW() where u.id = :id", nativeQuery = true)
    int updateUserLogin(@Param("id") Long id,@Param("ip") String ip);

    @Query(value = "SELECT u.code FROM  `user` u WHERE u.user_id = ?1 ORDER BY u.code DESC LIMIT 1", nativeQuery = true)
    String maxUserCode(Long userId);
}