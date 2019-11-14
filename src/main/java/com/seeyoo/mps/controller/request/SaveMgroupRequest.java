package com.seeyoo.mps.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SaveMgroupRequest {
    private Long parentId;
    private Long mgroupId;
    private String name;
    private Short share;
    private String remark;
}
