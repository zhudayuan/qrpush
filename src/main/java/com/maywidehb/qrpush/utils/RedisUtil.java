package com.maywidehb.qrpush.utils;

import com.maywidehb.qrpush.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.List;
import java.util.Map;


public class RedisUtil {
	// 不建议做更改 有些配置默认该库
	private static int curDB = 0;

	/**
	 * 添加单个 key - value
	 * @param key
	 * @param value
	 */
	public static void setKey(String key, String value){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(curDB);
		jedis.set(key, value);
		RedisConfig.returnResource(jedis);
	}

	/**
	 * 添加单个 key - value，指定DB
	 * @param key
	 * @param value
	 * @param db
	 */
	public static void setKey(String key, String value,int db){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(db);
		jedis.set(key, value);
		RedisConfig.returnResource(jedis);
	}

	/**
	 * 获取单个key值
	 * @param key
	 * @return
	 */
	public static String getKey(String key){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(curDB);
		String value = jedis.get(key);
		RedisConfig.returnResource(jedis);

		return value;
	}
	public static String getSet(String key,String v,int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		String value = jedis.getSet(key,v);
		RedisConfig.returnResource(jedis);

		return value;
	}
	/**
	 * Redis Incr 命令将 key 中储存的数字值增一。
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误
	 * @param key
	 * @return
	 */
	public static Long incr(String key,int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		Long value = jedis.incr(key);
		RedisConfig.returnResource(jedis);
		return value;
	}
	/**
	 * Redis Incr 命令将 key 中储存的数字值增Increment。
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误
	 * @param key
	 * @return
	 */
	public static Long incrBy(String key,long Increment,int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		Long value = jedis.incrBy(key,Increment);
		RedisConfig.returnResource(jedis);
		return value;
	}
	/**
	 * 取得key的过期时间 单位秒
	 * @param key
	 * @param dbidx
	 * @return -2 key不存在 -1 没有设置生存时间
	 */
	public static Long ttl(String key,int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		Long value = jedis.ttl(key);
		RedisConfig.returnResource(jedis);
		return value;
	}
	/**
	 * 设置key的过期时间 单位秒
	 * @param key
	 * @param seconds
	 * @param dbidx
	 * @return
	 */
	public static Long expire(String key, int seconds, int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		Long value = jedis.expire(key,seconds);
		RedisConfig.returnResource(jedis);
		return value;
	}
	public static String setex(String key, int seconds, String v,int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		String value = jedis.setex( key,  seconds, v);
		RedisConfig.returnResource(jedis);
		return value;
	}

	/**
     * 获取单个key值 指定数据库 dbidx
     * @param key
     * @return
     */
    public static String getKeyByDbidx(String key,int dbidx){
        Jedis jedis = RedisConfig.getJedis();
        jedis.select(dbidx);
        String value = jedis.get(key);
        RedisConfig.returnResource(jedis);

        return value;
    }
	/**
	 * 删除 单个 key
	 * @param key
	 */
	public static void delKey(String key){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(curDB);
		jedis.del(key);
		RedisConfig.returnResource(jedis);
	}
	/**
	 * 删除 单个 key 指定数据库 dbidx
	 * @param key
	 */
	public static void delKey(String key,int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		jedis.del(key);
		RedisConfig.returnResource(jedis);
	}

	/**
	 * 获取数据库key数量
	 * @param dbidx
	 * @return
	 */
	public static Long getDbSize(int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		Long dbsize = jedis.dbSize();
		RedisConfig.returnResource(jedis);
		return dbsize;
	}

	/**
	 * key-value 批量载入 Redis
	 * @param kvMap
	 */
	public static void pipeSetKeys(Map<String, String> kvMap){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(curDB);
		Pipeline p = jedis.pipelined();
		for(Map.Entry<String, String> entry : kvMap.entrySet()){
			p.set(entry.getKey(), entry.getValue());
		}
		p.exec();
		RedisConfig.returnResource(jedis);
	}


	/**
	 * 批量删除key
	 * @param keyList
	 */
	public static void pipeDelKeys(List<String> keyList){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(curDB);
		Pipeline p = jedis.pipelined();
		for(String key : keyList){
			p.del(key);
		}
		p.exec();
		RedisConfig.returnResource(jedis);
	}

	/**
	 * 清空库
	 * @param dbidx
	 */
	public static void flushDB(int dbidx){
		Jedis jedis = RedisConfig.getJedis();
		jedis.select(dbidx);
		jedis.flushDB();
		RedisConfig.returnResource(jedis);
	}



}
