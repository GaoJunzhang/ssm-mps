package com.seeyoo.mps.conn;

import com.seeyoo.mps.config.Parameter;
import com.seeyoo.mps.tool.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnServerHandler extends SimpleChannelInboundHandler<String> {

	MessageProcessor messageProcessor = SpringUtil.getBean(MessageProcessor.class);

	ConnChannelGroup channels = SpringUtil.getBean(ConnChannelGroup.class);

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		if (channels.size() <= Parameter.MaxConnection) {
			ctx.writeAndFlush(Dir.CT_AUTH(new String[]{"0", "ok", Parameter.WebUrl}));
		} else {
			log.error("connection limit:{}", Parameter.MaxConnection);
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
//		log.info(msg);
		messageProcessor.produce(ctx.channel(), msg);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				//System.out.println("读超时");
				ctx.close();
			}
			if (e.state() == IdleState.WRITER_IDLE) {
				Channel channel = ctx.channel();
				channel.writeAndFlush(Dir.CT_IMDSTAT(new String[] { channel.id() + "20" }));
			}

		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//		cause.printStackTrace();
		ctx.close();
	}
}