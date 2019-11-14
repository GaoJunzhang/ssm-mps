package com.seeyoo.mps.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by user on 2019/9/3.
 */

@Getter
@Setter
@ApiModel(value = "菜单VO")
public class AuthVo extends TreeNode<AuthVo> implements Serializable {
    @ApiModelProperty(value = "权限名称", notes = "权限名称")
    public String title;

    @ApiModelProperty(value = "id", notes = "id")
    private String key;

    @ApiModelProperty(value = "权限路径action",notes = "权限路径action")
    private String action;

    @ApiModelProperty(value = "排序",notes = "排序")
    private Integer sort;

    @ApiModelProperty(value = "icon",notes="菜单icon")
    private String icon;

    @ApiModelProperty(value = "component",notes="菜单页面路径")
    private String component;
}
