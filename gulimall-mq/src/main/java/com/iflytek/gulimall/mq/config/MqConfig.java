package com.iflytek.gulimall.mq.config;

import com.iflytek.gulimall.mq.dao.MqMessageDao;
import com.iflytek.gulimall.mq.entity.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Configuration
@Slf4j
public class MqConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MqMessageDao mqMessageDao;


    /**
     * 1服务器收到消息就回调
     *  1、    publisher-confirm-type: correlated
     *  2、    设置确认回调
     * 2 消息抵达队列
     */
    @PostConstruct  //MqConfig对象创建完成以后，执行这个方法
    public void initRabbitMq() {
        log.info("初始化mq增强版中************");
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {

            /**
             *
             * @param correlationData  当前消息的唯一关联数据（这个是消息的唯一Id）
             * @param ack    消息是否成功收到
             * @param cause   失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                String messageId = correlationData.getId();
                log.info("消息成功抵达broker----->messageId:{}", messageId);

            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {


            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message   投递失败的消息详细信息
             * @param replyCode  回复的状态吗
             * @param replyText   回复的文本内容
             * @param exchange    当时消息发送个哪个交换机的
             * @param routingKey   当时这个消息使用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                String messageId = message.getMessageProperties().getHeader("spring_returned_message_correlation").toString();
                while (true) {
                    Optional<MqMessage> optional = mqMessageDao.findById(messageId);
                    if (optional.isPresent()) {
                            MqMessage mqMessage = optional.get();
                            mqMessage = optional.get();
                            mqMessage.setMessageStatus(2);
                            mqMessage.setUpdateTime(LocalDateTime.now());
                            mqMessage.setReplyCode(replyCode);
                            mqMessage.setReplyText(replyText);
                            log.info("消息没有抵达队列----->messageId:{},mongo数据库保存的mqMessage:{}", messageId, mqMessage);
                            mqMessageDao.save(mqMessage);
                            break;

                    }

                }
            }
        });
    }
}
