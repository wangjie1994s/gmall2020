package com.aisino.gmall.seckill.controller;

import com.aisino.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SecKillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;


    /**
     * 先到先得式秒杀
     * @return
     */
    @RequestMapping("secKill")
    @ResponseBody
    public String secKill(){

        Jedis jedis = redisUtil.getJedis();

        RSemaphore semaphore = redissonClient.getSemaphore("product");
        boolean b = semaphore.tryAcquire();

        int stock = Integer.parseInt(jedis.get("product"));
        if (b){
            System.out.println("当前用户抢购成功");
            //用消息队列发出订单消息

        } else {
            System.out.println("当前缓存剩余数量:"+stock+"抢购失败");
        }
        jedis.close();
        return "1";
    }

    /**
     * 随机拼运气式秒杀
     * @return
     */
    @RequestMapping("kill")
    @ResponseBody
    public String kill(){

        String memberId = "1";
        Jedis jedis = redisUtil.getJedis();
        //开启商品监控
        jedis.watch("product");
        int stock = Integer.parseInt(jedis.get("product"));
        if (stock > 0){
            Transaction multi = jedis.multi();
            multi.incrBy("product", -1);
            List<Object> exec = multi.exec();
            if (exec != null && exec.size() > 0){
                System.out.println("当前缓存剩余数量:"+stock+",用户:"+memberId+"抢购成功");
                //用消息队列发出订单消息

            } else {
                System.out.println("当前缓存剩余数量:"+stock+"抢购失败");
            }

        }
        jedis.close();

        return "恭喜您已秒杀成功该商品";
    }
}
