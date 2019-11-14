package com.seeyoo.mps.dao;

import com.seeyoo.mps.bean.MenuBean;
import com.seeyoo.mps.generator.base.BaseRepository;
import com.seeyoo.mps.model.Auth;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by user on 2019/9/6.
 */
public interface AuthRepository extends BaseRepository<Auth,Long> {
    List<Auth> findByAuthOrderByCodeDesc(@Param("auth") Auth auth);

    List<Auth> findByAction(@Param("action") String action);

    List<Auth> findByAuthIsNullOrderByCodeDesc();

    @Query(value = "SELECT a.id,a.name,a.action,a.type,a.lev,a.code,a.sort,IFNULL(a.component,'') as component,IFNULL(a.icon,'') as icon,IFNULL(a.auth_id,'') as parentId,a.hidden,IFNULL(a.path,'') as path from user u left JOIN user_role ur " +
            "on u.id=ur.user_id " +
            "LEFT JOIN role_auth ra on ur.role_id=ra.role_id " +
            "LEFT JOIN auth a on a.id=ra.auth_id " +
            "where u.id=:userId " +
            "and u.is_delete=0 " +
            "and a.lev<3 " +
            "ORDER BY a.sort ASC",nativeQuery = true)
    List<Object[]> getMenuByUserId(@Param("userId") Long userId);

    List<Auth> findAllByOrderBySortAsc();

    List<Auth> findAllByAuthIsNullOrderBySortAsc();

    List<Auth> findAllByAuthIdOrderBySortAsc(@Param("authId") Long authId);
}
