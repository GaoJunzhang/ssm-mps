package com.seeyoo.mps.service;

import com.seeyoo.mps.bean.TerminalExpandBean;
import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.TerminalExpand;
import com.seeyoo.mps.vo.SearchVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 终端扩展接口
 * @author GaoJunZhang
 */
public interface TerminalExpandService extends BaseService<TerminalExpand,Long> {

    /**
    * 多条件分页获取
    * @param terminalExpand
    * @param searchVo
    * @param pageable
    * @return
    */
    Page<TerminalExpand> findByCondition(TerminalExpand terminalExpand, SearchVo searchVo, Pageable pageable);

    TerminalExpandBean terminalExpandByTid(Long id);

    String saveTerminalExpand (TerminalExpandBean terminalExpandBean, String fieldStr);
}