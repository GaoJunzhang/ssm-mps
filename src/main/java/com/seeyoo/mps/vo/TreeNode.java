package com.seeyoo.mps.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2019/9/3.
 */
@Getter
@Setter
public class TreeNode<T> {
    @ApiModelProperty(value = "id", notes = "id")
    public Long id;

    @ApiModelProperty(value = "父ID", notes = "父ID")
    private Long authId;

    @ApiModelProperty(value = "子节点", notes = "子节点")
    private List<T> children = new ArrayList<>();
}
