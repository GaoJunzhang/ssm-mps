package com.seeyoo.mps.integration.redis;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisByteManager {

    @Resource(name = "ByteRedisTemplate")
    private RedisTemplate<String, byte[]> redisTemplate;

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    public void setObject(String key, byte[] value) {
        setObject(key, value, 60, TimeUnit.SECONDS);
    }

    public void setObject(String key, byte[] value, int timeout) {
        setObject(key, value, timeout, TimeUnit.SECONDS);
    }

    public void setObject(String key, byte[] value, int timeout, TimeUnit timeUnit) {
        if (StrUtil.isEmpty(key) || value == null)
            return;

        redisTemplate.boundValueOps(key).set(value, timeout, timeUnit);
    }

    public byte[] getObject(String key) {
        return redisTemplate.boundValueOps(key).get();
    }

    public Set<String> getObjects(String key) {
        return redisTemplate.keys(key+"*");
    }

    public Long getObjectExpire(String key) {
        return redisTemplate.boundValueOps(key).getExpire();
    }


    public RedisTemplate getRedisTemplate(){
        return redisTemplate;
    }

}