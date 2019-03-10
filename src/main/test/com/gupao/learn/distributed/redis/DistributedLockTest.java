package com.gupao.learn.distributed.redis;

import com.gupao.learn.distributed.redis.demo.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:
 *
 * @author 轩辚
 * @date 2019/3/10 18:51
 */
public class DistributedLockTest extends Thread{
    /**
     * 锁的名称
     */
    private static final String LOCK_NAME = "updateOrder";
    /**
     * 初始化日志对象
     */
    private static Logger logger = LoggerFactory.getLogger(DistributedLockTest.class);

    /**
     * 分布式锁的测试
     */
    @Override
    public void run() {
        while(true) {
            DistributedLock distributedLock = new DistributedLock();
            //获取到锁
            String lockIdentifier = distributedLock.acquireLock(LOCK_NAME, 3000, 5000);
            //如果lockIdentifier不为空则表示成功获取到锁
            if (lockIdentifier != null && !"".equals(lockIdentifier)) {
                //休眠1s后释放锁
                try {
                    Thread.sleep(1000);
                    distributedLock.releaseLock(LOCK_NAME, lockIdentifier);
                } catch (InterruptedException e) {
                    logger.info("线程休眠异常", e);
                }
                break;
            }
        }
    }

    public static void main(String[] args) {
        DistributedLockTest distributedLockTest = new DistributedLockTest();
        for (int i=0;i<10;i++){
            new Thread(distributedLockTest,"ThreadName"+i).start();
        }
    }
}
