package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "task")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseEntity implements java.io.Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mgroup_id")
    private Mgroup mgroup;
    private String name;
    @Enumerated
    private TaskTypeEnum type;
    private Timestamp start;
    private Timestamp end;
    private Integer width;
    private Integer height;
    @Enumerated
    private TaskAuditEnum audit;
    private Short isDelete;
}