package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.Role;
import com.seeyoo.mps.model.RoleAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleAuthRepository extends JpaRepository<RoleAuth, Long> {
    List<RoleAuth> findAllByRoleId(Long roleId);

    int deleteAllByRole(Role role);
}