package com.gupao.learn.distributed.redis.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Description:获取redis连接的工具类
 *
 * @author 轩辚
 * @date 2019/3/10 11:59
 */
public class JedisConnectionUtil {
    /**
     * 定义JedisPool
     */
    private static JedisPool jedisPool = null;

    /**
     * 使用静态块初始化jedisPool
     */
    {
        //设置连接池的属性，例如最大连接数为100，用以保护服务器端的redis
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPool = new JedisPool(jedisPoolConfig,"120.77.35.203",6379);
    }

    /**
     * 从jedis连接池获取到jedis的连接
     * @return 获取到的jedis连接
     */
    public static Jedis getJedisConnection(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPool = new JedisPool(jedisPoolConfig,"120.77.35.203",6379);
        return jedisPool.getResource();
    }
}
