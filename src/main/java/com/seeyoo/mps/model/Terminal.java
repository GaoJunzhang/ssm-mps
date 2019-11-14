package com.seeyoo.mps.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "terminal")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Terminal extends BaseEntity implements java.io.Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tgroup_id", nullable = false)
    private Tgroup tgroup;
    private String name;
    private String mac;
    private String devState;
    private String dlFileSize;
    private String useableSpace;
    private String diskSpace;
    private String taskName;
    private String playContent;
    private Timestamp imdUpdateTime;
    private String serverIp;
    private String serverMac;
    private String systemVersion;
    private String appVersion;
    private Short connectStatus;
    private Timestamp connectTime;
    private Timestamp disconnectTime;
    private Integer adCount;
    private Integer adUseCount;
    private Short isDelete;
    private Timestamp deleteTime;
}