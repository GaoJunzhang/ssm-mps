package com.seeyoo.mps.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by user on 2019/9/6.
 */
@Getter
@Setter
public class AuthBean{
    private Long id;
    private String name;
    private String action;
    private Short type;
    private int lev;
    private String code;
    private int sort;
    private String component;
    private String icon;
    private Long parentId;
    private String parentName;
    private String parentCode;
    private Short hidden;
    private String path;
}
