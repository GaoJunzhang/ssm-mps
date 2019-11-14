package com.seeyoo.mps.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

/**
 * Created by user on 2019/9/10.
 */
@Data
public class MenuBean {
    private BigInteger id;
    private String name;
    private String action;
    private Byte type;
    private Integer lev;
    private String code;
    private Integer sort;
    private String component;
    private String icon;
    private String parentId;
    private Byte hidden;
    private String path;

    public MenuBean(){}
    public MenuBean(BigInteger id,String name,String action,Byte type,Integer lev,String code,Integer sort,String component,String icon,String parentId,Byte hidden,String path){
        this.id = id;
        this.name = name;
        this.action = action;
        this.type = type;
        this.lev = lev;
        this.code = code;
        this.sort = sort;
        this.component = component;
        this.icon = icon;
        this.parentId = parentId;
        this.hidden = hidden;
        this.path = path;
    }
}

