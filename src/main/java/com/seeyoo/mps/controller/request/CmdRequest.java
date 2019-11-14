package com.seeyoo.mps.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CmdRequest {
    private Long tgroupId;
    private Long[] terminalIds;
    private String param;
}
