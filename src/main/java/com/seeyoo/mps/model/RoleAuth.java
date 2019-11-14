package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "role_auth")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleAuth extends BaseEntity implements java.io.Serializable {
    @ManyToOne()
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne()
    @JoinColumn(name = "auth_id")
    private Auth auth;
}