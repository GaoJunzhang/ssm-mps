package com.seeyoo.mps.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SaveUserRequest {
    private Long userId;
    private String name;
    private String account;
    private Short status;
    private Long roleId;
    private String start;
    private String end;
    private Long[] mgroupIds;
    private Long[] tgroupIds;
    private String avatar;
}
