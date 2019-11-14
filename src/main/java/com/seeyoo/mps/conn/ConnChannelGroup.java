package com.seeyoo.mps.conn;

import com.seeyoo.mps.config.Parameter;
import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.sql.Timestamp;

import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConnChannelGroup extends DefaultChannelGroup {

    @Autowired
    TerminalOnlineProcesser onlineProcesser;

    @Autowired
    TerminalOfflineProcesser offlineProcesser;

    public ConnChannelGroup() {
        super(GlobalEventExecutor.INSTANCE);
    }

    public boolean register(Channel channel, String[] dataG) {
        try {
            if (dataG != null) {
                Attribute<Object> attribute = channel.attr(AttributeKey.valueOf("terminal"));
                Terminal terminal = new Terminal();
                terminal.setConnectTime(new Timestamp(System.currentTimeMillis()));
                terminal.setMac(dataG[0]);
                terminal.setNkVersion(dataG[1]);
                terminal.setAppVersion(dataG[2]);
                terminal.setThirdVersion(dataG[3]);
                terminal.setDev(dataG[4]);
                attribute.set(terminal);
                if( this.size()>= Parameter.MaxConnection){
                    log.info("[{}] --- 超过最大连接", dataG[0]);
                    channel.close();
                    return false;
                }
                onlineProcesser.produce(channel);
                return super.add(channel);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //log.info("[{}] --- 连接失败", dataG[0]);
        channel.close();
        return false;
    }


    @Override
    public boolean remove(Object o) {
        try {
            Channel channel = (Channel) o;
            Attribute<Object> attribute = channel.attr(AttributeKey.valueOf("terminal"));
            Terminal terminal = (Terminal) attribute.get();
            terminal.setDisconnectTime(new Timestamp(System.currentTimeMillis()));

            offlineProcesser.produce(channel);

//            System.out.println("[" + terminal.getMac() + "] --- 断开连接");
        } catch (InterruptedException e) {
        }

        return super.remove(o);
    }

    public Channel getChannel(long id) {
        for (Channel c : this) {
            Attribute<Object> attribute = c.attr(AttributeKey.valueOf("terminal"));
            Terminal terminal = (Terminal) attribute.get();
            if (terminal.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public Terminal getTerminal(long id) {
        for (Channel c : this) {
            Attribute<Object> attribute = c.attr(AttributeKey.valueOf("terminal"));
            Terminal terminal = (Terminal) attribute.get();
            if (terminal.getId().longValue() == id) {
                return terminal;
            }
        }
        return null;
    }

    public boolean writeAndFlush(long id, String msg) {
        for (Channel c : this) {
            Attribute<Object> attribute = c.attr(AttributeKey.valueOf("terminal"));
            Terminal terminal = (Terminal) attribute.get();
            if(terminal.getId()!= null){
                if (terminal.getId().longValue() == id) {
                    c.writeAndFlush(msg);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean writeAndFlush(String mac, String msg) {
        for (Channel c : this) {
            Attribute<Object> attribute = c.attr(AttributeKey.valueOf("terminal"));
            Terminal terminal = (Terminal) attribute.get();
            if (terminal.getMac().equals(mac)) {
                c.writeAndFlush(msg);
                return true;
            }
        }
        return false;
    }
}
