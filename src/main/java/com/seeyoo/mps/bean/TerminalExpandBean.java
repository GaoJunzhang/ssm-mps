package com.seeyoo.mps.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by user on 2019/10/29.
 */
@Getter
@Setter
public class TerminalExpandBean {
    private Long id;
    private Long terminalId;
    private String deviceName;
    private String deviceNo;
    private String keyNo;
    private String simNo;
    private String deviceType;
    private String macAddr;
    private String dateOfManufacture;
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
    public String installTime;
    public String screen;
    public String charger;
    public String chargerMobile;
    public String contractor;
    public String contractorMobile;
    public String simInfo;
    public String installAddr;
    public Integer adCount;
}
