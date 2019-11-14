package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by user on 2019/10/29.
 */
@Entity
@Table(name = "terminal_expand")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TerminalExpand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;
    private String deviceName;
    private String deviceNo;
    private String keyNo;
    private String simNo;
    private String deviceType;
    private String macAddr;
    private Timestamp dateOfManufacture;
    private String industry;
    private String address;
    private String requirement;
    private String bulidingDescription;
    private String height;
    private String area;
    private Short popularity;
    private Short isActivity;
    private String activityType;
    private String registeredCapital;
    private String monthWages;
    private String industryRank;
    private String marketDes;
    private String marketValue;
    private String annualTurnover;
    private String officeCost;
    private String employees;
    private String playTaboos;
    private String competitionName;
    private Short type;
    private Short level;
    public String bestCompany;
    public Timestamp installTime;
    public String screen;
    public String charger;
    public String chargerMobile;
    public String contractor;
    public String contractorMobile;
    public String simInfo;
    public String installAddr;
}
