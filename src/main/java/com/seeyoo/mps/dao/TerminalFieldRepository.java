package com.seeyoo.mps.dao;

import com.seeyoo.mps.model.TerminalField;
import com.seeyoo.mps.generator.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 终端字段数据处理层
 * @author GaoJunZhang
 */
public interface TerminalFieldRepository extends BaseRepository<TerminalField,Long>  {

    @Query(value = "SELECT t.* FROM terminal_field t,field f where t.field_id=f.id AND t.terminal_id=:terminalId AND f.is_delete=0 ORDER BY f.sort ASC", nativeQuery = true)
    List<TerminalField> findAllByTerminalIdOrderByCreateTimeDesc(@Param("terminalId") Long terminalId);

}