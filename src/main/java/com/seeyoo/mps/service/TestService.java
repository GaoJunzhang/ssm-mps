package com.seeyoo.mps.service;

import cn.hutool.core.util.IdUtil;
import com.seeyoo.mps.dao.TerminalSettingRepository;
import com.seeyoo.mps.integration.mq.RabbitMqManager;
import com.seeyoo.mps.model.TerminalSetting;
import com.seeyoo.mps.model.TerminalSettingEnum;
import com.seeyoo.mps.model.TerminalSettingKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestService {
    @Autowired
    private TerminalSettingRepository terminalSettingRepository;

    @Autowired
    private RabbitMqManager rabbitMqManager;

    public void sendCmd(){
        rabbitMqManager.sendCmd(IdUtil.simpleUUID());
//        TerminalSetting terminalSetting = new TerminalSetting();
//
//        com.seeyoo.mps.model.Terminal terminal = new com.seeyoo.mps.model.Terminal();
//        terminal.setId(1l);
//
//        TerminalSettingKey terminalSettingKey = new TerminalSettingKey(terminal, TerminalSettingEnum.PLAYCONTROL);
//        terminalSetting.setTerminalSettingKey(terminalSettingKey);
//        terminalSetting.setSettings("{11123}");
//        terminalSettingRepository.save(terminalSetting);
    }

    public void sendTask(){
        rabbitMqManager.sendTask(0l);
    }
//
//    public static void main(String[] args) {
//        SimpleHash hash = new SimpleHash("md5", "e10adc3949ba59abbe56e057f20f883e",
//                "abdefeacd7a345860276994e6bffc805", 2);
//        String encodedPassword = hash.toHex();
//        System.out.println(encodedPassword);
//
//    }
}
