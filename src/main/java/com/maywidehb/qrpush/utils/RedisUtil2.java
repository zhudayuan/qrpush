package com.maywidehb.qrpush.utils;

import com.maywidehb.qrpush.config.RedisConfig;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


@Service
public class RedisUtil2 {

    public String get(String key) {
        Jedis jedis  =  RedisConfig.getJedis();
        String ret;
        try {
            ret = jedis.get(key);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return ret;
    }

    public boolean set(String key, String val) {
        Jedis jedis =  RedisConfig.getJedis();
        try {
            return "OK".equals(jedis.set(key, val));
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

}