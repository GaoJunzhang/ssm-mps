package com.seeyoo.mps.service;

import com.seeyoo.mps.generator.base.BaseService;
import com.seeyoo.mps.model.OperationLog;
import com.seeyoo.mps.vo.SearchVo;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 操作日志接口
 * @author GaoJunZhang
 */
public interface OperationLogService extends BaseService<OperationLog,Long> {

    Map<String, Object> logList(String userName, String action, SearchVo searchVo, Pageable pageable);
}