package com.seeyoo.mps.integration.shiro;

import com.seeyoo.mps.integration.redis.RedisByteManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class ShiroRedisSessionDao extends AbstractSessionDAO {
    @Autowired
    RedisByteManager redisManager;

    @Value("${shiro.session.timeout}")
    int sessionTimeout;

    private final String SHIRO_SESSIOM_PREFIX = "shiro-session:";

    private String getKey(String key) {
        return SHIRO_SESSIOM_PREFIX + key;
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        saveSession(session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            return null;
        }
        String key = getKey(sessionId.toString());
        byte[] value = redisManager.getObject(key);
        if (value == null)
            return null;
        return (Session) SerializationUtils.deserialize(value);
    }


    @Override
    public void update(Session session) throws UnknownSessionException {
        saveSession(session);

    }

    private void saveSession(Session session) {
        if (session != null && session.getId() != null) {
            String key = getKey(session.getId().toString());
            byte[] value = SerializationUtils.serialize(session);
            redisManager.setObject(key, value, sessionTimeout*60);
        }
    }

    @Override
    public void delete(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }
        String key = getKey(session.getId().toString());
        redisManager.deleteKey(key);
    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<String> keys = redisManager.getObjects(SHIRO_SESSIOM_PREFIX);
        Set<Session> sessions = new HashSet<>();
        if (CollectionUtils.isEmpty(keys)) {
            return sessions;
        }
        for (String key : keys) {
            Session session = (Session) SerializationUtils.deserialize( redisManager.getObject(key));
            sessions.add(session);
        }
        return sessions;
    }
}
