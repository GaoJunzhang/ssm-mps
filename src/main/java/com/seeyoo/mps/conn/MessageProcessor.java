package com.seeyoo.mps.conn;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.TerminalSettingRepository;
import com.seeyoo.mps.model.TerminalSetting;
import com.seeyoo.mps.model.TerminalSettingEnum;
import com.seeyoo.mps.model.TerminalSettingKey;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageProcessor implements Runnable {

    @Autowired
    private ConnChannelGroup channels;

    @Autowired
    private TerminalSettingRepository terminalSettingRepository;

    @Autowired
    private TerminalImdStatProcesser terminalImdStatProcesser;

    @Autowired
    private UserCommander userCommander;

    private SAXReader saxReader = null;

    public MessageProcessor() {
        saxReader = new SAXReader();
        saxReader.setEncoding("UTF-8");

        new Thread(this).start();
    }

    private BlockingQueue<Message> queue = new ArrayBlockingQueue<>(30000);

    void produce(Channel channel, String msg) throws InterruptedException {
        if (!queue.offer(new Message(channel, msg))) {
            log.info("MessageManager length = " + queue.size());
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                msgProc(queue.take());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void msgProc(Message message) throws Exception {
        Channel channel = message.getChannel();
        if (channel != null && channel.isActive() && channel.isOpen()) {
            if (!channels.contains(channel)) {
                channels.register(channel, validate(message.getMsg()));
            } else {
                procMsg(channel, message.getMsg());
            }
        }
    }

    private void procMsg(Channel channel, String msg) throws Exception {
        Attribute<Object> attribute = channel.attr(AttributeKey.valueOf("terminal"));
        Terminal terminal = (Terminal) attribute.get();

        Document doc = saxReader.read(new ByteArrayInputStream((msg).getBytes("UTF-8")));
        Element root = doc.getRootElement();

        String id = root.elementText("ID");
        String iclass = root.elementText("Class");
        String num = root.elementText("Num");
        String cmdType = root.elementText("CmdType");
        String errCode = root.elementText("ErrCode");

        if (!cmdType.equals("20")) {
            log.info("receive from {} ->: {}", terminal.getName(), msg);
        }

        if (!StrUtil.isEmpty(id) && !StrUtil.isEmpty(iclass) && NumberUtil.isInteger(num) && !StrUtil.isEmpty(cmdType)
                && !StrUtil.isEmpty(errCode) && errCode.equals("0")) {

            switch (Integer.parseInt(cmdType)) {
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                    userCommander.setReturn(id, errCode);
                    break;
                case 16: {
                    String image = root.elementText("Image");
                    userCommander.setReturn(id, image);
                }
                break;
                case 17:
                    userCommander.setReturn(id, errCode);
                    break;
                case 18:
                    userCommander.setReturn(id, errCode);
                    break;
                case 19:
                    userCommander.setReturn(id, errCode);
                    break;
                case 20: {
                    terminalImdStatProcesser.produce(channel, msg);
                }
                break;
//			case 21:
//				errCode = root.elementText("ErrCode");
//				if (errCode != null && errCode.equals("0")) {
//					String msgId = id;
//					if (num.equals("12")) {
//						terminal.setThirdPath(root.elementText("ThirdPath") == null ? "" : root
//								.elementText("ThirdPath"));
//						terminal.setThirdTimeout(root.elementText("ThirdTimeout") == null ? "" : root
//								.elementText("ThirdTimeout"));
//					}
//					if (num.equals("11")) {
//						terminal.setProjectorScreen(root.elementText("Monitor") == null ? "" : root
//								.elementText("Monitor"));
//						terminal.setTelEnable(root.elementText("TelEnable") == null ? null : root
//								.elementText("TelEnable"));
//						terminal.setTel(root.elementText("Tel") == null ? null : root.elementText("Tel"));
//					}
//					if (num.equals("10")) {
//						terminal.setProjector(root.elementText("Projector") == null ? "" : root
//								.elementText("Projector"));
//					}
//					if (num.equals("9")) {
//						terminal.setSync(root.elementText("SyncPlay") == null ? "" : root
//								.elementText("SyncPlay"));
//					}
//					if (num.equals("8")) {
//						terminal.setPreDwn(root.elementText("PreDownload") == null ? "" : root
//								.elementText("PreDownload"));
//					}
//					if (num.equals("7")) {
//						terminal.setP2p(root.elementText("DownAccelerated") == null ? "" : root
//								.elementText("DownAccelerated"));
//					}
//					if (num.equals("6")) {
//						terminal.setNkVersion(root.elementText("NKVersion"));
//						terminal.setAppVersion(root.elementText("AppVersion"));
//						terminal.setThirdVersion(root.elementText("ThirdVersion") == null ? "" : root
//								.elementText("ThirdVersion"));
//					}
//					if (num.equals("5")) {
//						terminal.setDevType(root.elementText("DevType"));
//						terminal.setNetType(root.elementText("NetType"));
//						terminal.setDhcp(root.elementText("DHCP"));
//						terminal.setDevIp(root.elementText("DevIP"));
//						terminal.setDevSubMask(root.elementText("DevSubMask"));
//						terminal.setDevGateWay(root.elementText("DevGateWay"));
//						terminal.setWlanName(root.elementText("WLanName"));
//						terminal.setWlanEncryption(root.elementText("WLanEncryption"));
//						terminal.setWlanPasswd(root.elementText("WLanPasswd"));
//					}
//					if (num.equals("4")) {
//						terminal.setUlog(root.elementText("Ulog"));
//					}
//					if (num.equals("3")) {
//						terminal.setMcsAddr(root.elementText("MCSAddr"));
//						terminal.setMcsDomain(root.elementText("MCSDomain"));
//						terminal.setMcsPort(root.elementText("MCSPort"));
//						terminal.setSubAddr(root.elementText("SubAddr"));
//						terminal.setSubDomain(root.elementText("SubDomain"));
//						terminal.setSubPort(root.elementText("SubPort"));
//						terminal.setMainFtpAddr(root.elementText("MainFtpAddr"));
//						terminal.setMainFtpDomain(root.elementText("MainFtpDomain"));
//						terminal.setMainFtpPort(root.elementText("MainFtpPort"));
//						terminal.setMainFtpUser(root.elementText("MainFtpUser"));
//						terminal.setMainFtpPasswd(root.elementText("MainFtpPasswd"));
//						terminal.setSubFtpAddr(root.elementText("SubFtpAddr"));
//						terminal.setSubFtpDomain(root.elementText("SubFtpDomain"));
//						terminal.setSubFtpPort(root.elementText("SubFtpPort"));
//						terminal.setSubFtpUser(root.elementText("SubFtpUser"));
//						terminal.setSubFtpPasswd(root.elementText("SubFtpPasswd"));
//						terminal.setTrackAddr(root.elementText("TrackAddr"));
//						terminal.setTrackPort(root.elementText("TrackPort"));
//						terminal.setSatAddr(root.elementText("SATAddr"));
//					}
//					if (num.equals("2")) {
//						terminal.setDwnSpeed(root.elementText("FTPSpeed"));
//
//						String onTime1 = root.elementText("On1");
//						String offTime1 = root.elementText("Off1");
//						if (onTime1 == null) {
//							onTime1 = "00:00:00";
//						}
//						if (offTime1 == null) {
//							offTime1 = "00:00:00";
//						}
//						terminal.setSec1Start(onTime1);
//						terminal.setSec1End(offTime1);
//						String onTime2 = root.elementText("On2");
//						String offTime2 = root.elementText("Off2");
//						if (onTime2 == null) {
//							onTime2 = "00:00:00";
//						}
//						if (offTime2 == null) {
//							offTime2 = "00:00:00";
//						}
//						terminal.setSec2Start(onTime2);
//						terminal.setSec2End(offTime2);
//						String onTime3 = root.elementText("On3");
//						String offTime3 = root.elementText("Off3");
//						if (onTime3 == null) {
//							onTime3 = "00:00:00";
//						}
//						if (offTime3 == null) {
//							offTime3 = "00:00:00";
//						}
//						terminal.setSec3Start(onTime3);
//						terminal.setSec3End(offTime3);
//						String onTime4 = root.elementText("On4");
//						String offTime4 = root.elementText("Off4");
//						if (onTime4 == null) {
//							onTime4 = "00:00:00";
//						}
//						if (offTime4 == null) {
//							offTime4 = "00:00:00";
//						}
//						terminal.setSec4Start(onTime4);
//						terminal.setSec4End(offTime4);
//						String onTime5 = root.elementText("On5");
//						String offTime5 = root.elementText("Off5");
//						if (onTime5 == null) {
//							onTime5 = "00:00:00";
//						}
//						if (offTime5 == null) {
//							offTime5 = "00:00:00";
//						}
//						terminal.setSec5Start(onTime5);
//						terminal.setSec5End(offTime5);
//
//						terminal.setW1(root.elementText("W1"));
//						terminal.setW2(root.elementText("W2"));
//						terminal.setW3(root.elementText("W3"));
//						terminal.setW4(root.elementText("W4"));
//						terminal.setW5(root.elementText("W5"));
//						terminal.setW6(root.elementText("W6"));
//						terminal.setW7(root.elementText("W7"));
//
//						terminal.setD1(root.elementText("D1"));
//						terminal.setD2(root.elementText("D2"));
//						terminal.setD3(root.elementText("D3"));
//						terminal.setD4(root.elementText("D4"));
//						terminal.setD5(root.elementText("D5"));
//						terminal.setD6(root.elementText("D6"));
//						terminal.setD7(root.elementText("D7"));
//						terminal.setD8(root.elementText("D8"));
//
//						terminal.setG1(root.elementText("G1"));
//						terminal.setG2(root.elementText("G2"));
//						terminal.setG3(root.elementText("G3"));
//						terminal.setG4(root.elementText("G4"));
//						terminal.setG5(root.elementText("G5"));
//
//						terminal.setTimeMode(root.elementText("TimeMode"));
//						terminal.setDownloadTime(root.elementText("DownloadTime"));
//
//					}
//					if (num.equals("1")) {
//						terminal.setTimeShow(root.elementText("TimeShow") == null ? StringUtils.EMPTY : root
//								.elementText("TimeShow"));
//						terminal.setVolume(root.elementText("Volume") == null ? StringUtils.EMPTY : root
//								.elementText("Volume"));
//						terminal.setBrightnessBL(root.elementText("BrightnessBL") == null ? StringUtils.EMPTY : root
//								.elementText("BrightnessBL"));
//						terminal.setBrightnessPIC(root.elementText("BrightnessPIC") == null ? StringUtils.EMPTY : root
//								.elementText("BrightnessPIC"));
//						terminal.setContrast(root.elementText("Contrast") == null ? StringUtils.EMPTY : root
//								.elementText("Contrast"));
//						terminal.setSaturation(root.elementText("Saturation") == null ? StringUtils.EMPTY : root
//								.elementText("Saturation"));
//						terminal.setDayNum(root.elementText("DayNum") == null ? StringUtils.EMPTY : root
//								.elementText("DayNum"));
//						terminal.setDirDay(root.elementText("DirDay") == null ? StringUtils.EMPTY : root
//								.elementText("DirDay"));
//						terminal.setMode(root.elementText("Mode") == null ? StringUtils.EMPTY : root
//								.elementText("Mode"));
//
//						TerminalNotImdStatManager.instance().produce(channel);
//
//						if (!msgId.startsWith("system")) {
//							Map<String, String> map = new HashMap<String, String>();
//							map.put("projector", terminal.getProjector());
//							map.put("projectorScreen", terminal.getProjectorScreen());
//							map.put("sync", terminal.getSync());
//							map.put("preDwn", terminal.getPreDwn());
//							map.put("p2p", terminal.getP2p());
//							map.put("NkVersion", terminal.getNkVersion());
//							map.put("AppVersion", terminal.getAppVersion());
//							map.put("DevType", terminal.getDevType());
//							map.put("NetType", terminal.getNetType());
//							map.put("Dhcp", terminal.getDhcp());
//							map.put("DevIp", terminal.getDevIp());
//							map.put("DevSubMask", terminal.getDevSubMask());
//							map.put("DevGateWay", terminal.getDevGateWay());
//							map.put("WlanName", terminal.getWlanName());
//							map.put("WlanEncryption", terminal.getWlanEncryption());
//							map.put("WlanPasswd", terminal.getWlanPasswd());
//							map.put("McsAddr", terminal.getMcsAddr());
//							map.put("McsDomain", terminal.getMcsDomain());
//							map.put("McsPort", terminal.getMcsPort());
//							map.put("SubAddr", terminal.getSubAddr());
//							map.put("SubDomain", terminal.getSubDomain());
//							map.put("SubPort", terminal.getSubPort());
//							map.put("MainFtpAddr", terminal.getMainFtpAddr());
//							map.put("MainFtpDomain", terminal.getMainFtpDomain());
//							map.put("MainFtpPort", terminal.getMainFtpPort());
//							map.put("MainFtpUser", terminal.getMainFtpUser());
//							map.put("MainFtpPasswd", terminal.getMainFtpPasswd());
//							map.put("SubFtpAddr", terminal.getSubFtpAddr());
//							map.put("SubFtpDomain", terminal.getSubFtpDomain());
//							map.put("SubFtpPort", terminal.getSubFtpPort());
//							map.put("SubFtpUser", terminal.getSubFtpUser());
//							map.put("SubFtpPasswd", terminal.getSubFtpPasswd());
//							map.put("TrackAddr", terminal.getTrackAddr());
//							map.put("TrackPort", terminal.getTrackPort());
//							map.put("SatAddr", terminal.getSatAddr());
//							map.put("DwnSpeed", terminal.getDwnSpeed());
//							map.put("Sec1Start", terminal.getSec1Start());
//							map.put("Sec2Start", terminal.getSec2Start());
//							map.put("Sec3Start", terminal.getSec3Start());
//							map.put("Sec4Start", terminal.getSec4Start());
//							map.put("Sec5Start", terminal.getSec5Start());
//							map.put("Sec1End", terminal.getSec1End());
//							map.put("Sec2End", terminal.getSec2End());
//							map.put("Sec3End", terminal.getSec3End());
//							map.put("Sec4End", terminal.getSec4End());
//							map.put("Sec5End", terminal.getSec5End());
//							map.put("W1Start", terminal.getW1().split("-")[0]);
//							map.put("W2Start", terminal.getW2().split("-")[0]);
//							map.put("W3Start", terminal.getW3().split("-")[0]);
//							map.put("W4Start", terminal.getW4().split("-")[0]);
//							map.put("W5Start", terminal.getW5().split("-")[0]);
//							map.put("W6Start", terminal.getW6().split("-")[0]);
//							map.put("W7Start", terminal.getW7().split("-")[0]);
//							map.put("W1End", terminal.getW1().split("-")[1]);
//							map.put("W2End", terminal.getW2().split("-")[1]);
//							map.put("W3End", terminal.getW3().split("-")[1]);
//							map.put("W4End", terminal.getW4().split("-")[1]);
//							map.put("W5End", terminal.getW5().split("-")[1]);
//							map.put("W6End", terminal.getW6().split("-")[1]);
//							map.put("W7End", terminal.getW7().split("-")[1]);
//							Calendar now = Calendar.getInstance();
//							map.put("D1Date",
//									terminal.getD1().split(" ")[0].equals("00-00") ? "" : now.get(Calendar.YEAR) + "-"
//											+ terminal.getD1().split(" ")[0]);
//							map.put("D2Date",
//									terminal.getD2().split(" ")[0].equals("00-00") ? "" : now.get(Calendar.YEAR) + "-"
//											+ terminal.getD2().split(" ")[0]);
//							map.put("D3Date",
//									terminal.getD3().split(" ")[0].equals("00-00") ? "" : now.get(Calendar.YEAR) + "-"
//											+ terminal.getD3().split(" ")[0]);
//							map.put("D4Date",
//									terminal.getD4().split(" ")[0].equals("00-00") ? "" : now.get(Calendar.YEAR) + "-"
//											+ terminal.getD4().split(" ")[0]);
//							map.put("D5Date",
//									terminal.getD5().split(" ")[0].equals("00-00") ? "" : now.get(Calendar.YEAR) + "-"
//											+ terminal.getD5().split(" ")[0]);
//							map.put("D6Date",
//									terminal.getD6().split(" ")[0].equals("00-00") ? "" : now.get(Calendar.YEAR) + "-"
//											+ terminal.getD6().split(" ")[0]);
//							map.put("D7Date",
//									terminal.getD7().split(" ")[0].equals("00-00") ? "" : now.get(Calendar.YEAR) + "-"
//											+ terminal.getD7().split(" ")[0]);
//							map.put("D8Date",
//									terminal.getD8().split(" ")[0].equals("00-00") ? "" : now.get(Calendar.YEAR) + "-"
//											+ terminal.getD8().split(" ")[0]);
//							map.put("D1Start", terminal.getD1().split(" ")[1].split("-")[0]);
//							map.put("D2Start", terminal.getD2().split(" ")[1].split("-")[0]);
//							map.put("D3Start", terminal.getD3().split(" ")[1].split("-")[0]);
//							map.put("D4Start", terminal.getD4().split(" ")[1].split("-")[0]);
//							map.put("D5Start", terminal.getD5().split(" ")[1].split("-")[0]);
//							map.put("D6Start", terminal.getD6().split(" ")[1].split("-")[0]);
//							map.put("D7Start", terminal.getD7().split(" ")[1].split("-")[0]);
//							map.put("D8Start", terminal.getD8().split(" ")[1].split("-")[0]);
//							map.put("D1End", terminal.getD1().split(" ")[1].split("-")[1]);
//							map.put("D2End", terminal.getD2().split(" ")[1].split("-")[1]);
//							map.put("D3End", terminal.getD3().split(" ")[1].split("-")[1]);
//							map.put("D4End", terminal.getD4().split(" ")[1].split("-")[1]);
//							map.put("D5End", terminal.getD5().split(" ")[1].split("-")[1]);
//							map.put("D6End", terminal.getD6().split(" ")[1].split("-")[1]);
//							map.put("D7End", terminal.getD7().split(" ")[1].split("-")[1]);
//							map.put("D8End", terminal.getD8().split(" ")[1].split("-")[1]);
//							map.put("G1Start", terminal.getG1().split("-")[0]);
//							map.put("G2Start", terminal.getG2().split("-")[0]);
//							map.put("G3Start", terminal.getG3().split("-")[0]);
//							map.put("G4Start", terminal.getG4().split("-")[0]);
//							map.put("G5Start", terminal.getG5().split("-")[0]);
//							map.put("G1End", terminal.getG1().split("-")[1]);
//							map.put("G2End", terminal.getG2().split("-")[1]);
//							map.put("G3End", terminal.getG3().split("-")[1]);
//							map.put("G4End", terminal.getG4().split("-")[1]);
//							map.put("G5End", terminal.getG5().split("-")[1]);
//							map.put("TimeMode", terminal.getTimeMode());
//							map.put("DownloadTime", terminal.getDownloadTime());
//							map.put("TimeShow", terminal.getTimeShow());
//							map.put("Volume", terminal.getVolume());
//							map.put("BrightnessBL", terminal.getBrightnessBL());
//							map.put("BrightnessPIC", terminal.getBrightnessPIC());
//							map.put("Contrast", terminal.getContrast());
//							map.put("Saturation", terminal.getSaturation());
//							map.put("DayNum", terminal.getDayNum());
//							map.put("DirDay", terminal.getDirDay());
//							map.put("Mode", terminal.getMode());
//							map.put("Ulog", terminal.getUlog());
//							map.put("thirdPath", terminal.getThirdPath());
//							map.put("thirdTimeout", terminal.getThirdTimeout());
//							map.put("thirdVersion", terminal.getThirdVersion());
//
//							IntrmClient.instance().sendMsgReturn(msgId.substring(0, msgId.length() - 1),
//									JSONObject.fromObject(map).toString());
//						}
//					}
//				}
//				break;
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 28: {
                    JSONObject obj = new JSONObject();
                    obj.put("devType", root.elementText("DevType"));
                    obj.put("netType", root.elementText("NetType"));
                    obj.put("dhcp", root.elementText("DHCP"));
                    obj.put("ip", root.elementText("DevIP"));
                    obj.put("subMask", root.elementText("DevSubMask"));
                    obj.put("gateway", root.elementText("DevGateWay"));
                    obj.put("wlanName", root.elementText("WLanName"));
                    obj.put("wlanEnc", root.elementText("WLanEncryption"));
                    obj.put("wlanPassWd", root.elementText("WLanPasswd"));
                    terminal.setDevNet(obj.toString());
                    updateTerminalSetting(terminal.getId(), TerminalSettingEnum.DEVNET, obj.toString());
                }
                break;
                case 29: {
                    terminal.setUlog(root.elementText("Ulog"));
                }
                break;
                case 30: {
                    JSONObject obj = new JSONObject();
                    obj.put("mIp", root.elementText("MCSAddr"));
                    obj.put("mDomain", root.elementText("MCSDomain"));
                    obj.put("mPort", root.elementText("MCSPort"));
                    obj.put("sIp", root.elementText("SubAddr"));
                    obj.put("sDomain", root.elementText("SubDomain"));
                    obj.put("sPort", root.elementText("SubPort"));
                    obj.put("mFtp", root.elementText("MainFtpAddr"));
                    obj.put("mFDomain", root.elementText("MainFtpDomain"));
                    obj.put("mFPort", root.elementText("MainFtpPort"));
                    obj.put("mFUser", root.elementText("MainFtpUser"));
                    obj.put("mFPass", root.elementText("MainFtpPasswd"));
                    obj.put("sFtp", root.elementText("SubFtpAddr"));
                    obj.put("sFDomain", root.elementText("SubFtpDomain"));
                    obj.put("sFPort", root.elementText("SubFtpPort"));
                    obj.put("sFUser", root.elementText("SubFtpUser"));
                    obj.put("sFPass", root.elementText("SubFtpPasswd"));
                    obj.put("tAddr", root.elementText("TrackAddr"));
                    obj.put("tPort", root.elementText("TrackPort"));
                    obj.put("satAddr", root.elementText("SATAddr"));

                    terminal.setServerInfo(obj.toString());
                    updateTerminalSetting(terminal.getId(), TerminalSettingEnum.SERVER, obj.toString());
                    userCommander.setReturn(id, obj.toString());
                }
                break;
                case 31: {
                    JSONObject obj = new JSONObject();
//                    obj.put("ftpSpeed", root.elementText("FTPSpeed"));
                    JSONArray day = new JSONArray();
                    for (int i = 1; i <= 5; i++) {
                        String on = root.elementText("On" + i);
                        String off = root.elementText("Off" + i);
                        if (StrUtil.isEmpty(on)) {
                            on = "00:00:00";
                        }
                        if (StrUtil.isEmpty(off)) {
                            off = "00:00:00";
                        }
                        day.put(on + "-" + off);
                    }
                    obj.put("day", day);

                    JSONArray week = new JSONArray();
                    for (int i = 1; i <= 7; i++) {
                        String onoff = root.elementText("W" + i);
                        if (StrUtil.isEmpty(onoff)) {
                            onoff = "00:00-00:00";
                        }
                        week.put(onoff);
                    }
                    obj.put("week", week);

                    JSONArray holiday = new JSONArray();
                    for (int i = 1; i <= 7; i++) {
                        String onoff = root.elementText("D" + i);
                        if (StrUtil.isEmpty(onoff)) {
                            onoff = "00-00 00:00-00:00";
                        }
                        holiday.put(onoff);
                    }
                    obj.put("holiday", holiday);

//                    JSONArray g = new JSONArray();
//                    for (int i = 1; i <= 5; i++) {
//                        String onoff = root.elementText("G" + i);
//                        if (StrUtil.isEmpty(onoff)) {
//                            onoff = "00:00-00:00";
//                        }
//                        g.put(onoff);
//                    }
//                    obj.put("g", g);

//                    obj.put("tMode", root.elementText("TimeMode"));
//                    obj.put("dTime", root.elementText("DownloadTime"));

                    terminal.setOnOffInfo(obj.toString());
                    updateTerminalSetting(terminal.getId(), TerminalSettingEnum.ONOFF, obj.toString());
                    userCommander.setReturn(id, obj.toString());
                }
                break;
                case 32: {
                    JSONObject obj = new JSONObject();
                    obj.put("timeShow", root.elementText("TimeShow"));
                    obj.put("volume", root.elementText("Volume"));
                    obj.put("bl", root.elementText("BrightnessBL"));
                    obj.put("pic", root.elementText("BrightnessPIC"));
                    obj.put("contrast", root.elementText("Contrast"));
                    obj.put("saturation", root.elementText("Saturation"));
                    obj.put("dayNum", root.elementText("DayNum"));
                    obj.put("dirDay", root.elementText("DirDay"));
                    obj.put("mode", root.elementText("Mode"));

                    terminal.setPlayControl(obj.toString());
                    updateTerminalSetting(terminal.getId(), TerminalSettingEnum.PLAYCONTROL, obj.toString());
                }
                break;
                case 33:
                    userCommander.setReturn(id, errCode);
                    break;
                case 34: {
                    String time = root.elementText("DevTime");
                    userCommander.setReturn(id, time);
                }
                break;
                case 35:
                    userCommander.setReturn(id, errCode);
                    break;
                case 36:
                    userCommander.setReturn(id, errCode);
                    break;
                case 37: {
                    String fileType = root.elementText("FileType");
                    int pageNO = Integer.parseInt(root.elementText("PageNO"));
                    int pageNum = Integer.parseInt(root.elementText("PageNum"));
                    if (pageNO == 1) {
                        terminal.getFileList().clear();
                    }

                    int n = 1;
                    String name;
                    while ((name = root.elementText("F" + n)) != null) {
                        terminal.getFileList().add(name);
                        n++;
                    }

                    if (pageNO < pageNum) {
                        channel.writeAndFlush(Dir.CT_GETTERFILE(new String[]{id, fileType,
                                String.valueOf(pageNO + 1)}));
                    } else {
                        JSONArray files = new JSONArray(terminal.getFileList());
                        userCommander.setReturn(id, files.toString());
                    }
                }
                break;
                case 38:
                    userCommander.setReturn(id, errCode);
                    break;
                case 39:
                    userCommander.setReturn(id, errCode);
                    break;
                case 40:
                    userCommander.setReturn(id, errCode);
                    break;
                case 41:
                    userCommander.setReturn(id, errCode);
                    break;
                case 42:
                    userCommander.setReturn(id, errCode);
                    break;
                case 43:
                    userCommander.setReturn(id, errCode);
                    break;
                case 44:
                    userCommander.setReturn(id, errCode);
                    break;
                case 45:
                    userCommander.setReturn(id, errCode);
                    break;
                case 46:
                    errCode = root.elementText("ErrCode");
                    if (errCode != null && errCode.equals("0")) {
                        int pageNO = Integer.parseInt(root.elementText("PageNO"));
                        int pageNum = Integer.parseInt(root.elementText("PageNum"));

                        if (pageNO == 1) {
                            terminal.getTaskFileList().clear();
                        }

                        int n = 1;
                        String name;
                        String status;
                        while ((name = root.elementText("F" + n)) != null) {
                            status = root.elementText("S" + n);
                            terminal.getTaskFileList().add(new String[]{name, status});
                            n++;
                        }

                        if (pageNO < pageNum) {
                            channel.writeAndFlush(Dir.CT_TASKFILESTATUS(new String[]{id, String.valueOf(pageNO + 1)}));
                        } else {
                            JSONArray list = new JSONArray(terminal.getTaskFileList());
                            userCommander.setReturn(id, list.toString());
                        }
                    }
                    break;
                case 47:
                    userCommander.setReturn(id, errCode);
                    break;
                case 49:
                    userCommander.setReturn(id, errCode);
                    break;
                case 50:
                    userCommander.setReturn(id, errCode);
                    break;
                case 51:
                    userCommander.setReturn(id, errCode);
                    break;
                case 52:
                    userCommander.setReturn(id, errCode);
                    break;
                case 53:
                    userCommander.setReturn(id, errCode);
                case 54:
                    userCommander.setReturn(id, errCode);
                case 55:
                    userCommander.setReturn(id, errCode);
                    break;
                case 56: {
                    Element tasksEle = root.element("Tasks");
                    List<Map<String, String>> mapList = new ArrayList<>();
                    List<Element> taskEleList = tasksEle.elements("Task");
                    for (Element taskEle : taskEleList) {
                        Map<String, String> map = new HashMap<>();
                        String name = taskEle.elementText("Name");
                        String time = taskEle.elementText("Time");
                        map.put("name", name);
                        map.put("time", time);
                        mapList.add(map);
                    }
                    JSONArray list = new JSONArray(mapList);
                    userCommander.setReturn(id, list.toString());
                }
                break;
                case 57:
                    userCommander.setReturn(id, errCode);
                case 58: {
                    String result = root.elementText("Result");
                    userCommander.setReturn(id, result);
                }
                break;
                case 61: {
                    Element ele = root.element("Flow");
                    if (ele != null) {
                        String iccid = ele.elementText("Iccid");
                        String rx = ele.elementText("Rx");
                        String tx = ele.elementText("Tx");
                        if (!StrUtil.isEmpty(iccid)) {
                            JSONObject obj = new JSONObject();
                            obj.put("iccid", iccid);
                            obj.put("rx", rx);
                            obj.put("tx", tx);
                            terminal.setIccidInfo(obj.toString());
                        }
                    }
                }
                break;
                case 62:
                    userCommander.setReturn(id, errCode);
                    break;
                case 63:
                    userCommander.setReturn(id, errCode);
                    break;
//			case 64:
//				errCode = root.elementText("ErrCode");
//				if (errCode.equals("0")) {
//					Element list = root.element("List");
//					boolean playRecord = false;
//					List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
//					if (list != null) {
//						List<Element> mediaList = (List<Element>) list.elements("M");
//
//						if (mediaList != null) {
//							for (Element media : mediaList) {
//								Map<String, String> map = new HashMap<String, String>();
//								String name = media.elementText("N");
//								String count = media.elementText("C");
//								String date = media.elementText("D");
//								map.put("name", name);
//								map.put("count", count);
//								if (!StringTools.isEmptyString(date)) {
//									playRecord = true;
//									map.put("date", date);
//								}
//								mapList.add(map);
//							}
//						}
//					}
//					if (playRecord) {
//						TerminalPlayRecordManager.instance().produce(terminal.getId(), mapList);
//					} else {
//						IntrmClient.instance().sendMsgReturn(id, JSONArray.fromObject(mapList).toString());
//					}
//				}
//				break;
                case 65:
                    userCommander.setReturn(id, errCode);
                    break;
                case 66:
                    userCommander.setReturn(id, errCode);
                    break;
                case 67:
                    userCommander.setReturn(id, errCode);
                    break;
                case 68:
                    userCommander.setReturn(id, errCode);
                    break;
                case 69: {
                    Element hanWangEle = root.element("HanWang");
                    if (hanWangEle != null) {
                        JSONObject obj = new JSONObject();
                        obj.put("r", hanWangEle.elementText("Purify"));
                        obj.put("pm", hanWangEle.elementText("Pm25"));
                        obj.put("rh", hanWangEle.elementText("TempMos"));
                        obj.put("ha", hanWangEle.elementText("AutoSta"));
                        obj.put("title", hanWangEle.elementText("Title"));
                        obj.put("enable", hanWangEle.elementText("Enable"));
                        userCommander.setReturn(id, obj.toString());
                    }
                }
                break;
                case 71:
                    userCommander.setReturn(id, errCode);
                    break;
                case 72:
                    userCommander.setReturn(id, errCode);
                    break;
                case 73: {
                    String camera = root.elementText("Camera");
                    String wifi = root.elementText("Wifi");
                    JSONObject obj = new JSONObject();
                    obj.put("camera", StrUtil.isEmpty(camera) ? "" : camera);
                    obj.put("wifi", StrUtil.isEmpty(wifi) ? "" : wifi);
                    userCommander.setReturn(id, obj.toString());
                }
                break;
                case 74:
                    userCommander.setReturn(id, errCode);
                    break;
                case 75: {
                    String password = root.elementText("Password");
                    JSONObject obj = new JSONObject();
                    obj.put("password", StrUtil.isEmpty(password) ? "" : password);
                    userCommander.setReturn(id, obj.toString());
                }
                break;
                case 87:
                    userCommander.setReturn(id, errCode);
                    break;
                default:
                    break;
            }
        }

    }

    private String[] validate(String msg) throws Exception {
        Document doc = saxReader.read(new ByteArrayInputStream((msg).getBytes("UTF-8")));
        Element root = doc.getRootElement();

        String id = root.elementText("ID");
        String cmdType = root.elementText("CmdType");

        if (id != null && cmdType != null) {
            if (Integer.parseInt(cmdType) == 2) {
                String codeResult = root.elementText("CodeResult");
                String userName = root.elementText("UserName");
                String passwd = root.elementText("Passwd");

                if (codeResult != null && codeResult.equals("ok") && userName != null && userName.equals("admin")
                        && passwd != null && passwd.equals("123456")) {

                    String mac = root.elementText("MacAddr");
                    String nkVersion = root.elementText("NKVersion");
                    String appVersion = root.elementText("AppVersion");
                    String thirdVersion = root.elementText("ThirdVersion");
                    String dev = root.elementText("Dev");

                    if (StrUtil.isEmpty(nkVersion)) {
                        nkVersion = "";
                    }
                    if (StrUtil.isEmpty(appVersion)) {
                        appVersion = "";
                    }
                    if (StrUtil.isEmpty(thirdVersion)) {
                        thirdVersion = "";
                    }
                    if (StrUtil.isEmpty(dev)) {
                        dev = "";
                    }

                    return new String[]{mac, nkVersion, appVersion, thirdVersion, dev};
                }
            }
        }

        return null;
    }

    private void updateTerminalSetting(Long tId, TerminalSettingEnum terminalSettingEnum, String settings) {
        TerminalSetting terminalSetting = new TerminalSetting();
        com.seeyoo.mps.model.Terminal terminal = new com.seeyoo.mps.model.Terminal();
        terminal.setId(tId);
        TerminalSettingKey terminalSettingKey = new TerminalSettingKey(terminal, terminalSettingEnum);
        terminalSetting.setTerminalSettingKey(terminalSettingKey);
        terminalSetting.setSettings(settings);
        terminalSettingRepository.save(terminalSetting);
    }


    @Setter
    @Getter
    @AllArgsConstructor
    class Message {
        Channel channel = null;
        String msg = null;
    }

}