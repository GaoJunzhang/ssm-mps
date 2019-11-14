package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;

import java.sql.Timestamp;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "industry")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Industry {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "name", length = 32)
    private String name;
    @Column(name = "is_delete")
    private Short isDelete;
    @Column(name = "create_time", length = 19)
    private Timestamp createTime;
}
