package com.minister.component.redis.service.impl;

import cn.hutool.core.date.DateUtil;
import com.minister.component.redis.exception.RedisException;
import com.minister.component.redis.service.RedisStringService;
import com.minister.component.utils.JacksonUtil;
import com.minister.component.utils.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * RedisStringServiceImpl
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 12:02
 */
@Service
@Slf4j
public class RedisStringServiceImpl implements RedisStringService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public long del(String... key) {
        if (key == null || key.length <= 0) {
            throw new IllegalArgumentException("redis del key can not be null");
        }
        Arrays.asList(key).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis del key can not be null");
            }
        });
        try {
            if (key.length == 1) {
                Boolean result = stringRedisTemplate.delete(key[0]);
                if (result == null) {
                    log.info("redis del key : " + Arrays.toString(key) + " result is null");
                }
                return (result == null || !result) ? 0L : 1L;
            } else {
                Long result = stringRedisTemplate.delete(Arrays.asList(key));
                if (result == null) {
                    log.info("redis del key : " + Arrays.toString(key) + " result is null");
                }
                return result == null ? 0L : result;
            }
        } catch (Exception e) {
            log.error("redis del key : " + Arrays.toString(key) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public long del(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            throw new IllegalArgumentException("redis del keys can not be null");
        }
        keys.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis del key can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.delete(keys);
            if (result == null) {
                log.info("redis del keys : " + JacksonUtil.bean2Json(keys) + " result is null");
            }
            return result == null ? 0L : result;
        } catch (Exception e) {
            log.error("redis del keys : " + JacksonUtil.bean2Json(keys) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean exists(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis exists key can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.hasKey(key);
            if (result == null) {
                log.info("redis exists key : " + key + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis exists key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean expire(String key, long time) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis expire key can not be null");
        }
        if (time <= 0) {
            throw new IllegalArgumentException("redis expire time has to be greater than zero");
        }
        try {
            Boolean result = stringRedisTemplate.expire(key, time, TimeUnit.SECONDS);
            if (result == null) {
                log.info("redis expire key : " + key + ", time : " + time + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis expire key : " + key + ", time : " + time + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean expireAt(String key, Date date) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis expireAt key can not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("redis expireAt date can not be null");
        }
        if (!date.after(new Date())) {
            throw new IllegalArgumentException("redis expireAt date has to be after now date");
        }
        try {
            Boolean result = stringRedisTemplate.expireAt(key, date);
            if (result == null) {
                log.info("redis expireAt key : " + key + ", date : " + DateUtil.formatDateTime(date) + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis expireAt key : " + key + ", date : " + DateUtil.formatDateTime(date) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public long ttl(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis ttl key can not be null");
        }
        try {
            Long result = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (result == null) {
                log.info("redis ttl key : " + key + " result is null");
            }
            return result == null ? 0L : result;
        } catch (Exception e) {
            log.error("redis ttl key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            throw new IllegalArgumentException("redis keys pattern can not be null");
        }
        try {
            return stringRedisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("redis keys pattern : " + pattern + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean persist(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis persist key can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.persist(key);
            if (result == null) {
                log.info("redis persist key : " + key + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis persist key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void rename(String oldKey, String newKey) {
        if (StringUtils.isBlank(oldKey)) {
            throw new IllegalArgumentException("redis rename oldKey can not be null");
        }
        if (StringUtils.isBlank(newKey)) {
            throw new IllegalArgumentException("redis rename newKey can not be null");
        }
        try {
            stringRedisTemplate.rename(oldKey, newKey);
        } catch (Exception e) {
            log.error("redis rename oldKey : " + oldKey + ", newKey : " + newKey + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean renameNX(String oldKey, String newKey) {
        if (StringUtils.isBlank(oldKey)) {
            throw new IllegalArgumentException("redis renameNX oldKey can not be null");
        }
        if (StringUtils.isBlank(newKey)) {
            throw new IllegalArgumentException("redis renameNX newKey can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.renameIfAbsent(oldKey, newKey);
            if (result == null) {
                log.info("redis renameNX oldKey : " + oldKey + ", newKey : " + newKey + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis renameNX oldKey : " + oldKey + ", newKey : " + newKey + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public DataType type(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis type key can not be null");
        }
        try {
            return stringRedisTemplate.type(key);
        } catch (Exception e) {
            log.error("redis type key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

//    ==============================String==============================

    @Override
    public String get(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis get key can not be null");
        }
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("redis get key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public String getSet(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis getSet key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis getSet value can not be null");
        }
        try {
            return stringRedisTemplate.opsForValue().getAndSet(key, value);
        } catch (Exception e) {
            log.error("redis getSet key : " + key + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer incrBy(String key, int increment) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis incrBy key can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForValue().increment(key, increment);
            if (result == null) {
                log.info("redis incrBy(int) key : " + key + ", increment : " + increment + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis incrBy(int) key : " + key + ", increment : " + increment + "->fail", e);
            throw new RedisException(e);
        }
    }

    /**
     * 原子操作，自增并设置过期时间
     */
    private static final String INCR_BY_INT = "local v;" +
            "v=redis.call('INCRBY',KEYS[1],ARGV[1])\n" +
            "redis.call('expire',KEYS[1],ARGV[2])\n" +
            "return v;";

    @Override
    public Integer incrBy(String key, int increment, long time) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis incrBy key can not be null");
        }
        if (time <= 0) {
            throw new IllegalArgumentException("redis incrBy time has to be greater than zero");
        }
        try {
            DefaultRedisScript<Long> rs = new DefaultRedisScript<>(INCR_BY_INT, Long.class);
            List<String> keys = new ArrayList<String>() {{
                add(key);
            }};
            Object[] args = new String[]{String.valueOf(increment), String.valueOf(time)};
            Long result = stringRedisTemplate.execute(rs, keys, args);
            if (result == null) {
                log.info("redis incrBy(int) key : " + key + ", increment : " + increment + ", time : " + time + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis incrBy(int) key : " + key + ", increment : " + increment + ", time : " + time + "->fail");
            throw new RedisException(e);
        }
    }

    @Override
    public Double incrBy(String key, double increment) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis incrBy key can not be null");
        }
        try {
            Double result = stringRedisTemplate.opsForValue().increment(key, increment);
            if (result == null) {
                log.info("redis incrBy(double) key : " + key + ", increment : " + increment + " result is null");
            }
            return result;
        } catch (Exception e) {
            log.error("redis incrBy(double) key : " + key + ", increment : " + increment + "->fail", e);
            throw new RedisException(e);
        }
    }

    /**
     * 原子操作，自增并设置过期时间
     */
    private static final String INCR_BY_DOUBLE = "local v;" +
            "v=redis.call('INCRBYFLOAT',KEYS[1],ARGV[1])\n" +
            "redis.call('expire',KEYS[1],ARGV[2])\n" +
            "return v;";

    @Override
    public Double incrBy(String key, double increment, long time) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis incrBy key can not be null");
        }
        if (time <= 0) {
            throw new IllegalArgumentException("redis incrBy time has to be greater than zero");
        }
        try {
            DefaultRedisScript<Long> rs = new DefaultRedisScript<>(INCR_BY_DOUBLE, Long.class);
            List<String> keys = new ArrayList<String>() {{
                add(key);
            }};
            Object[] args = new String[]{String.valueOf(increment), String.valueOf(time)};
            Long result = stringRedisTemplate.execute(rs, keys, args);
            if (result == null) {
                log.info("redis incrBy(double) key : " + key + ", increment : " + increment + ", time : " + time + " result is null");
            }
            return result == null ? null : result.doubleValue();
        } catch (Exception e) {
            log.error("redis incrBy(double) key : " + key + ", increment : " + increment + ", time : " + time + "->fail");
            throw new RedisException(e);
        }
    }

    @Override
    public Integer append(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis append key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis append value can not be null");
        }
        try {
            Integer result = stringRedisTemplate.opsForValue().append(key, value);
            if (result == null) {
                log.info("redis append key : " + key + ", value : " + value + " result is null");
            }
            return result;
        } catch (Exception e) {
            log.error("redis append key : " + key + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public String getRange(String key, int start, int end) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis getRange key can not be null");
        }
        try {
            return stringRedisTemplate.opsForValue().get(key, start, end);
        } catch (Exception e) {
            log.error("redis getRange key : " + key + ", start : " + start + ", end : " + end + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Map<String, String> mGet(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            throw new IllegalArgumentException("redis mGet keys can not be null");
        }
        keys.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis mGet key can not be null");
            }
        });
        try {
            List<String> resultList = stringRedisTemplate.opsForValue().multiGet(keys);
            if (CollectionUtils.isEmpty(resultList)) {
                return Collections.emptyMap();
            }
            Map<String, String> resultMap = new HashMap<>(keys.size());
            int i = 0;
            for (String key : keys) {
                String obj = resultList.get(i++);
                if (obj != null) {
                    resultMap.put(key, obj);
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("redis mGet keys : " + JacksonUtil.bean2Json(keys) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void mSet(Map<String, String> map) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException("redis mSet map can not be null");
        }
        map.values().forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis mSet values can not be null");
            }
        });
        try {
            stringRedisTemplate.opsForValue().multiSet(map);
        } catch (Exception e) {
            log.error("redis mSet : " + map.toString() + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean mSetNX(Map<String, String> map) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException("redis mSetNX map can not be null");
        }
        map.values().forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis mSetNX values can not be null");
            }
        });
        try {
            Boolean result = stringRedisTemplate.opsForValue().multiSetIfAbsent(map);
            if (result == null) {
                log.info("redis mSetNX map : " + map.toString() + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis mSetNX map : " + map.toString() + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void set(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis set key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis set value can not be null");
        }
        try {
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("redis set key : " + key + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void set(String key, String value, long time) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis set key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis set value can not be null");
        }
        if (time <= 0) {
            throw new IllegalArgumentException("redis set time has to be greater than zero");
        }
        try {
            stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set key : " + key + ", value : " + value + ", time : " + time + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean setNX(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis setNX key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis setNX value can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value);
            if (result == null) {
                log.info("redis setNX key : " + key + ", value : " + value + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis setNX key : " + key + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    private static final String SET_NX = "local v;" +
            "v=redis.call('SETNX',KEYS[1],ARGV[1])\n" +
            "if v == 1 then\n" +
            "   redis.call('expire',KEYS[1],ARGV[2])\n" +
            "end\n" +
            "return v;";

    @Override
    public boolean setNX(String key, String value, long time) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis setNX key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis setNX value can not be null");
        }
        try {
            DefaultRedisScript<Boolean> rs = new DefaultRedisScript<>(SET_NX, Boolean.class);
            List<String> keys = new ArrayList<String>() {{
                add(key);
            }};
            Object[] args = new String[]{value, String.valueOf(time)};
            Boolean result = stringRedisTemplate.execute(rs, keys, args);
            if (result == null) {
                log.info("redis setNX key : " + key + ", value : " + value + ", time : " + time + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis setNX key : " + key + ", value : " + value + ", time : " + time + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void setRange(String key, int offset, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis setRange key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis setRange value can not be null");
        }
        try {
            stringRedisTemplate.opsForValue().set(key, value, offset);
        } catch (Exception e) {
            log.error("redis setRange key : " + key + ", offset : " + offset + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer strLen(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis strLen key can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForValue().size(key);
            if (result == null) {
                log.info("redis strLen key : " + key + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis strLen key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

//    ==============================Map==============================

    @Override
    public String hGet(String key, String field) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hGet key can not be null");
        }
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException("redis hGet field can not be null");
        }
        try {
            return stringRedisTemplate.<String, String>opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("redis hGet key : " + key + ", field : " + field + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean hExists(String key, String field) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hExists key can not be null");
        }
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException("redis hExists field can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.<String, String>opsForHash().hasKey(key, field);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("redis hExists key : " + key + ", field : " + field + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer hIncrBy(String key, String field, int increment) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hIncrBy key can not be null");
        }
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException("redis hIncrBy field can not be null");
        }
        try {
            Long result = stringRedisTemplate.<String, String>opsForHash().increment(key, field, increment);
            if (result == null) {
                log.info("redis hIncrBy key : " + key + ", field : " + field + ", increment : " + increment + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis hIncrBy(int) key : " + key + ", field : " + field + ", increment : " + increment + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Double hIncrBy(String key, String field, double increment) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hIncrBy key can not be null");
        }
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException("redis hIncrBy field can not be null");
        }
        try {
            Double result = stringRedisTemplate.<String, String>opsForHash().increment(key, field, increment);
            if (result == null) {
                log.info("redis hIncrBy key : " + key + ", field : " + field + ", increment : " + increment + " result is null");
            }
            return result;
        } catch (Exception e) {
            log.error("redis hIncrBy(double) key : " + key + ", field : " + field + ", increment : " + increment + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> hKeys(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hKeys key can not be null");
        }
        try {
            return stringRedisTemplate.<String, String>opsForHash().keys(key);
        } catch (Exception e) {
            log.error("redis hKeys key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer hLen(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hLen key can not be null");
        }
        try {
            Long result = stringRedisTemplate.<String, String>opsForHash().size(key);
            if (result == null) {
                log.info("redis hLen key : " + key + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis hLen key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void hMSet(String key, Map<String, String> map) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hMSet key can not be null");
        }
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException("redis hMSet map can not be null");
        }
        map.values().forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis hMSet values can not be null");
            }
        });
        try {
            stringRedisTemplate.<String, String>opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("redis hMSet key : " + key + ", map : " + map.toString() + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Map<String, String> hMGet(String key, Collection<String> fields) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hMGet key can not be null");
        }
        if (CollectionUtils.isEmpty(fields)) {
            throw new IllegalArgumentException("redis hMGet fields can not be null");
        }
        fields.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis hMGet field can not be null");
            }
        });
        try {
            List<String> resultList = stringRedisTemplate.<String, String>opsForHash().multiGet(key, fields);
            if (CollectionUtils.isEmpty(resultList)) {
                return Collections.emptyMap();
            }
            Map<String, String> resultMap = new HashMap<>(fields.size());
            int i = 0;
            for (String field : fields) {
                String obj = resultList.get(i++);
                if (obj != null) {
                    resultMap.put(field, obj);
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("redis hMGet key : " + key + ", fields : " + JacksonUtil.bean2Json(fields) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void hSet(String key, String field, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hSet key can not be null");
        }
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException("redis hSet field can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis hSet value can not be null");
        }
        try {
            stringRedisTemplate.<String, String>opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("redis hSet key : " + key + ", field : " + field + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean hSetNX(String key, String field, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hSetNX key can not be null");
        }
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException("redis hSetNX field can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis hSetNX value can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.<String, String>opsForHash().putIfAbsent(key, field, value);
            if (result == null) {
                log.info("redis hSetNX key : " + key + ", field : " + field + ", value : " + value + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis hSetNX key : " + key + ", field : " + field + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public List<String> hVals(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hVals key can not be null");
        }
        try {
            return stringRedisTemplate.<String, String>opsForHash().values(key);
        } catch (Exception e) {
            log.error("redis hVals key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer hDel(String key, String... field) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hDel key can not be null");
        }
        if (field == null || field.length <= 0) {
            throw new IllegalArgumentException("redis hDel field can not be null");
        }
        Arrays.asList(field).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis hDel field can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.<String, String>opsForHash().delete(key, (Object[]) field);
            if (result == null) {
                log.info("redis hDel key : " + key + ", field : " + Arrays.toString(field) + " result is null");
            }
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            log.error("redis hDel key : " + key + ", field : " + Arrays.toString(field) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer hDel(String key, Collection<String> fields) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hDel key can not be null");
        }
        if (CollectionUtils.isEmpty(fields)) {
            throw new IllegalArgumentException("redis hDel fields can not be null");
        }
        fields.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis hDel field can not be null");
            }
        });
        try {
            return this.hDel(key, fields.toArray(new String[0]));
        } catch (Exception e) {
            log.error("redis hDel key : " + key + ", fields : " + JacksonUtil.bean2Json(fields) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Map<String, String> hGetAll(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis hGetAll key can not be null");
        }
        try {
            return stringRedisTemplate.<String, String>opsForHash().entries(key);
        } catch (Exception e) {
            log.error("redis hGetAll key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

//    ==============================list==============================

    @Override
    public String lIndex(String key, int index) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lIndex key can not be null");
        }
        try {
            return stringRedisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("redis lIndex key : " + key + ", index : " + index + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public String lPop(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lPop key can not be null");
        }
        try {
            return stringRedisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("redis lPop key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer lPush(String key, String... value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lPush key can not be null");
        }
        if (value == null || value.length <= 0) {
            throw new IllegalArgumentException("redis lPush value can not be null");
        }
        Arrays.asList(value).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis lPush value can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForList().leftPushAll(key, value);
            if (result == null) {
                log.info("redis lPush key : " + key + ", value : " + Arrays.toString(value) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis lPush key : " + key + ", value : " + Arrays.toString(value) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer lPush(String key, Collection<String> values) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lPush key can not be null");
        }
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException("redis lPush values can not be null");
        }
        values.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis lPush value can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForList().leftPushAll(key, values);
            if (result == null) {
                log.info("redis lPush key : " + key + ", values : " + JacksonUtil.bean2Json(values) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis lPush key : " + key + ", values : " + JacksonUtil.bean2Json(values) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer lPushX(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lPushX key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis lPushX value can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForList().leftPushIfPresent(key, value);
            if (result == null) {
                log.info("redis lPushX key : " + key + ", value : " + value + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis lPushX key : " + key + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer lInsert(String key, Position position, String pivot, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lInsert key can not be null");
        }
        if (position == null) {
            throw new IllegalArgumentException("redis lInsert position can not be null");
        } else if (!Position.BEFORE.equals(position) && !Position.AFTER.equals(position)) {
            throw new IllegalArgumentException("redis lInsert position is error");
        }
        if (StringUtils.isBlank(pivot)) {
            throw new IllegalArgumentException("redis lInsert pivot can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis lInsert value can not be null");
        }
        try {
            Long result;
            if (Position.BEFORE.equals(position)) {
                result = stringRedisTemplate.opsForList().leftPush(key, pivot, value);
            } else {
                result = stringRedisTemplate.opsForList().rightPush(key, pivot, value);
            }
            if (result == null) {
                log.info("redis lPush key : " + key + ", position : " + position.name() + ", pivot : " + pivot + ", value : " + value + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis lPush key : " + key + ", position : " + position.name() + ", pivot : " + pivot + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer lLen(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lLen key can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForList().size(key);
            if (result == null) {
                log.info("redis lLen key : " + key + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis lLen key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public List<String> lRange(String key, int start, int end) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lRange key can not be null");
        }
        try {
            List<String> result = stringRedisTemplate.opsForList().range(key, start, end);
            if (result == null) {
                log.info("redis lRange key : " + key + ", start : " + start + ", end : " + end + " result is null");
            }
            return result == null ? Collections.emptyList() : result;
        } catch (Exception e) {
            log.error("redis lRange key : " + key + ", start : " + start + ", end : " + end + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer lRem(String key, int count, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lRem key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis lRem value can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForList().remove(key, count, value);
            if (result == null) {
                log.info("redis lRem key : " + key + ", count : " + count + ", value : " + value + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis lRem key : " + key + ", count : " + count + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public String rPop(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis rPop key can not be null");
        }
        try {
            return stringRedisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.error("redis rPop key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer rPush(String key, String... value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis rPush key can not be null");
        }
        if (value == null || value.length <= 0) {
            throw new IllegalArgumentException("redis rPush value can not be null");
        }
        Arrays.asList(value).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis rPush value can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForList().rightPushAll(key, value);
            if (result == null) {
                log.info("redis rPush key : " + key + ", value : " + Arrays.toString(value) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis rPush key : " + key + ", value : " + Arrays.toString(value) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer rPush(String key, Collection<String> values) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis rPush key can not be null");
        }
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException("redis rPush values can not be null");
        }
        values.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis rPush value can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForList().rightPushAll(key, values);
            if (result == null) {
                log.info("redis rPush key : " + key + ", values : " + JacksonUtil.bean2Json(values) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis rPush key : " + key + ", values : " + JacksonUtil.bean2Json(values) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer rPushX(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis rPushX key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis rPushX value can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForList().rightPushIfPresent(key, value);
            if (result == null) {
                log.info("redis rPushX key : " + key + ", value : " + value + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis rPushX key : " + key + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public String rPopLPush(String sourceKey, String destinationKey) {
        if (StringUtils.isBlank(sourceKey)) {
            throw new IllegalArgumentException("redis rPopLPush sourceKey can not be null");
        }
        if (StringUtils.isBlank(destinationKey)) {
            throw new IllegalArgumentException("redis rPopLPush destinationKey can not be null");
        }
        try {
            return stringRedisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
        } catch (Exception e) {
            log.error("redis rPopLPush sourceKey : " + sourceKey + ", destinationKey : " + destinationKey + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void lSet(String key, int index, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lSet key can not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("redis lSet value can not be null");
        }
        try {
            stringRedisTemplate.opsForList().set(key, index, value);
        } catch (Exception e) {
            log.error("redis lSet key : " + key + ", index : " + index + ", value : " + value + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public void lTrim(String key, int start, int end) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis lTrim key can not be null");
        }
        try {
            stringRedisTemplate.opsForList().trim(key, start, end);
        } catch (Exception e) {
            log.error("redis lTrim key : " + key + ", start : " + start + ", end : " + end + "->fail", e);
            throw new RedisException(e);
        }
    }

//    ==============================set==============================

    @Override
    public Integer sAdd(String key, String... value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sAdd key can not be null");
        }
        if (value == null || value.length <= 0) {
            throw new IllegalArgumentException("redis sAdd value can not be null");
        }
        Arrays.asList(value).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sAdd value can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForSet().add(key, value);
            if (result == null) {
                log.info("redis sAdd key : " + key + ", value : " + Arrays.toString(value) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis sAdd key : " + key + ", value : " + Arrays.toString(value) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sAdd(String key, Collection<String> values) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sAdd key can not be null");
        }
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException("redis sAdd values can not be null");
        }
        values.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sAdd value can not be null");
            }
        });
        try {
            return this.sAdd(key, values.toArray(new String[0]));
        } catch (Exception e) {
            log.error("redis sAdd key : " + key + ", value  : " + JacksonUtil.bean2Json(values) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> sDiff(String key, String... compareKey) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sDiff key can not be null");
        }
        if (compareKey == null || compareKey.length <= 0) {
            throw new IllegalArgumentException("redis sDiff compareKey can not be null");
        }
        Arrays.asList(compareKey).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sDiff compareKey can not be null");
            }
        });
        try {
            return this.sDiff(key, Arrays.asList(compareKey));
        } catch (Exception e) {
            log.error("redis sDiff key : " + key + ", compareKey : " + Arrays.toString(compareKey) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> sDiff(String key, Collection<String> compareKeys) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sDiff key can not be null");
        }
        if (CollectionUtils.isEmpty(compareKeys)) {
            throw new IllegalArgumentException("redis sDiff compareKeys can not be null");
        }
        compareKeys.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sDiff compareKey can not be null");
            }
        });
        try {
            Set<String> result = stringRedisTemplate.opsForSet().difference(key, compareKeys);
            if (result == null) {
                log.info("redis sDiff key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis sDiff key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sDiffStore(String destinationKey, String key, String... compareKey) {
        if (StringUtils.isBlank(destinationKey)) {
            throw new IllegalArgumentException("redis sDiffStore destinationKey can not be null");
        }
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sDiffStore key can not be null");
        }
        if (compareKey == null || compareKey.length <= 0) {
            throw new IllegalArgumentException("redis sDiffStore compareKey can not be null");
        }
        Arrays.asList(compareKey).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sDiffStore compareKey can not be null");
            }
        });
        try {
            return this.sDiffStore(destinationKey, key, Arrays.asList(compareKey));
        } catch (Exception e) {
            log.error("redis sDiffStore destinationKey : " + destinationKey + ", key : " + key + ", compareKey : " + Arrays.toString(compareKey) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sDiffStore(String destinationKey, String key, Collection<String> compareKeys) {
        if (StringUtils.isBlank(destinationKey)) {
            throw new IllegalArgumentException("redis sDiffStore destinationKey can not be null");
        }
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sDiffStore key can not be null");
        }
        if (CollectionUtils.isEmpty(compareKeys)) {
            throw new IllegalArgumentException("redis sDiffStore compareKeys can not be null");
        }
        compareKeys.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sDiffStore compareKey can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForSet().differenceAndStore(key, compareKeys, destinationKey);
            if (result == null) {
                log.info("redis sDiffStore destinationKey : " + destinationKey + ", key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis sDiffStore destinationKey : " + destinationKey + ", key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> sInter(String key, String... compareKey) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sInter key can not be null");
        }
        if (compareKey == null || compareKey.length <= 0) {
            throw new IllegalArgumentException("redis sInter compareKey can not be null");
        }
        Arrays.asList(compareKey).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sInter compareKey can not be null");
            }
        });
        try {
            return this.sInter(key, Arrays.asList(compareKey));
        } catch (Exception e) {
            log.error("redis sInter key : " + key + ", compareKey is null->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> sInter(String key, Collection<String> compareKeys) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sInter key can not be null");
        }
        if (CollectionUtils.isEmpty(compareKeys)) {
            throw new IllegalArgumentException("redis sInter compareKeys can not be null");
        }
        compareKeys.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sInter compareKey can not be null");
            }
        });
        try {
            Set<String> result = stringRedisTemplate.opsForSet().intersect(key, compareKeys);
            if (result == null) {
                log.info("redis sInter key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis sInter key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sInterStore(String destinationKey, String key, String... compareKey) {
        if (StringUtils.isBlank(destinationKey)) {
            throw new IllegalArgumentException("redis sInterStore destinationKey can not be null");
        }
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sInterStore key can not be null");
        }
        if (compareKey == null || compareKey.length <= 0) {
            throw new IllegalArgumentException("redis sInterStore compareKey can not be null");
        }
        Arrays.asList(compareKey).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sInterStore compareKey can not be null");
            }
        });
        try {
            return this.sInterStore(destinationKey, key, Arrays.asList(compareKey));
        } catch (Exception e) {
            log.error("redis sInterStore destinationKey : " + destinationKey + ", key : " + key + ", compareKey : " + Arrays.toString(compareKey) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sInterStore(String destinationKey, String key, Collection<String> compareKeys) {
        if (StringUtils.isBlank(destinationKey)) {
            throw new IllegalArgumentException("redis sInterStore destinationKey can not be null");
        }
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sInterStore key can not be null");
        }
        if (CollectionUtils.isEmpty(compareKeys)) {
            throw new IllegalArgumentException("redis sInterStore compareKeys can not be null");
        }
        compareKeys.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sInterStore compareKey can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForSet().intersectAndStore(key, compareKeys, destinationKey);
            if (result == null) {
                log.info("redis sInterStore destinationKey : " + destinationKey + ", key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis sInterStore destinationKey : " + destinationKey + ", key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Boolean sIsMember(String key, String member) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sIsMember key can not be null");
        }
        if (StringUtils.isBlank(member)) {
            throw new IllegalArgumentException("redis sIsMember member can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.opsForSet().isMember(key, member);
            if (result == null) {
                log.info("redis sIsMember key : " + key + ", member : " + member + " result is null");
            }
            return result;
        } catch (Exception e) {
            log.error("redis sIsMember key : " + key + ", member : " + member + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> sMembers(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sMembers key can not be null");
        }
        try {
            Set<String> result = stringRedisTemplate.opsForSet().members(key);
            if (result == null) {
                log.info("redis sMembers key : " + key + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis sMembers key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public boolean sMove(String sourceKey, String destinationKey, String member) {
        if (StringUtils.isBlank(sourceKey)) {
            throw new IllegalArgumentException("redis sMove sourceKey can not be null");
        }
        if (StringUtils.isBlank(destinationKey)) {
            throw new IllegalArgumentException("redis sMove destinationKey can not be null");
        }
        if (StringUtils.isBlank(member)) {
            throw new IllegalArgumentException("redis sMove member can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.opsForSet().move(sourceKey, member, destinationKey);
            if (result == null) {
                log.info("redis sMove sourceKey : " + sourceKey + ", destinationKey : " + destinationKey + ", member : " + member + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis sMove sourceKey : " + sourceKey + ", destinationKey : " + destinationKey + ", member : " + member + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public String sRandMember(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sRandMember key can not be null");
        }
        try {
            return stringRedisTemplate.opsForSet().randomMember(key);
        } catch (Exception e) {
            log.error("redis sRandMember key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> sRandMember(String key, int count) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sRandMember key can not be null");
        }
        if (count < 0) {
            throw new IllegalArgumentException("redis sRandMember count can not less than or equal to zero");
        }
        try {
            Set<String> result = stringRedisTemplate.opsForSet().distinctRandomMembers(key, count);
            if (result == null) {
                log.info("redis distinctRandomMembers key : " + key + ", count : " + count + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis distinctRandomMembers key : " + key + ", count : " + count + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public List<String> sRandMembers(String key, int count) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sRandMembers key can not be null");
        }
        if (count < 0) {
            throw new IllegalArgumentException("redis sRandMembers count can not less than or equal to zero");
        }
        try {
            List<String> result = stringRedisTemplate.opsForSet().randomMembers(key, count);
            if (result == null) {
                log.info("redis sRandMembers key : " + key + ", count : " + count + " result is null");
            }
            return result == null ? Collections.emptyList() : result;
        } catch (Exception e) {
            log.error("redis sRandMembers key : " + key + ", count : " + count + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sRem(String key, String... value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sRem key can not be null");
        }
        if (value == null || value.length <= 0) {
            throw new IllegalArgumentException("redis sRem value can not be null");
        }
        Arrays.asList(value).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sRem value can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForSet().remove(key, (Object[]) value);
            if (result == null) {
                log.info("redis sRem key : " + key + ", value : " + Arrays.toString(value) + " result is null");
            }
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            log.error("redis sRem key : " + key + ", value : " + Arrays.toString(value) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public String sPop(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sPop key can not be null");
        }
        try {
            return stringRedisTemplate.opsForSet().pop(key);
        } catch (Exception e) {
            log.error("redis sPop key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sCard(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sCard key can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForSet().size(key);
            if (result == null) {
                log.info("redis sCard key : " + key + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis sCard key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> sUnion(String key, String... compareKey) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sUnion key can not be null");
        }
        if (compareKey == null || compareKey.length <= 0) {
            throw new IllegalArgumentException("redis sUnion compareKey can not be null");
        }
        Arrays.asList(compareKey).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sUnion compareKey can not be null");
            }
        });
        try {
            return this.sUnion(key, Arrays.asList(compareKey));
        } catch (Exception e) {
            log.error("redis sUnion key : " + key + ", compareKey : " + Arrays.toString(compareKey) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> sUnion(String key, Collection<String> compareKeys) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sUnion key can not be null");
        }
        if (CollectionUtils.isEmpty(compareKeys)) {
            throw new IllegalArgumentException("redis sUnion compareKeys can not be null");
        }
        compareKeys.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sUnion compareKey can not be null");
            }
        });
        try {
            Set<String> result = stringRedisTemplate.opsForSet().union(key, compareKeys);
            if (result == null) {
                log.info("redis sUnion key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis sUnion key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sUnionStore(String destinationKey, String key, String... compareKey) {
        if (StringUtils.isBlank(destinationKey)) {
            throw new IllegalArgumentException("redis sUnionStore destinationKey can not be null");
        }
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sUnionStore key can not be null");
        }
        if (compareKey == null || compareKey.length <= 0) {
            throw new IllegalArgumentException("redis sUnionStore compareKey can not be null");
        }
        Arrays.asList(compareKey).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sUnionStore compareKey can not be null");
            }
        });
        try {
            return this.sUnionStore(destinationKey, key, Arrays.asList(compareKey));
        } catch (Exception e) {
            log.error("redis sUnionStore destinationKey : " + destinationKey + ", key : " + key + ", compareKey" + Arrays.toString(compareKey) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer sUnionStore(String destinationKey, String key, Collection<String> compareKeys) {
        if (StringUtils.isBlank(destinationKey)) {
            throw new IllegalArgumentException("redis sUnionStore destinationKey can not be null");
        }
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis sUnionStore key can not be null");
        }
        if (CollectionUtils.isEmpty(compareKeys)) {
            throw new IllegalArgumentException("redis sUnionStore compareKeys can not be null");
        }
        compareKeys.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis sUnionStore compareKey can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForSet().unionAndStore(key, compareKeys, destinationKey);
            if (result == null) {
                log.info("redis sUnionStore destinationKey : " + destinationKey + ", key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis sUnionStore destinationKey : " + destinationKey + ", key : " + key + ", compareKeys : " + JacksonUtil.bean2Json(compareKeys) + "->fail", e);
            throw new RedisException(e);
        }
    }

//    ==============================有序集合==============================

    @Override
    public boolean zAdd(String key, double score, String member) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zAdd key can not be null");
        }
        if (member == null) {
            throw new IllegalArgumentException("redis zAdd member can not be null");
        }
        try {
            Boolean result = stringRedisTemplate.opsForZSet().add(key, member, score);
            if (result == null) {
                log.info("redis zAdd key : " + key + ", score : " + score + ", member : " + member + " result is null");
            }
            return result == null ? false : result;
        } catch (Exception e) {
            log.error("redis zAdd key : " + key + ", score : " + score + ", member : " + member + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zAdd(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zAdd key can not be null");
        }
        if (CollectionUtils.isEmpty(tuples)) {
            throw new IllegalArgumentException("redis zAdd tuples can not be null");
        }
        tuples.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis zAdd tuple can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForZSet().add(key, tuples);
            if (result == null) {
                log.info("redis zAdd key : " + key + ", tuples : " + JacksonUtil.bean2Json(tuples) + " result is null");
            }
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            log.error("redis zAdd key : " + key + ", tuples : " + JacksonUtil.bean2Json(tuples) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Double zScore(String key, String member) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zScore key can not be null");
        }
        if (member == null) {
            throw new IllegalArgumentException("redis zScore member can not be null");
        }
        try {
            Double result = stringRedisTemplate.opsForZSet().score(key, member);
            if (result == null) {
                log.info("redis zScore key : " + key + ", member : " + member + " result is null");
            }
            return result;
        } catch (Exception e) {
            log.error("redis zScore key : " + key + ", member : " + member + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Double zIncrBy(String key, double increment, String member) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zIncrBy key can not be null");
        }
        if (member == null) {
            throw new IllegalArgumentException("redis zIncrBy member can not be null");
        }
        try {
            Double result = stringRedisTemplate.opsForZSet().incrementScore(key, member, increment);
            if (result == null) {
                log.info("redis zIncrBy key : " + key + ", increment : " + increment + ", member : " + member + " result is null");
            }
            return result;
        } catch (Exception e) {
            log.error("redis zIncrBy key : " + key + ", increment : " + increment + ", member : " + member + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zCard(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zScard key can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForZSet().size(key);
            if (result == null) {
                log.info("redis zScard key : " + key + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis zScard key : " + key + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zCount(String key, double min, double max) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zCount key can not be null");
        }
        if (MathUtil.compare(min, max) > 0) {
            throw new IllegalArgumentException("redis zCount max can not be less than min");
        }
        try {
            Long result = stringRedisTemplate.opsForZSet().count(key, min, max);
            if (result == null) {
                log.info("redis zCount key : " + key + ", min : " + min + ", max : " + max + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis zCount key : " + key + ", min : " + min + ", max : " + max + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> zRange(String key, int start, int stop) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRange key can not be null");
        }
        try {
            Set<String> result = stringRedisTemplate.opsForZSet().range(key, (long) start, (long) stop);
            if (result == null) {
                log.info("redis zRange key : " + key + ", start : " + start + ", stop : " + stop + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis zRange key : " + key + ", start : " + start + ", stop : " + stop + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> zRevRange(String key, int start, int stop) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRevRange key can not be null");
        }
        try {
            Set<String> result = stringRedisTemplate.opsForZSet().reverseRange(key, (long) start, (long) stop);
            if (result == null) {
                log.info("redis zRevRange key : " + key + ", start : " + start + ", stop : " + stop + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis zRevRange key : " + key + ", start : " + start + ", stop : " + stop + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> zRangeByScore(String key, double min, double max) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRevRange key can not be null");
        }
        if (MathUtil.compare(min, max) > 0) {
            throw new IllegalArgumentException("redis zRevRange max can not be less than min");
        }
        try {
            Set<String> result = stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
            if (result == null) {
                log.info("redis zRangeByScore key : " + key + ", min : " + min + ", max : " + max + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis zRangeByScore key : " + key + ", min : " + min + ", max : " + max + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> zRangeByScore(String key, double min, double max, int offset, int count) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRevRange key can not be null");
        }
        if (MathUtil.compare(min, max) > 0) {
            throw new IllegalArgumentException("redis zRevRange max can not be less than min");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("redis zRevRange count has to be greater than zero");
        }
        try {
            Set<String> result = stringRedisTemplate.opsForZSet().rangeByScore(key, min, max, offset, count);
            if (result == null) {
                log.info("redis zRangeByScore key : " + key + ", min : " + min + ", max : " + max + ", offset : " + offset + ", count : " + count + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis zRangeByScore key : " + key + ", min : " + min + ", max : " + max + ", offset : " + offset + ", count : " + count + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> zRevRangeByScore(String key, double min, double max) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRevRangeByScore key can not be null");
        }
        if (MathUtil.compare(min, max) > 0) {
            throw new IllegalArgumentException("redis zRevRangeByScore max can not be less than min");
        }
        try {
            Set<String> result = stringRedisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
            if (result == null) {
                log.info("redis zRevRangeByScore key : " + key + ", min : " + min + ", max : " + max + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis zRevRangeByScore key : " + key + ", min : " + min + ", max : " + max + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Set<String> zRevRangeByScore(String key, double min, double max, int offset, int count) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRevRangeByScore key can not be null");
        }
        if (MathUtil.compare(min, max) > 0) {
            throw new IllegalArgumentException("redis zRevRangeByScore max can not be less than min");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("redis zRevRangeByScore count has to be greater than zero");
        }
        try {
            Set<String> result = stringRedisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
            if (result == null) {
                log.info("redis zRevRangeByScore key : " + key + ", min : " + min + ", max : " + max + ", offset : " + offset + ", count : " + count + " result is null");
            }
            return result == null ? Collections.emptySet() : result;
        } catch (Exception e) {
            log.error("redis zRevRangeByScore key : " + key + ", min : " + min + ", max : " + max + ", offset : " + offset + ", count : " + count + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zRank(String key, String member) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRank key can not be null");
        }
        if (member == null) {
            throw new IllegalArgumentException("redis zRank member can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForZSet().rank(key, member);
            if (result == null) {
                log.info("redis zRank key : " + key + ", member : " + member + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis zRank key : " + key + ", member : " + member + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zRevRank(String key, String member) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRevRank key can not be null");
        }
        if (member == null) {
            throw new IllegalArgumentException("redis zRevRank member can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForZSet().reverseRank(key, member);
            if (result == null) {
                log.info("redis zRevRank key : " + key + ", member : " + member + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis zRevRank key : " + key + ", member : " + member + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zRem(String key, String... member) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRem key can not be null");
        }
        if (member == null || member.length <= 0) {
            throw new IllegalArgumentException("redis zRem member can not be null");
        }
        Arrays.asList(member).forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis zRem member can not be null");
            }
        });
        try {
            Long result = stringRedisTemplate.opsForZSet().remove(key, (Object[]) member);
            if (result == null) {
                log.info("redis zRem key : " + key + ", member : " + Arrays.toString(member) + " result is null");
            }
            return result == null ? null : result.intValue();
        } catch (Exception e) {
            log.error("redis zRem key : " + key + ", member : " + Arrays.toString(member) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zRem(String key, Collection<String> members) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRem key can not be null");
        }
        if (CollectionUtils.isEmpty(members)) {
            throw new IllegalArgumentException("redis zRem members can not be null");
        }
        members.forEach(s -> {
            if (s == null) {
                throw new IllegalArgumentException("redis zRem member can not be null");
            }
        });
        try {
            return this.zRem(key, members.toArray(new String[0]));
        } catch (Exception e) {
            log.error("redis zRem key : " + key + ", members : " + JacksonUtil.bean2Json(members) + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zRemRangeByRank(String key, int start, int stop) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRemRangeByRank key can not be null");
        }
        try {
            Long result = stringRedisTemplate.opsForZSet().removeRange(key, start, stop);
            if (result == null) {
                log.info("redis zRemRangeByRank key : " + key + ", start : " + start + ", stop : " + stop + " result is null");
            }
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            log.error("redis zRemRangeByRank key : " + key + ", start : " + start + ", stop : " + stop + "->fail", e);
            throw new RedisException(e);
        }
    }

    @Override
    public Integer zRemRangeByScore(String key, double min, double max) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("redis zRemRangeByScore key can not be null");
        }
        if (MathUtil.compare(min, max) > 0) {
            throw new IllegalArgumentException("redis zRemRangeByScore max can not be less than min");
        }
        try {
            Long result = stringRedisTemplate.opsForZSet().removeRangeByScore(key, min, max);
            if (result == null) {
                log.info("redis zRemRangeByScore key : " + key + ", min : " + min + ", max : " + max + " result is null");
            }
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            log.error("redis zRemRangeByScore key : " + key + ", min : " + min + ", max : " + max + "->fail", e);
            throw new RedisException(e);
        }
    }

}
