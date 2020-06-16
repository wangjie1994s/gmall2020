package com.aisino.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class testMq {

    public static void main(String[] args) {

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);//开启事务
            //队列模式的消息
            Queue testqueue = session.createQueue("drink");
            /**
             * 一次消费，适用于支付和订单服务
             */

            //话题模式的消息
            //Topic topic = session.createTopic("login");
            /**
             * 可多次消费，使用于登陆服务
             */

            MessageProducer producer = session.createProducer(testqueue);//创建消息
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("我渴了，谁能帮我打一杯水！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);//持久化
            producer.send(textMessage);//发送消息
            session.commit();//提交事务
            connection.close();//关闭连接
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
