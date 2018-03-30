package com.maywidehb.qrpush.config;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
public class RedisConfig {

    private static Logger logger = Logger.getLogger(RedisConfig.class);

    private static JedisPool jpool = null;
    static {
        try{
            if(jpool == null){
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMinIdle(CF.redis.minIdle);
                config.setMaxIdle(CF.redis.maxIdle);
                config.setMaxWaitMillis(CF.redis.maxWait);
                config.setMaxTotal(CF.redis.maxTotal);
                config.setTestOnBorrow(true);
//                jpool = new JedisPool(config,CF.redis.host,CF.redis.port,CF.redis.timeout);
                jpool = new JedisPool(config,CF.redis.host,CF.redis.port,CF.redis.timeout,CF.redis.password);

            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public static Jedis getJedis(){
        try{
            if(jpool != null){
                return jpool.getResource();
            }else{
                return null;
            }
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static void returnResource(Jedis jedis){
        try{
            if(jpool != null){
                jpool.returnBrokenResource(jedis);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     @Bean("jedis.config")
     public JedisPoolConfig getRedisConfig(){
     JedisPoolConfig config = new JedisPoolConfig();
     config.setMinIdle(minIdle);
     config.setMaxIdle(maxIdle);
     config.setMaxWaitMillis(maxWaitMillis);
     config.setMaxTotal(maxTotal);
     config.setTestOnBorrow(testOnBorrow);
     return config;
     }

     @Bean
     public JedisPool jedisPool(){
     JedisPoolConfig config = getRedisConfig();
     JedisPool pool = new JedisPool(config,host,port);
     logger.info("init JredisPool ...");
     return pool;
     }
     */
}
