package com.seeyoo.mps.conn;

import com.seeyoo.mps.dao.TerminalRepository;
import com.seeyoo.mps.model.Tgroup;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.common.base.Joiner;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class TerminalOnlineProcesser implements Runnable {

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private TerminalInitProcesser initProcesser;

    private ArrayBlockingQueue<Channel> queue = new ArrayBlockingQueue<>(3000);

    private List<Channel> channelList = new ArrayList<>();

    public TerminalOnlineProcesser() {
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
                channelList.clear();
                e.printStackTrace();
            }
        }
    }

    @Transactional
    public void onlineProc(Channel channel) throws Exception {
        channelList.add(channel);

        if (queue.size() == 0 || channelList.size() >= 100) {
            Set<Long> tIds = new HashSet<>();
            for (Channel c : channelList) {
                Attribute<Object> attribute = c.attr(AttributeKey.valueOf("terminal"));
                Terminal terminal = (Terminal) attribute.get();
                String mac = terminal.getMac();

                com.seeyoo.mps.model.Terminal example = new com.seeyoo.mps.model.Terminal();
                example.setMac(mac);
                com.seeyoo.mps.model.Terminal t = terminalRepository.findOne(Example.of(example)).orElse(null);
                if (t != null) {
                    terminal.setId(t.getId());
                    terminal.setName(t.getName());

                } else {
                    example.setName(mac);
                    Tgroup tgroup = new Tgroup();
                    tgroup.setId(2l);
                    example.setTgroup(tgroup);
                    example.setImdUpdateTime(new Timestamp(System.currentTimeMillis()));
                    example.setConnectStatus((short) 1);
                    example.setConnectTime(new Timestamp(System.currentTimeMillis()));
                    example.setAdCount(0);
                    example.setAdUseCount(0);
                    example.setIsDelete((short) 0);
                    terminalRepository.save(example);

                    terminal.setId(example.getId());
                    terminal.setName(example.getName());
                }

                tIds.add(terminal.getId());
            }
            terminalRepository.updateTerminalOnlines(tIds);

            for (Channel c : channelList) {
                initProcesser.produce(c);
            }

            channelList.clear();
        }
    }
}
