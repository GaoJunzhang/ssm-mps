package com.seeyoo.mps.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuditProjectRequest {
    private Long[] ids;
    private Short audit;
    private String remark;
}
