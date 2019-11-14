package com.seeyoo.mps.bean;

import com.seeyoo.mps.model.ProjectAuditEnum;
import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by user on 2019/10/23.
 */
@Getter
@Setter
public class ProjectBean implements Serializable {
    private Long id;
    private String name;
    private Integer adCount;
    private Timestamp validStart;
    private Timestamp validEnd;
    private ProjectAuditEnum audit;
}
