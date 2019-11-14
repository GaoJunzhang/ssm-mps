package com.seeyoo.mps.conn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Command implements Serializable {
    Long id;
    String mac;
    String name;
    String cmdId;
    String oprNo;
    short cmdNo;
    Timestamp makeTime;
    Timestamp sendTime;
    Timestamp returnTime;
    short sendStatus = 0;
    short returnStatus = 0;
    String sendContent;
    String returnContent;
    Long userId;

    public Command(Long id, String mac, String name, String cmdId, String oprNo, Short cmdNo, String sendContent, Long userId) {
        this.id = id;
        this.mac = mac;
        this.name = name;
        this.cmdId = cmdId;
        this.oprNo = oprNo;
        this.cmdNo = cmdNo;
        this.makeTime = new Timestamp(System.currentTimeMillis());
        this.sendContent = sendContent;
        this.userId = userId;
    }
}