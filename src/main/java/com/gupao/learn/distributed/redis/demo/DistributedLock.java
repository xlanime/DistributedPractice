package com.gupao.learn.distributed.redis.demo;

import com.gupao.learn.distributed.redis.utils.JedisConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.UUID;

/**
 * Description:Redis实现分布式锁
 *
 * @author 轩辚
 * @date 2019/3/10 17:55
 */
public class DistributedLock {

    /**
     * 初始化日志对象
     */
    private Logger logger = LoggerFactory.getLogger(DistributedLock.class);

    /**
     * 请求锁
     * @param lockName   锁的名称
     * @param acquireTimeout 获取锁的超时时间  单位毫秒
     * @param lockTimeOut 锁本身的过期时间（防止死锁） 单位毫秒
     * @return 获取到的锁
     */
    public String acquireLock(String lockName,long acquireTimeout,long lockTimeOut){
        //获取UUID作为锁的标识
        String identifier = UUID.randomUUID().toString();
        logger.info(lockName+" 开始获取锁："+identifier);
        //设置锁的Key
        String lockKey = Consts.LOCK_NAMESPACE+lockName;
        //锁的超时时间
        int timeoutExpire = (int)(lockTimeOut);
        //定义redis连接
        Jedis jedisConnection = null;
        //获取锁的超时时间
        long endTime = System.currentTimeMillis() + acquireTimeout;
        try {
            //获取redis连接
            jedisConnection = JedisConnectionUtil.getJedisConnection();
            jedisConnection.auth(Consts.REDIS_AUTH);
            //如果没有超时，则一直循环获取锁，直到获取成功
            while (System.currentTimeMillis() < endTime) {
                //如果设置锁成功
                if (jedisConnection.setnx(lockKey, identifier) == 1) {
                    //设置超时时间
                    jedisConnection.expire(lockKey, timeoutExpire);
                    logger.info(lockName+" 获取锁成功："+identifier);
                    return identifier;
                }
                //如果超时没有设置成功，重新设置超时
                if (jedisConnection.ttl(lockKey) == -1) {
                    jedisConnection.expire(lockKey, timeoutExpire);
                }
                //间隔性地进行尝试
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.info("线程休眠异常：", e);
                }
            }
        }finally {
            //回收jedis连接
            if(jedisConnection != null) {
                jedisConnection.close();
            }
        }
        logger.info(lockName+" 获取锁失败："+identifier);
        return null;
    }

    /**
     * 释放锁
     * @param lockName 锁的名称
     * @param identifier 锁的标识
     * @return 释放的结果，释放成功为true，失败为false
     */
    public boolean releaseLock(String lockName,String identifier){
        logger.info(lockName+" 开始释放锁："+identifier);
        //初始化释放锁的结果
        boolean isRealease = false;
        //需要释放的锁的key
        String lockKey = Consts.LOCK_NAMESPACE+lockName;
        //获取redis连接
        Jedis jedisConnection = null;
        try{
            jedisConnection = JedisConnectionUtil.getJedisConnection();
            jedisConnection.auth(Consts.REDIS_AUTH);
            while(true) {
                //添加jedis事务
                jedisConnection.watch(lockKey);
                if (identifier != null && identifier.equals(jedisConnection.get(lockKey))) {
                    Transaction transaction = jedisConnection.multi();
                    transaction.del(lockKey);
                    //判断执行结果是否为空，如果为空则认为没有释放，继续执行。
                    if(transaction.exec().isEmpty()){
                        continue;
                    }
                    isRealease = true;
                    logger.info(lockName+" 释放锁完成。");
                    logger.info(" =========================================== ");
                }else{
                    logger.info("请求释放的锁与缓存中的锁不一致，无法释放");
                    break;
                }
                jedisConnection.unwatch();
                break;
            }
        }finally {
            //释放jedis连接
            if(jedisConnection != null) {
                jedisConnection.close();
            }
        }

        return isRealease;
    }
}
