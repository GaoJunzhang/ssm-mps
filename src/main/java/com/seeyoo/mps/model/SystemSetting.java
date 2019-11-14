package com.seeyoo.mps.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "system_setting")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SystemSetting implements java.io.Serializable {
    @Id
    private String name;
    private String setting;
}