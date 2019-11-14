package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "auth")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Auth implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_id")
    private Auth auth;
    private String name;
    private String action;
    private Short type;
    private int lev;
    private String code;
    private int sort;
    private String component;
    private String icon;
    private Short hidden;
    private String path;
}