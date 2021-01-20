package com.redis.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {

    public final static int CAHCETIME = 60;// 默认缓存时间 60S
    public final static int CAHCEHOUR = 60 * 60;// 默认缓存时间 1hr
    public final static int CAHCEDAY = 60 * 60 * 24;// 默认缓存时间 1Day
    public final static int CAHCEWEEK = 60 * 60 * 24 * 7;// 默认缓存时间 1week
    public final static int CAHCEMONTH = 60 * 60 * 24 * 7 * 30;// 默认缓存时间 1month
    public final static int CAHCEYEAR = 60 * 60 * 24 * 7 * 30 * 12;// 默认缓存时间 1年

    private static long expiresIn = TimeUnit.SECONDS.toSeconds(CAHCEHOUR * 8);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // =============================common============================

    /**
     * 获取所有的key
     *
     * @return
     */
    public Set<String> GetAllKeys() {
        return redisTemplate.keys("*");
    }

    /**
     * 返回 key 的剩余的过期时间
     *
     * @param key
     * @return
     */
    public Long GetLiveTime(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 删除指定key
     *
     * @param key
     */
    public void Remove(String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 删除全部redis
     */
    public void RemoveAll() {
        Set<String> keys = redisTemplate.keys("*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 指定缓存失效时间
     *
     * @param key
     * @param time
     * @return
     */
    public void expire(String key, long time) {
        if (time > 0) {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
        } else {
            redisTemplate.expire(key, expiresIn, TimeUnit.SECONDS);
        }
    }

    /**
     * 插入缓存(无时间)
     *
     * @param key
     * @param object
     */
    public void Insert(String key, Object object) {
        if (object instanceof Map) {
            redisTemplate.opsForHash().putAll(key, (Map<String, String>) object);
        } else if (object instanceof List) {
            redisTemplate.opsForList().rightPush(key, object);
        } else if (object instanceof Set) {
            redisTemplate.opsForSet().add(key, object);
        } else {
            redisTemplate.opsForValue().set(key, JSON.toJSONString(object));
        }
        expire(key, 0);
    }

    /**
     * 插入缓存(有时间)
     *
     * @param key
     * @param object
     */
    public void Insert(String key, Object object, long time) {
        if (object instanceof Map) {
            redisTemplate.opsForHash().putAll(key, (Map<String, String>) object);
        } else if (object instanceof List) {
            redisTemplate.opsForList().rightPush(key, object);
        } else if (object instanceof Set) {
            redisTemplate.opsForSet().add(key, object);
        } else {
            redisTemplate.opsForValue().set(key, JSON.toJSONString(object));
        }
        expire(key, time);
    }


    /**
     * 修改key
     *
     * @param oldKey 旧的key
     * @param newKey 新的key
     */
    public void rename(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * 返回key存储的类型
     *
     * @param key
     */
    public String getType(String key) {
        return redisTemplate.type(key).code();
    }

    // ============================String=============================

    /**
     * 获取redis的String值
     *
     * @param key
     * @return
     */
    public Object GetString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // ============================Map=============================

    /**
     * 判断hash表中是否有对应的value
     *
     * @param hashId
     * @param key
     * @return
     */
    public boolean HasKey(String hashId, String key) {
        return redisTemplate.opsForHash().hasKey(hashId, key);
    }

    /**
     * 获取hashKey对应的所有键
     *
     * @param hashId 键
     */
    public List<String> GetHashKeys(String hashId) {
        List<String> list = new ArrayList<>();
        Map<Object, Object> map = this.GetMap(hashId);
        for (Object object : map.keySet()) {
            if (object instanceof String) {
                list.add(String.valueOf(object));
            }
        }
        return list;
    }

    private void setDataBase(int num) {
        LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
        if (connectionFactory != null && num != connectionFactory.getDatabase()) {
            connectionFactory.setDatabase(num);
            this.redisTemplate.setConnectionFactory(connectionFactory);
            connectionFactory.resetConnection();
        }
    }

    /**
     * 获取hashKey对应的所有值
     *
     * @param hashId 键
     */
    public List<String> GetHashValues(String hashId) {
        List<String> list = new ArrayList<>();
        Map<Object, Object> map = this.GetMap(hashId);
        for (Object object : map.keySet()) {
            if (map.get(object) instanceof String) {
                list.add(String.valueOf(map.get(object)));
            }
        }
        return list;
    }

    /**
     * 查询具体map的值
     *
     * @param hashId
     * @param key
     * @return
     */
    public String GetHashValues(String hashId, String key) {
        Object object = redisTemplate.opsForHash().get(hashId, key);
        if (object != null) {
            return String.valueOf(object);
        } else {
            return null;
        }
    }

    /**
     * 删除指定map的key
     *
     * @param key
     */
    public void RemoveHash(String hashId, String key) {
        if (HasKey(hashId, key)) {
            redisTemplate.opsForHash().delete(hashId, key);
        }
    }

    /**
     * 获取所有的map缓存
     *
     * @param key
     * @return
     */
    public Map<Object, Object> GetMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 插入map的值
     *
     * @param hashId 主键id
     * @param key    map的key
     * @param value  map的值
     */
    public void InsertHash(String hashId, String key, String value) {
        redisTemplate.opsForHash().put(hashId, key, value);
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> GetSet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error(key, e);
            return null;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long GetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始 0 是第一个元素
     * @param end   结束 -1代表所有值
     * @return
     * @取出来的元素 总数 end-start+1
     */
    public List<Object> Get(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error(key, e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long GetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error(key, e);
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object GetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error(key, e);
            return null;
        }
    }



}
