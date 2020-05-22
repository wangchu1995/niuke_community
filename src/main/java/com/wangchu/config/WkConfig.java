package com.wangchu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {
    @Value("${wk.image.storage}")
    private String storagePath;

    private final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @PostConstruct
    public void init(){
        File file = new File(storagePath);
        if(!file.exists()){
            file.mkdir();
            logger.info("创建长图目录："+storagePath);
        }
    }
}
