package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;

import java.sql.Timestamp;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "saler")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Saler {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "name", length = 64)
    private String name;
    @Column(name = "sex")
    private Short sex;
    @Column(name = "tel", length = 16)
    private String tel;
    @Column(name = "is_delete")
    private Short isDelete;
    @Column(name = "create_time", length = 19)
    private Timestamp createTime;
}
