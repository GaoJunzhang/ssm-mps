package com.seeyoo.mps.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SaveTemplateRequest {
    private Long templateId;
    private String name;
    private Integer width;
    private Integer height;
    private String areas;
}
