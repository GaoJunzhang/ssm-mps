package com.seeyoo.mps.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SimpleTerminalBean {
    private Long id;
    private String mac;
    private String name;
}
