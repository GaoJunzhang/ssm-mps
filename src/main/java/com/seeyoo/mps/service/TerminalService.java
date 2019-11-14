package com.seeyoo.mps.service;

import com.seeyoo.mps.bean.TerminalBean;
import com.seeyoo.mps.model.Terminal;
import com.seeyoo.mps.vo.Result;
import com.seeyoo.mps.vo.SearchVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 终端接口
 * @author GaoJunZhang
 */
public interface TerminalService{

    /**
    * 多条件分页获取
    * @param pageable
    * @return
    */
    Map<String, Object> terminalList(TerminalBean terminalBean, Pageable pageable);

    Page<Terminal> terminalPage(TerminalBean terminalBean, Pageable pageable);

    int rename(Long id, String name);

    int updateTgroupById(Long tgroupId, Long id);

    Result exportVacancy(TerminalBean terminalBean);
}