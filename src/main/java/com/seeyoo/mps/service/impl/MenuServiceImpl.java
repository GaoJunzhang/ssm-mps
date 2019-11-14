package com.seeyoo.mps.service.impl;

import com.seeyoo.mps.bean.MenuBean;
import com.seeyoo.mps.dao.AuthRepository;
import com.seeyoo.mps.model.Auth;
import com.seeyoo.mps.service.MenuService;
import com.seeyoo.mps.tool.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2019/9/6.
 */
@Slf4j
@Service
@Transactional
public class MenuServiceImpl implements MenuService {

    @Autowired
    private AuthRepository authRepository;

    @Override
    public AuthRepository getRepository() {
        return authRepository;
    }

    public Auth findById(Long id){
        return authRepository.findById(id).orElse(null);
    }

    public List<Auth> findByAuth(Auth auth){
        return authRepository.findByAuthOrderByCodeDesc(auth);
    }

    public List<Auth> findByAction(String action){
        return authRepository.findByAction(action);
    }

    public List<Auth> findByAuthIsNull(){
        return authRepository.findByAuthIsNullOrderByCodeDesc();
    }

    public List<MenuBean> getMenuByUserId(Long userId){
        try {
            return CommonUtil.castEntity(authRepository.getMenuByUserId(userId),MenuBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
