package com.seeyoo.mps.service;

import com.seeyoo.mps.bean.MenuBean;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.Auth;

import java.util.List;

/**
 * Created by user on 2019/9/6.
 */
public interface MenuService extends BaseService<Auth,Long> {
    Auth findById(Long id);

    List<Auth> findByAuth(Auth auth);

    List<Auth> findByAction(String action);

    List<Auth> findByAuthIsNull();

    List<MenuBean> getMenuByUserId(Long userId);
}
