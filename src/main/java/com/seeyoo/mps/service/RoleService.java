package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.seeyoo.mps.vo.SearchVo;

import java.util.List;
import java.util.Map;

/**
 * 角色接口
 * @author GaoJunZhang
 */
public interface RoleService extends BaseService<Role,Long> {

    List<Role> findAllByRole(String role);

    void updateIsDelete(Long id);

    List<Role> findAllByIsDelete(Short isDelete);

    Map<String, Object> roleData(Role role, SearchVo searchVo, Pageable pageable);
}