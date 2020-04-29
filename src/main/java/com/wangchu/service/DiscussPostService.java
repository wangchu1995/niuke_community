package com.wangchu.service;

import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dao.mapper.DiscussPostMapper;
import com.wangchu.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    DiscussPostMapper discussPostMapper;
    @Autowired
    SensitiveFilter filter;

    public List<DiscussPost> findDiscussPostByUserId(int userid,int offset,int limit){
        return discussPostMapper.selectDiscussPostByUserId(userid,offset,limit);
    }

    public int findDiscussPostNumByUserId(int userid){
        return discussPostMapper.selectDiscussPostNumByUserId(userid);
    }

    public int insertDiscustPost(DiscussPost post){
        if(post==null) throw new IllegalArgumentException("参数不能为空");
        //1.转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //2.过滤敏感词
        post.setTitle(filter.filter(post.getTitle()));
        post.setContent(filter.filter(post.getContent()));

        int i = discussPostMapper.insertDiscussPost(post);
        return i;
    }

    public DiscussPost selectOneDiscussPost(int id){
        return discussPostMapper.selectOneDiscussPost(id);
    }
}
