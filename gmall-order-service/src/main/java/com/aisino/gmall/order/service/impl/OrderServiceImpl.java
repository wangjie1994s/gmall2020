package com.aisino.gmall.order.service.impl;

import com.aisino.gmall.bean.OmsOrder;
import com.aisino.gmall.bean.OmsOrderItem;
import com.aisino.gmall.mq.ActiveMQUtil;
import com.aisino.gmall.order.mapper.OmsOrderItemMapper;
import com.aisino.gmall.order.mapper.OmsOrderMapper;
import com.aisino.gmall.service.CartService;
import com.aisino.gmall.service.OrderService;
import com.aisino.gmall.util.RedisUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Reference
    CartService cartService;

    @Autowired
    ActiveMQUtil activeMQUtil;



    @Override
    public String checkTradeCode(String memberId, String tradeCode) {

        Jedis jedis = null;
        try{
            //连接缓存
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";
            //String tradeCodeFromCache = jedis.get(tradeKey);
            //防止多线程并发下一key多用，解决方法：可以用lua脚本，在查询到key的同时删除该key，防止高并发下的意外的发生
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));

            if (eval!=null&&eval!=0) {
                jedis.del(tradeKey);
                return "success";
            } else {
                return "fail";
            }
        }finally {
            jedis.close();
        }
    }

    @Override
    public String genTradeCode(String memberId) {

        Jedis jedis = null;
        try{
            //根据memberId生成交易码
            //连接缓存
            jedis = redisUtil.getJedis();
            //查询缓存
            String tradeCode = UUID.randomUUID().toString();
            String tradeKey = "user:" + memberId + ":tradeCode";
            jedis.setex(tradeKey, 60*15, tradeCode);
            return tradeCode;
        }finally {
            jedis.close();
        }
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {

        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        //保存订单详情
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {

            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车数据,需要注入购物车的mapper
            //todo
            //cartService.delCart();


        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {

        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        omsOrder = omsOrderMapper.selectOne(omsOrder);
        return omsOrder;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {

        Example e = new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());
        OmsOrder omsOrderUpdate = new OmsOrder();
        //订单已支付状态
        omsOrderUpdate.setStatus("1");

        //发送一个订单已支付的队列，提供给库存消费
        Connection connection = null;
        Session session = null;
        try{
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(order_pay_queue);
            MapMessage mapMessage = new ActiveMQMapMessage();// hash结构
            //更新数据库中status状态为1
            omsOrderMapper.updateByExampleSelective(omsOrderUpdate, e);
            producer.send(mapMessage);
            session.commit();
        }catch (Exception ex){
            // 消息回滚
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                connection.close();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }

    }
}
