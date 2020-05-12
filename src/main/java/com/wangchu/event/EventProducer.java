package com.wangchu.event;

import com.alibaba.fastjson.JSONObject;
import com.wangchu.dal.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    @Autowired
    KafkaTemplate kafkaTemplate;

    public void sendMessage(Event event){
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
