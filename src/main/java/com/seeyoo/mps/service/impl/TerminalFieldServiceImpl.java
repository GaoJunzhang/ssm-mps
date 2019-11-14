package com.seeyoo.mps.service.impl;

import com.seeyoo.mps.dao.TerminalFieldRepository;
import com.seeyoo.mps.service.TerminalFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 终端字段接口实现
 * @author GaoJunZhang
 */
@Slf4j
@Service
@Transactional
public class TerminalFieldServiceImpl implements TerminalFieldService {

    @Autowired
    private TerminalFieldRepository terminalFieldRepository;

    @Override
    public TerminalFieldRepository getRepository() {
        return terminalFieldRepository;
    }
}