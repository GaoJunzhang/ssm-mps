package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.TerminalExpand;
import org.springframework.data.jpa.repository.JpaRepository;
import com.seeyoo.mps.generator.base.BaseRepository;

import java.util.List;

/**
 * 终端扩展数据处理层
 * @author GaoJunZhang
 */
public interface TerminalExpandRepository extends BaseRepository<TerminalExpand,Long>  {
    TerminalExpand findByTerminalId(Long terminalId);

}