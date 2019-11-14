package com.seeyoo.mps.conn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Terminal {

	private Long id;
	private String name;
	private String mac;
	private Long tgroupId;
	private String code;

	// 即时信息
	private String devState;
	private String dlFileSize;
	private String useableSpace;
	private String diskSpace;
	private String playStatus;
	private String taskName;
	private String secondaryTaskName;
	private String templateID;
	private String secondaryTemplateID;
	private String blight;
	private String fileStatus;
	private String playContent;
	private String secondaryPlayContent;

	private String dev;
	private String nkVersion;
	private String appVersion;
	private String thirdTask;
	private String thirdVersion;

	// 非即时信息
	private List<String> fileList;
	private List<String[]> taskFileList;
	private String devNet;
	private String ulog;
	private String onOffInfo;
	private String serverInfo;
	private String playControl;
	private String iccidInfo;
	private String hanWangInfo;


	private Timestamp connectTime;
	private Timestamp disconnectTime;
	


	
	
}
