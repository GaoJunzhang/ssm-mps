package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "project")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity implements java.io.Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mgroup_id", nullable = false)
    private Mgroup mgroup;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    private String name;
    private String contractNo;
    private Timestamp validStart;
    private Timestamp validEnd;
    private Integer adCount;
    private String taboo;
    private String special;
    @Enumerated
    private ProjectAuditEnum audit;
    private String remark;
    private Short isDelete;
}
