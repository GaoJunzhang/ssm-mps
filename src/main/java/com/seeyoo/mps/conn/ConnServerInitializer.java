package com.seeyoo.mps.conn;

import com.seeyoo.mps.service.SystemSettingService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.codec.xml.XmlFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConnServerInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    SystemSettingService systemSettingService;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        int writeIdle = 30;
        ChannelPipeline pipeline = ch.pipeline();
        if (systemSettingService != null) {
            String idle = systemSettingService.getSetting("conn_heartbeat", "30");
            try {
                writeIdle = Integer.valueOf(idle);
            } catch (Exception e) {

            }
        }

        pipeline.addLast("framer", new XmlFrameDecoder(10240));
        pipeline.addLast("encoder", new StringEncoder());
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("idleStateHandler", new IdleStateHandler(60, writeIdle, 0));
        pipeline.addLast(new ConnServerHandler());
    }
}