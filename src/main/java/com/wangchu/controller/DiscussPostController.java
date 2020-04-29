package com.wangchu.controller;

import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.User;
import com.wangchu.service.DiscussPostService;
import com.wangchu.service.UserService;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    UserService userService;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addPost(String title,String content){
        User user = hostHolder.getUsers();
        if(user==null) return CommonUtils.getJSONString(403,"账户未登录");
        DiscussPost post = new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setUserId(user.getId());
        discussPostService.insertDiscustPost(post);
        return CommonUtils.getJSONString(0,"发布成功");
    }

    @RequestMapping("/detail/{postId}")
    public String findOnePost(Model model, @PathVariable("postId") int id){
        DiscussPost post = discussPostService.selectOneDiscussPost(id);
        int userId = post.getUserId();
        User user = userService.selectUserById(userId);
        model.addAttribute("post",post);
        model.addAttribute("user",user);
        return "/site/discuss-detail";

    }
}
