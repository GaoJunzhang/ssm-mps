package com.seeyoo.mps.controller;

import com.seeyoo.mps.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {
    @Autowired
    private TestService testService;

    @GetMapping(path = "/sendCmd")
    public String sendCmd() {
        testService.sendCmd();
        return "OK";
    }

    @GetMapping(path = "/sendTask")
    public String sendTask() {
        testService.sendTask();
        return "OK";
    }
}
