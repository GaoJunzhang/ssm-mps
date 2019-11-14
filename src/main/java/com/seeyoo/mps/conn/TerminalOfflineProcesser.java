package com.seeyoo.mps.conn;

import com.seeyoo.mps.dao.TerminalDurationRepository;
import com.seeyoo.mps.dao.TerminalRepository;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
@Slf4j
public class TerminalOfflineProcesser implements Runnable {

    @Autowired
    TerminalRepository terminalRepository;

    @Autowired
    TerminalDurationRepository terminalDurationRepository;

    private BlockingQueue<Channel> queue = new ArrayBlockingQueue<Channel>(3000);

    private List<Channel> channelList = new ArrayList<Channel>();

    public TerminalOfflineProcesser() {
        new Thread(this).start();
    }

    public void produce(Channel channel) throws InterruptedException {
        if (!queue.offer(channel)) {
            System.out.println("TerminalOfflineManager length = " + queue.size());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                offlineProc(queue.take());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void offlineProc(Channel channel) throws Exception {
        channelList.add(channel);

        if (queue.size() == 0 || channelList.size() >= 500) {
            Set<Long> tIds = new HashSet<>();
            List<Object[]> params = new ArrayList<>();
            for (Channel c : channelList) {
                try {
                    Attribute<Object> attribute = c.attr(AttributeKey.valueOf("terminal"));
                    Terminal terminal = (Terminal) attribute.get();
                    if (terminal.getId() != null && terminal.getId().longValue() > 0) {
                        tIds.add(terminal.getId());
//                        terminalRepository.updateTerminalDisconnectTime(terminal.getId(), terminal.getDisconnectTime());

                        Timestamp connectTime = terminal.getConnectTime();
                        Timestamp disconnectTime = terminal.getDisconnectTime();

                        Date startDate = connectTime;
                        Date endDate = disconnectTime;
                        List<Date> dateList = dateSplit(startDate, endDate);
                        for (Date date : dateList) {
                            long start = startDate.getTime();
                            long end = endDate.getTime();
                            if (date.compareTo(startDate) == 0) {
                                start = connectTime.getTime();
                            }
                            if (date.compareTo(endDate) == 0) {
                                end = disconnectTime.getTime();
                            }

                            Long second = (end - start) / 1000;

                            params.add(new Object[]{terminal.getId(), date, second.intValue(),second.intValue()});
//                            terminalDurationRepository.updateTerminalDuration(terminal.getId(), date, second.intValue());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            terminalRepository.updateTerminalDisconnectTimes(tIds);
            String sql = "insert into terminal_duration (terminal_id, date, duration) values (?, ?, ?) on duplicate key update duration = duration + ?";

            terminalDurationRepository.batchUpdate(sql, params);
            channelList.clear();
        }

    }

    private List<Date> dateSplit(Date startDate, Date endDate) {
        Long spi = endDate.getTime() - startDate.getTime();
        Long step = spi / (24 * 60 * 60 * 1000);

        List<Date> dateList = new ArrayList<Date>();
        dateList.add(endDate);
        for (int i = 1; i <= step; i++) {
            dateList.add(new Date(dateList.get(i - 1).getTime() - (24 * 60 * 60 * 1000)));
        }
        return dateList;
    }
}
