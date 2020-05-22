package com.wangchu.controller;

import com.wangchu.dal.entity.Comment;
import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.Event;
import com.wangchu.dal.entity.User;
import com.wangchu.event.EventProducer;
import com.wangchu.service.CommentService;
import com.wangchu.service.DiscussPostService;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.HostHolder;
import com.wangchu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    HostHolder hostHolder;
    @Autowired
    CommentService commentService;
    @Autowired
    EventProducer  eventProducer;
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{postId}",method = RequestMethod.POST)
    public String addPost(@PathVariable("postId")int postId, Comment comment){
        //补充comment信息
        User user = hostHolder.getUsers();
        comment.setUserId(user.getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.intsertOneComment(comment);

        //发送系统通知事件,同时添加了评论
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_COMMENT);
        event.setUserId(hostHolder.getUsers().getId());
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId())
                .setData("postId",postId);
        //评论和回复需要查表才能知道回复的是谁
        if(comment.getEntityType()==CommunityConstant.ENTITY_TYPE_COMMENT){
            Comment target = commentService.selectCommentById(comment.getEntityId());//通过回复ID查询
            event.setEntityUserId(target.getTargetId());
        }else if(comment.getEntityType()==CommunityConstant.ENTITY_TYPE_POST){
            //回复的是帖子:
            DiscussPost post = discussPostService.selectOneDiscussPost(comment.getEntityId());//通过帖子id查询
            event.setEntityUserId(post.getUserId());

            //修改了帖子,统计入缓存,定时重新计算帖子分数
            String redisKey = RedisKeyUtil.getPostScore();
            //设置key的过期时间
            redisTemplate.expire(redisKey,1000*3600*10, TimeUnit.MILLISECONDS);
            redisTemplate.opsForSet().add(redisKey,post.getId());
        }
        eventProducer.sendMessage(event);
        event.setTopic(CommunityConstant.TOPIC_PUBLISH).setEntityType(CommunityConstant.ENTITY_TYPE_COMMENT)
                .setEntityId(comment.getId()).setUserId(user.getId());
        eventProducer.sendMessage(event);

        return "redirect:/discuss/detail/"+postId;
    }
}

