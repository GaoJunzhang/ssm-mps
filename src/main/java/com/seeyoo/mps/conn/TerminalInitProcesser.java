package com.seeyoo.mps.conn;

import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.dao.TerminalRepository;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class TerminalInitProcesser implements Runnable {

    @Autowired
    TerminalRepository terminalRepository;

    private ArrayBlockingQueue<Channel> queue = new ArrayBlockingQueue<Channel>(3000);

    private List<Channel> channelList = new ArrayList<Channel>();

    public TerminalInitProcesser() {
        new Thread(this).start();
    }

    public void produce(Channel channel) throws InterruptedException {
        if (!queue.offer(channel)) {
            System.out.println("TerminalOnlineManager length = " + queue.size());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                onlineProc(queue.take());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onlineProc(Channel channel) throws Exception {
        channelList.add(channel);

        if (queue.size() == 0 || channelList.size() >= 100) {
            List<Object[]> params = new ArrayList<>();
            for (Channel c : channelList) {
                Attribute<Object> attribute = c.attr(AttributeKey.valueOf("terminal"));
                Terminal terminal = (Terminal) attribute.get();
                Long id = terminal.getId();
                String serverIp = InetAddress.getLocalHost().getHostAddress();
                String serverMac = getMac();
                String appVersion = terminal.getAppVersion();
                String systemVersion = terminal.getNkVersion();

                if (!StrUtil.isEmpty(appVersion) && !StrUtil.isEmpty(systemVersion)) {
                    params.add(new Object[]{id, serverIp, serverMac, appVersion, systemVersion});
                }
//                    terminalRepository.updateTerminalInitInfo(id, serverIp, serverMac, appVersion, systemVersion);
            }

            String sql = "update terminal t set t.server_ip = ?, t.server_mac = ?, t.app_version = ?, t.system_version = ? where t.id = ?";
            terminalRepository.batchUpdate(sql, params);
            channelList.clear();
        }
    }

    private String getMac() throws IOException {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < mac.length; i++) {
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }

            return sb.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
