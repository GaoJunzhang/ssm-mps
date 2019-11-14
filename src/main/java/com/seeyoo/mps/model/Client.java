package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "client")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Client extends BaseEntity implements java.io.Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saler_id")
    private Saler saler;
    private String name;
    private String shortName;
    private String area;
    private String contact;
    private String contactType;
    private String position;
    private Short isDelete;
}
