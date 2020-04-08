package com.wangchu.service;

import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dao.mapper.DiscussPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPostByUserId(int userid,int offset,int limit){
        return discussPostMapper.selectDiscussPostByUserId(userid,offset,limit);
    }

    public int findDiscussPostNumByUserId(int userid){
        return discussPostMapper.selectDiscussPostNumByUserId(userid);
    }
}
