package com.seeyoo.mps.integration.shiro;

import com.seeyoo.mps.integration.redis.RedisObjectManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class ShiroRedisCache<K, V> implements Cache<K, V> {
    @Autowired
    private RedisObjectManager redisObjectManager;

    private String SHIRO_SESSIOM_PREFIX = "shiro-redis";

    private String getKey(String key) {
        return SHIRO_SESSIOM_PREFIX +":" +key;
    }

    @Override
    public V get(K k) throws CacheException {
        if (k == null) {
            return null;
        }
        String key = getKey(""+k);
        return (V) redisObjectManager.getObject(key);

    }

    @Override
    public V put(K k, V v) throws CacheException {
        if (k == null || v == null) {
            return null;
        }

        String key = getKey(""+k);
        redisObjectManager.setObject(key, v, 10*60);
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        if (k == null) {
            return null;
        }
        String key = getKey(""+k);
        V v = (V) redisObjectManager.getObject(key);
        redisObjectManager.deleteKey(key);
        return v;
    }

    @Override
    public void clear() throws CacheException {
        redisObjectManager.getRedisTemplate().getConnectionFactory().getConnection().flushDb();

    }

    @Override
    public int size() {
        return redisObjectManager.getRedisTemplate().getConnectionFactory().getConnection().dbSize().intValue();
    }

    @Override
    public Set<K> keys() {
        Set<String> keys = redisObjectManager.getObjects(SHIRO_SESSIOM_PREFIX);
        Set<K> sets = new HashSet<>();
        for (String key : keys) {
            sets.add((K) key);
        }
        return sets;
    }

    @Override
    public Collection<V> values() {
        Set<K> keys = keys();
        List<V> values = new ArrayList<>(keys.size());
        for (K k : keys) {
            values.add(get(k));
        }
        return values;
    }

}