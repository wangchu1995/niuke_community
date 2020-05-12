package com.wangchu.controller;

import com.wangchu.dal.entity.Event;
import com.wangchu.dal.entity.Page;
import com.wangchu.dal.entity.User;
import com.wangchu.event.EventProducer;
import com.wangchu.service.FollowService;
import com.wangchu.service.UserService;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController {
    @Autowired
    FollowService followService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    UserService userService;
    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUsers();
        if(user == null) throw new IllegalArgumentException("用户不能为空");
        followService.follow(user.getId(),entityType,entityId);
        //只能关注用户，所以目标直接传递entityId
        Event event = new Event().setTopic(CommunityConstant.TOPIC_FOLLOW).setUserId(user.getId())
                .setEntityType(entityType).setEntityId(entityId).setEntityUserId(entityId);
        eventProducer.sendMessage(event);
        return CommonUtils.getJSONString(0,"关注成功");
    }

    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUsers();
        if(user == null) throw new IllegalArgumentException("用户不能为空");
        followService.unfollow(user.getId(),entityType,entityId);
        return CommonUtils.getJSONString(0,"取消关注成功");
    }

    @RequestMapping(path = "/followee/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.selectUserById(userId);
        if(user==null) throw new RuntimeException("用户不存在");
        model.addAttribute("user",user);
        //设置分页
        page.setShowItems(5);
        page.setPath("/followee/"+userId);
        page.setTotalItems((int) followService.findFolloweeNum(userId, CommunityConstant.ENTITY_TYPE_USER));

        //补充是否关注过
        List<Map<String, Object>> targetUsers = followService.findFollowees(userId, page.getOffset(), page.getShowItems());
        if(targetUsers!=null){
            for(Map<String,Object> map:targetUsers){
                boolean hasFollowed = followService.findHasFollowed(hostHolder.getUsers().getId(), CommunityConstant.ENTITY_TYPE_USER, ((User) map.get("user")).getId());
                map.put("hasFollowed",hasFollowed);
            }
        }
        model.addAttribute("targetUsers",targetUsers);
        return "/site/followee";
    }

    @RequestMapping(path = "/follower/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.selectUserById(userId);
        if(user==null) throw new RuntimeException("用户不存在");
        model.addAttribute("user",user);
        //设置分页
        page.setShowItems(5);
        page.setPath("/follower/"+userId);
        page.setTotalItems((int) followService.findFollowerNum(userId, CommunityConstant.ENTITY_TYPE_USER));

        //补充是否关注过
        List<Map<String, Object>> targetUsers = followService.findFollowers(userId, page.getOffset(), page.getShowItems());
        if(targetUsers!=null){
            for(Map<String,Object> map:targetUsers){
                boolean hasFollowed = followService.findHasFollowed(hostHolder.getUsers().getId(), CommunityConstant.ENTITY_TYPE_USER, ((User) map.get("user")).getId());
                map.put("hasFollowed",hasFollowed);
            }
        }
        model.addAttribute("targetUsers",targetUsers);
        return "/site/follower";
    }
}
