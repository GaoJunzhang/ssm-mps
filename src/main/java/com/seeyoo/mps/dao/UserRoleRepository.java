package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findAllByUserId(Long id);
    void deleteByUserId(Long id);

}