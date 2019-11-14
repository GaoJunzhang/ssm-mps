package com.seeyoo.mps.conn;

import com.seeyoo.mps.integration.mq.RabbitMqManager;
import com.seeyoo.mps.integration.redis.RedisObjectManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class UserCommander {
    @Autowired
    RabbitMqManager rabbitMqManager;

    @Autowired
    RedisObjectManager redisManager;

    public void send(Long id, String mac, String name, String cmdId, String oprNo, short cmdNo, String sendContent, Long userId) {
        Command command = new Command(id, mac, name, cmdId, oprNo, cmdNo, sendContent, userId);
        redisManager.setObject(cmdId, command);
        rabbitMqManager.sendCmd(cmdId);
    }

    public boolean setSend(String cmdId) {
        Object obj = redisManager.getObject(cmdId);
        if (obj != null) {
            Command command = (Command) obj;
            command.setSendStatus((short) 1);
            command.setSendTime(new Timestamp(System.currentTimeMillis()));
            redisManager.setObject(cmdId, command);
            return true;
        }
        return false;
    }

    public boolean setReturn(String cmdId, String content) {
        Object obj = redisManager.getObject(cmdId);
        if (obj != null) {
            Command command = (Command) obj;
            command.setReturnStatus((short) 1);
            command.setReturnTime(new Timestamp(System.currentTimeMillis()));
            command.setReturnContent(content);
            redisManager.setObject(cmdId, command);
            return true;
        }
        return false;
    }

    public int getCount(String oprNo) {
        return redisManager.getObjects(oprNo).size();
    }

    public int getNotSendCount(String oprNo) {
        int count = 0;
        Set<String> keys = redisManager.getObjects(oprNo);
        for (String key : keys) {
            Object obj = redisManager.getObject(key);
            if (obj != null) {
                Command command = (Command) obj;
                if (command.sendStatus == 0) {
                    count++;
                }
            }

        }
        return count;
    }

    public int getSendCount(String oprNo) {
        int count = 0;
        Set<String> keys = redisManager.getObjects(oprNo);
        for (String key : keys) {
            Object obj = redisManager.getObject(key);
            if (obj != null) {
                Command command = (Command) obj;
                if (command.sendStatus == 1 && command.returnStatus != 1) {
                    count++;
                }
            }

        }
        return count;
    }

    public int getReturnCount(String oprNo) {
        int count = 0;
        Set<String> keys = redisManager.getObjects(oprNo);
        for (String key : keys) {
            Object obj = redisManager.getObject(key);
            if (obj != null) {
                Command command = (Command) obj;
                if (command.returnStatus == 1) {
                    count++;
                }
            }

        }
        return count;
    }

    public List<Command> getCommands(String oprNo) {
        List<Command> commands = new ArrayList<>();
        Set<String> keys = redisManager.getObjects(oprNo);
        for (String key : keys) {
            Object obj = redisManager.getObject(key);
            if (obj != null) {
                Command command = (Command) obj;
                commands.add(command);
                if (commands.size() >= 100) {
                    break;
                }
            }

        }
        return commands;
    }

    public String take(String msgId) {
        String content = "";
        try {
            for (int i = 0; i < 50; i++) {
                Thread.sleep(200);
                {
                    Object obj = redisManager.getObject(msgId);
                    if (obj != null) {
                        Command command = (Command) obj;
                        if (command != null && command.getReturnStatus() == 1) {
                            return command.getReturnContent();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
        }
        return content;
    }
}
