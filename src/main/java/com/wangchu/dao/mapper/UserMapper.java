package com.wangchu.dao.mapper;

import com.wangchu.dal.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    //mapper注解在mybatis-spring-boot-starter包下
    User selectUserById(int id);
    User selectUserByUsername(String username);
    User selectUserByEmail(String email);
    int insertOneUser(User user);
    int updateStatusById(@Param("status") int status, @Param("id") int id);
    int updataHeaderUrlById(@Param("headerUrl") String headerUrl,@Param("id") int id);
    int updatePasswordById(@Param("password") String password,@Param("id") int id);
}
