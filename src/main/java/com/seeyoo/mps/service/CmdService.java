package com.seeyoo.mps.service;

import com.seeyoo.mps.controller.request.CmdRequest;
import com.seeyoo.mps.vo.Result;

public interface CmdService {
    Result cmdStatus(String opNo);

    Result reboot(Long userId, CmdRequest cmdRequest);

    Result setTime(Long userId, CmdRequest cmdRequest);

    Result getTime(Long userId, Long terminalId);

    Result setOnOff(Long userId, CmdRequest cmdRequest);

    Result getOnOff(Long userId, Long terminalId);

    Result setServer(Long userId, CmdRequest cmdRequest);

    Result getServer(Long userId, Long terminalId);

    Result sendTask(Long userId, CmdRequest cmdRequest);

    Result sendDefaultTask(Long userId, CmdRequest cmdRequest);

    Result setCity(Long userId, CmdRequest cmdRequest);

    Result getCity(Long userId, Long terminalId);

    Result sendUpgrade(Long userId, CmdRequest cmdRequest);

    Result checkTask(Long userId, Long terminalId);

    Result selfCmd(Long userId, Long terminalId, String cmd);

}
