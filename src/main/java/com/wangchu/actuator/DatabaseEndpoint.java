package com.wangchu.actuator;

import com.wangchu.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Endpoint(id="database")
public class DatabaseEndpoint {

    private final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);
    @Qualifier("dataSource")
    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection(){
        try (Connection connection = dataSource.getConnection();){
            return CommonUtils.getJSONString(0,"获取连接陈成功");
        } catch (SQLException throwables) {
            logger.error("获取连接失败");
            throwables.printStackTrace();
            return CommonUtils.getJSONString(1,"获取连接陈失败");
        }
    }

}
