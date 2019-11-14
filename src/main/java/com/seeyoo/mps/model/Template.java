package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "template")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Template extends BaseEntity implements java.io.Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String name;
    private Integer width;
    private Integer height;
    private String areas;
    private Short isDelete;
}