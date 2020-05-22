package com.wangchu.dao;

import com.wangchu.WCApplication;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;




@Component
class KafkaProducerxx{
    @Autowired
    KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WCApplication.class)
public class KafkaTests {
    @Autowired
    KafkaProducerxx kaproducer;

    @Test
    public void test1(){
        System.out.println("xx");
    }

    @Test
    public void kafkaTest(){
        System.out.println("1");
        kaproducer.sendMessage("test","你好");
        kaproducer.sendMessage("test","我发送消息了");
        try {
            Thread.sleep(1000*8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}



@Component
class Consumer{
    @KafkaListener(topics = {"test"})
    public void acceptMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}