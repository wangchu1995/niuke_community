package com.wangchu.service;

import com.wangchu.dal.entity.User;
import com.wangchu.dao.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    public User selectUserById(int id){
        User user = userMapper.selectUserById(id);
        return user;
    }


}
