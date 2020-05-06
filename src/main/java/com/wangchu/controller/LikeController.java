package com.wangchu.controller;

import com.wangchu.annotation.LoginRequired;
import com.wangchu.dal.entity.User;
import com.wangchu.service.LikeService;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String like(int entityType,int entityId,int entityUserId){
        //点赞后异步请求此方法
        //必须登录后才能点赞
        User user = hostHolder.getUsers();
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        Map<String,Object> map = new HashMap<>();
        long count = likeService.findLikeCount(entityType,entityId);
        int status = likeService.findLikeStatus(user.getId(),entityType,entityId);
        map.put("likeCount",count);
        map.put("likeStatus",status);
        return CommonUtils.getJSONString(0,null,map);
    }

}
