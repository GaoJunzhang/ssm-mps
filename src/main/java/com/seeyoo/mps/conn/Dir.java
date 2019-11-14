package com.seeyoo.mps.conn;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Dir {

	public static String CT_POLLING(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>01</CmdType></MSG>";
		return dir;
	}

	public static String CT_AUTH(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>02</CmdType><Code>" + arg[1]
				+ "</Code><WebUrl>" + arg[2] + "</WebUrl></MSG>";
		return dir;
	}

	public static String CT_SETSERVER(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>03</CmdType><MCSAddr>" + arg[1]
				+ "</MCSAddr><MCSDomain>" + arg[2] + "</MCSDomain><MCSPort>" + arg[3] + "</MCSPort><MainFtpAddr>"
				+ arg[4] + "</MainFtpAddr><MainFtpDomain>" + arg[5] + "</MainFtpDomain><MainFtpPort>" + arg[6]
				+ "</MainFtpPort><MainFtpUser>" + arg[7] + "</MainFtpUser><MainFtpPasswd>" + arg[8]
				+ "</MainFtpPasswd><SubAddr>" + arg[9] + "</SubAddr><SubDomain>" + arg[10] + "</SubDomain><SubPort>"
				+ arg[11] + "</SubPort><SubFtpAddr>" + arg[12] + "</SubFtpAddr><SubFtpDomain>" + arg[13]
				+ "</SubFtpDomain><SubFtpPort>" + arg[14] + "</SubFtpPort><SubFtpUser>" + arg[15]
				+ "</SubFtpUser><SubFtpPasswd>" + arg[16] + "</SubFtpPasswd><TrackAddr>" + arg[17]
				+ "</TrackAddr><TrackPort>" + arg[18] + "</TrackPort><SATAddr>" + arg[19] + "</SATAddr></MSG>";
		return dir;
	}

	public static String CT_SETDLTIME(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>04</CmdType><TimeMode>" + arg[1]
				+ "</TimeMode><DownloadTime>" + arg[2] + "</DownloadTime></MSG>";
		return dir;
	}

	public static String CT_SETTIMESHOW(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>05</CmdType><TimeShow>" + arg[1]
				+ "</TimeShow></MSG>";
		return dir;
	}

	public static String CT_SETTIME(String[] arg) {
		Date currentTime = new Date();
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>06</CmdType><Date>"
				+ formatter1.format(currentTime) + "</Date><Time>" + formatter2.format(currentTime) + "</Time></MSG>";
		return dir;
	}

	public static String CT_POWERRST(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>07</CmdType><PwrCtrl>" + arg[1]
				+ "</PwrCtrl></MSG>";
		return dir;
	}

	public static String CT_SETONOFFTIME(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>08</CmdType>" + arg[1]
				+ "</MSG>";
		return dir;
	}

	public static String CT_SETDEVNO(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>09</CmdType><Sno>" + arg[1]
				+ "</Sno><Group>" + arg[2] + "</Group></MSG>";
		return dir;
	}

	public static String CT_CLINTNETSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>10</CmdType><DevType>" + arg[1]
				+ "</DevType><NetType>" + arg[2] + "</NetType><DHCP>" + arg[3] + "</DHCP><DevIP>" + arg[4]
				+ "</DevIP><DevSubMask>" + arg[5] + "</DevSubMask><DevGateWay>" + arg[6] + "</DevGateWay><WLanName>"
				+ arg[7] + "</WLanName><WLanEncryption>" + arg[8] + "</WLanEncryption><WLanPasswd>" + arg[9]
				+ "</WLanPasswd></MSG>";
		return dir;
	}

	public static String CT_SETSPLASH(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>11</CmdType><BootLogoName>"
				+ arg[1] + "</BootLogoName></MSG>";
		return dir;
	}

	public static String CT_SETLOGO(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>12</CmdType><LogoName>" + arg[1]
				+ "</LogoName></MSG>";
		return dir;
	}

	public static String CT_DLOAD(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>13</CmdType><FileName>" + arg[1]
				+ "</FileName><FileType>" + arg[2] + "</FileType></MSG>";
		return dir;
	}

	public static String CT_INTERPLY(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>14</CmdType><InsMode>" + arg[1]
				+ "</InsMode>" + arg[2] + "</MSG>";
		return dir;
	}

	public static String CT_PLAYCTRL(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>15</CmdType><CtrlPlay>" + arg[1]
				+ "</CtrlPlay></MSG>";
		return dir;
	}

	public static String CT_CAPSCREEN(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>16</CmdType><CapScreen>"
				+ arg[1] + "</CapScreen><Addr>" + arg[2] + "</Addr></MSG>";
		return dir;
	}

	public static String CT_VOLUME(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>17</CmdType><Volume>" + arg[1]
				+ "</Volume></MSG>";
		return dir;
	}

	public static String CT_PICTURE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>18</CmdType><BrightnessBL>"
				+ arg[1] + "</BrightnessBL><BrightnessPIC>" + arg[2] + "</BrightnessPIC><Contrast>" + arg[3]
				+ "</Contrast><Saturation>" + arg[4] + "</Saturation></MSG>";
		return dir;
	}

	public static String CT_DLTFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>19</CmdType>" + arg[1]
				+ "</MSG>";
		return dir;
	}

	public static String CT_IMDSTAT(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>20</CmdType></MSG>";
		return dir;
	}

	public static String CT_NOTIMDSTAT(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>21</CmdType></MSG>";
		return dir;
	}

	public static String CT_GETVERSION(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>27</CmdType></MSG>";
		return dir;
	}

	public static String CT_GETNET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>28</CmdType></MSG>";
		return dir;
	}

	public static String CT_GETULOG(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>29</CmdType></MSG>";
		return dir;
	}

	public static String CT_GETSERVER(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>30</CmdType></MSG>";
		return dir;
	}

	public static String CT_GETONOFF(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>31</CmdType></MSG>";
		return dir;
	}

	public static String CT_GETPLAYCTRL(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>32</CmdType></MSG>";
		return dir;
	}

	public static String CT_GETFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>22</CmdType></MSG>";
		return dir;
	}

	public static String CT_UPLDFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>23</CmdType><UploadFile>"
				+ arg[1] + "<UploadFile></MSG>";
		return dir;
	}

	public static String CT_UPDATE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>25</CmdType><UpdateType>"
				+ arg[1] + "</UpdateType><UpdateFileName>" + arg[2] + "</UpdateFileName></MSG>";
		return dir;
	}

	public static String CT_FORMATDISK(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>26</CmdType></MSG>";
		return dir;
	}

	public static String CT_UPDATEDEFAULTFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>33</CmdType><FileType>" + arg[1]
				+ "</FileType><FileName>" + arg[2] + "</FileName></MSG>";
		return dir;
	}

	public static String CT_GETTIME(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>34</CmdType></MSG>";
		return dir;
	}

	public static String CT_SETDWNSPEED(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>35</CmdType><FTPSpeed>" + arg[1]
				+ "</FTPSpeed></MSG>";
		return dir;
	}

	public static String CT_GETTERFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>37</CmdType><FileType>" + arg[1]
				+ "</FileType><PageNO>" + arg[2] + "</PageNO></MSG>";
		return dir;
	}

	public static String CT_DELTERFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>38</CmdType><FileType>" + arg[1]
				+ "</FileType><DelFileNum>" + arg[2] + "</DelFileNum>" + arg[3] + "</MSG>";
		return dir;
	}

	public static String CT_DELALLTERFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>39</CmdType></MSG>";
		return dir;
	}

	public static String CT_DELDWNLDTERFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>40</CmdType><File>" + arg[1]
				+ "</File></MSG>";
		return dir;
	}

	public static String CT_AUTODELTERFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>41</CmdType><DayNum>" + arg[1]
				+ "</DayNum></MSG>";
		return dir;
	}

	public static String CT_DIRDAY(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>43</CmdType><DirDay>" + arg[1]
				+ "</DirDay></MSG>";
		return dir;
	}

	public static String CT_DWNMODE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>44</CmdType><Mode>" + arg[1]
				+ "</Mode></MSG>";
		return dir;
	}

	public static String CT_TASKFILESTATUS(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>46</CmdType><PageNO>" + arg[1]
				+ "</PageNO></MSG>";
		return dir;
	}

	public static String CT_ONOFF3G(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>47</CmdType><G1>" + arg[1]
				+ "</G1><G2>" + arg[2] + "</G2><G3>" + arg[3] + "</G3><G4>" + arg[4] + "</G4><G5>" + arg[5]
				+ "</G5></MSG>";
		return dir;
	}

	public static String CT_GETPLAYFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>49</CmdType></MSG>";
		return dir;
	}

	public static String CT_SETMAP(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>50</CmdType><Longitude>"
				+ arg[1] + "</Longitude><Latitude>" + arg[2] + "</Latitude></MSG>";
		return dir;
	}

	public static String CT_UPLOADLOGFILE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>52</CmdType><LogType>" + arg[1]
				+ "</LogType><LogDate>" + arg[2] + "</LogDate><Addr>" + arg[3] + "</Addr></MSG>";
		return dir;
	}

	public static String CT_P2PSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>54</CmdType><DownAccelerated>"
				+ arg[1] + "</DownAccelerated></MSG>";
		return dir;
	}

	public static String CT_SYNCSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>53</CmdType><SyncPlay>" + arg[1]
				+ "</SyncPlay></MSG>";
		return dir;
	}

	public static String CT_DWNTASKOPR(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>55</CmdType><Type>" + arg[1]
				+ "</Type><DownTime>" + arg[2] + "</DownTime><TaskName>" + arg[3] + "</TaskName></MSG>";
		return dir;
	}

	public static String CT_GETDWNTASK(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>56</CmdType></MSG>";
		return dir;
	}

	public static String CT_PREDWNSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>57</CmdType><PreDownload>"
				+ arg[1] + "</PreDownload></MSG>";
		return dir;
	}

	public static String CT_SELFCMD(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>58</CmdType><SelfCmd><![CDATA[" + arg[1]
				+ "]]></SelfCmd></MSG>";
		return dir;
	}

	public static String CT_GETICCID(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>61</CmdType></MSG>";
		return dir;
	}

	public static String CT_PROJECTORSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>62</CmdType><Projector>"
				+ arg[1] + "</Projector></MSG>";
		return dir;
	}

	public static String CT_PROJECTORSCREENSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>63</CmdType><Monitor>" + arg[1]
				+ "</Monitor></MSG>";
		return dir;
	}

	public static String CT_GETSATISTICS(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>64</CmdType><BeginDate>"
				+ arg[1] + "</BeginDate><EndDate>" + arg[2] + "</EndDate><List>" + arg[3] + "</List></MSG>";
		return dir;
	}

	public static String CT_SENDMESSAGE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>65</CmdType><Times>" + arg[1]
				+ "</Times><Color>" + arg[2] + "</Color><Content><![CDATA[" + arg[3] + "]]></Content></MSG>";
		return dir;
	}

	public static String CT_THIRDSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>66</CmdType><ThirdPath>"
				+ arg[1] + "</ThirdPath><ThirdTimeout>" + arg[2] + "</ThirdTimeout></MSG>";
		return dir;
	}

	public static String CT_THIRDTASKSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>67</CmdType><ThirdTask>"
				+ arg[1] + "</ThirdTask></MSG>";
		return dir;
	}

	public static String CT_CLEANPARAMSET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>68</CmdType><Purify>" + arg[1]
				+ "</Purify><Pm25>" + arg[2] + "</Pm25><TempMos>" + arg[3] + "</TempMos><AutoSta>" + arg[4]
				+ "</AutoSta><Title>" + arg[5] + "</Title><Enable>" + arg[6] + "</Enable></MSG>";
		return dir;
	}

	public static String CT_CLEANPARAMGET(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>69</CmdType></MSG>";
		return dir;
	}

	public static String CT_SETTEL(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>71</CmdType><TelEnable>"
				+ arg[1] + "</TelEnable><Tel>" + arg[2] + "</Tel>";
		if (arg.length >= 4) {
			dir += "<Mobile>" + arg[3] + "</Mobile>";
		}
		dir += "</MSG>";
		return dir;
	}

	public static String CT_SETOTHERDEVICEREPORT(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>72</CmdType><Camera>" + arg[1]
				+ "</Camera><Wifi>" + arg[2] + "</Wifi></MSG>";
		return dir;
	}

	public static String CT_GETOTHERDEVICEREPORT(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>73</CmdType></MSG>";
		return dir;
	}

	public static String CT_SETOSDPASSWORD(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>74</CmdType><Password>" + arg[1]
				+ "</Password></MSG>";
		return dir;
	}

	public static String CT_GETOSDPASSWORD(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>75</CmdType></MSG>";
		return dir;
	}

	public static String CT_XWUPDATE(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>89</CmdType><UpdateFileName>"
				+ arg[1] + "</UpdateFileName></MSG>";
		return dir;
	}

	public static String CT_NEWSPAPERDIR(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>14</CmdType>" + arg[1]
				+ "</MSG>";
		return dir;
	}

	public static String CT_SETWEATHER(String[] arg) {
		String dir = "<MSG><ID>" + arg[0] + "</ID><Class>1</Class><Num>1</Num><CmdType>99</CmdType><CityId>" + arg[1]
				+ "</CityId><CityName>" + arg[2] + "</CityName><Addr>" + arg[3]
				+ "</Addr></MSG>";
		return dir;
	}
}
