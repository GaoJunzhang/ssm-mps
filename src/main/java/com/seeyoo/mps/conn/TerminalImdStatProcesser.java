package com.seeyoo.mps.conn;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.TerminalRepository;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
@Slf4j
public class TerminalImdStatProcesser implements Runnable {

    @Autowired
    TerminalRepository terminalRepository;

    private BlockingQueue<Message> queue = new ArrayBlockingQueue<>(30000);

    SAXReader saxReader = null;
    private final List<Message> messageList = new ArrayList<>();

    public TerminalImdStatProcesser() {
        saxReader = new SAXReader();
        saxReader.setEncoding("UTF-8");
        new Thread(this).start();
    }

    public void produce(Channel channel, String msg) throws InterruptedException {
        if (!queue.offer(new Message(channel, msg))) {
            System.out.println("FaultProcesser length = " + queue.size());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                messageProc(queue.take());
            } catch (Exception e) {
            }
        }
    }

    @Transactional
    public void messageProc(Message message) throws Exception {
        messageList.add(message);

        if (queue.size() == 0 || messageList.size() >= 500) {
            List<Object[]> params = new ArrayList<>();
            for (Message m : messageList) {
                try {
                    String msg = m.getMsg();
                    Channel channel = m.getChannel();

                    Attribute<Object> attribute = channel.attr(AttributeKey.valueOf("terminal"));
                    Terminal terminal = (Terminal) attribute.get();
                    Document doc = saxReader.read(new ByteArrayInputStream((msg).getBytes("UTF-8")));
                    Element root = doc.getRootElement();

                    String devState = root.elementText("DevState");
                    String dlFileSize = root.elementText("DLFileSize");
                    String useableSpace = root.elementText("UseableSpace");
                    String diskSpace = root.elementText("DiskSpace");
                    String playStatus = root.elementText("PlayStatus");
                    String taskName = root.elementText("TaskName");
                    String secondaryTaskName = root.elementText("SecTaskName");
                    String templateID = root.elementText("TemplateID");
                    String secondaryTemplateID = root.elementText("SecTemplateID");
                    String blight = root.elementText("BLight");
                    String fileStatus = root.elementText("FileStatus");

                    terminal.setDevState(StrUtil.nullToEmpty(devState));
                    terminal.setDlFileSize(StrUtil.nullToEmpty(dlFileSize));
                    terminal.setUseableSpace(StrUtil.nullToEmpty(useableSpace));
                    terminal.setDiskSpace(StrUtil.nullToEmpty(diskSpace));
                    terminal.setPlayStatus(StrUtil.nullToEmpty(playStatus));
                    terminal.setTaskName(StrUtil.nullToEmpty(taskName));
                    terminal.setSecondaryTaskName(StrUtil.nullToEmpty(secondaryTaskName));
                    terminal.setTemplateID(StrUtil.nullToEmpty(templateID));
                    terminal.setSecondaryTemplateID(StrUtil.nullToEmpty(secondaryTemplateID));
                    terminal.setBlight(StrUtil.nullToEmpty(blight));
                    terminal.setFileStatus(StrUtil.nullToEmpty(fileStatus));
                    JSONObject playContentJO = new JSONObject();
                    try {
                        String winNum = root.elementText("WinNum");
                        if (winNum != null && NumberUtil.isInteger(winNum)) {
                            for (int n = 0; n < Integer.parseInt(winNum); n++) {
                                String winID = root.elementText("WinID" + (n + 1));
                                String file = root.elementText("File" + (n + 1));
                                if (winID != null && file != null) {
                                    if (file.lastIndexOf("_") > 0 && file.lastIndexOf(".") > 0) {
                                        file = file.substring(0, file.lastIndexOf("_")) + file.substring(file.lastIndexOf("."));
                                    }
                                    playContentJO.put(winID, file);
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                    terminal.setPlayContent(playContentJO.toString());

                    JSONObject secondaryPlayContentJO = new JSONObject();
                    String winNum = root.elementText("SecWinNum");
                    if (winNum != null && NumberUtil.isInteger(winNum)) {
                        for (int n = 0; n < Integer.parseInt(winNum); n++) {
                            String winID = root.elementText("SecWinID" + (n + 1));
                            String file = root.elementText("SecFile" + (n + 1));
                            if (winID != null && file != null) {
                                if (file.lastIndexOf("_") > 0 && file.lastIndexOf(".") > 0) {
                                    file = file.substring(0, file.lastIndexOf("_")) + file.substring(file.lastIndexOf("."));
                                }
                                secondaryPlayContentJO.put(winID, file);
                            }
                        }
                    }
                    terminal.setSecondaryPlayContent(secondaryPlayContentJO.toString());
                    params.add(new Object[]{terminal.getDevState(), terminal.getDlFileSize(), terminal.getUseableSpace(),
                            terminal.getDiskSpace(), terminal.getTaskName(), terminal.getPlayContent(),
                            terminal.getId()});
//                    terminalRepository.updateTerminalImdStat(terminal.getDevState(), terminal.getDlFileSize(), terminal.getUseableSpace(),
//                            terminal.getDiskSpace(), terminal.getTaskName(), terminal.getPlayContent(),
//                            terminal.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String sql = "update terminal t set t.dev_state = ?, t.dl_file_size = ?, t.useable_space = ?, t.disk_space = ?, t.task_name = ?, t.play_content = ?, t.imd_update_time = NOW() where t.id = ?";

            terminalRepository.batchUpdate(sql, params);
//            log.info("process imd {},{}", queue.size(), params.size());
            messageList.clear();
        }

    }


    class Message {
        Channel channel = null;
        String msg = null;

        public Message(Channel channel, String msg) {
            this.channel = channel;
            this.msg = msg;
        }

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
