package com.seeyoo.mps.integration.mq;

import com.seeyoo.mps.conn.Command;
import com.seeyoo.mps.conn.ConnChannelGroup;
import com.seeyoo.mps.conn.UserCommander;
import com.seeyoo.mps.integration.redis.RedisObjectManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMqManager {
    public static final String CmdExchange = "Cmd";

    public static final String TaskExchange = "Task";
    public static final String TaskQueue = "taskQueue";

    private final AmqpAdmin amqpAdmin;
    private final AmqpTemplate amqpTemplate;

    @Autowired
    RedisObjectManager redisManager;

    @Autowired
    ConnChannelGroup connChannelGroup;

    @Autowired
    UserCommander userCommander;

    @Autowired
    public RabbitMqManager(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
    }

    public void sendCmd(String uuid) {
        amqpTemplate.convertAndSend("Cmd", "", uuid);
    }

    public void sendTask(Long terminalId) {
        amqpTemplate.convertAndSend(TaskExchange, TaskQueue, terminalId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${mq.cmd.queue}", durable = "true"),
            exchange = @Exchange(value = CmdExchange, type = "fanout")
    ))
    public void processMessage(String content) {
        log.info("sendCmd:{}", content);
        try {
            Object obj = redisManager.getObject(content);
            if (obj != null) {
                Command command = (Command) obj;
                if (connChannelGroup.writeAndFlush(command.getId(), command.getSendContent())) {
                    userCommander.setSend(command.getCmdId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = TaskQueue, durable = "true"),
            exchange = @Exchange(value = TaskExchange, type = "fanout")
    ))
    public void processTask(Long terminalId) {
        log.info("processTask start:{}", terminalId);
    }


}
