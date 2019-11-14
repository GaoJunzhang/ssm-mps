package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "weather")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Weather extends BaseEntity implements java.io.Serializable {
    private Integer weatherId;
    private String cityName;
    private String cityId;
    @Column(name = "area_1")
    private String area1;
    @Column(name = "area_2")
    private String area2;
    @Column(name = "area_3")
    private String area3;
    private String remark;
    private String weather;
}