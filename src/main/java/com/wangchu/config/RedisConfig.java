package com.wangchu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
            //设置连接工厂
            RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(factory);

            //设置序列化方式
            redisTemplate.setKeySerializer(RedisSerializer.string());
            redisTemplate.setValueSerializer(RedisSerializer.json());
            redisTemplate.setHashKeySerializer(RedisSerializer.string());
            redisTemplate.setHashValueSerializer(RedisSerializer.json());
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
    }

}
