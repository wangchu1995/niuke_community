package com.wangchu.controller;

import com.wangchu.annotation.LoginRequired;
import com.wangchu.dal.entity.Event;
import com.wangchu.dal.entity.User;
import com.wangchu.event.EventProducer;
import com.wangchu.service.LikeService;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.HostHolder;
import com.wangchu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String like(int entityType,int entityId,int entityUserId,int postId){
        //点赞后异步请求此方法
        //必须登录后才能点赞
        User user = hostHolder.getUsers();
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        Map<String,Object> map = new HashMap<>();
        long count = likeService.findLikeCount(entityType,entityId);
        int status = likeService.findLikeStatus(user.getId(),entityType,entityId);
        map.put("likeCount",count);
        map.put("likeStatus",status);
        if(status== 1){
            //点赞才能发送点赞事件，取消点赞不发送
            //点赞状态后为1意味着之前没点赞
            //目前只实现了给帖子/回复点赞的功能
            Event event = new Event().setEntityType(entityType).setEntityId(entityId)
                    .setEntityUserId(entityUserId).setTopic(CommunityConstant.TOPIC_LIKE)
                    .setUserId(user.getId()).setData("postId",postId);
            eventProducer.sendMessage(event);
        }

        //修改了帖子,统计入缓存,定时重新计算帖子分数
        String redisKey = RedisKeyUtil.getPostScore();
        //设置key的过期时间
        redisTemplate.expire(redisKey,1000*3600*10, TimeUnit.MILLISECONDS);
        redisTemplate.opsForSet().add(redisKey,postId);

        return CommonUtils.getJSONString(0,null,map);
    }

}
