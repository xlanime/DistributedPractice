package com.gupao.learn.distributed.redis.demo;

import com.gupao.learn.distributed.redis.utils.JedisConnectionUtil;
import redis.clients.jedis.Jedis;

/**
 * Description:
 *
 * @author 轩辚
 * @date 2019/3/10 17:13
 */
public class JedisTest {

    public static final String PASSWORD = "wozai80";
    public static final String TEST_KEY = "test";

    public static void main(String[] args) {
        Jedis jedisClient = JedisConnectionUtil.getJedisConnection();
        jedisClient.auth(PASSWORD);
        String testStr = jedisClient.get(TEST_KEY);
        System.out.println(testStr);
    }
}
