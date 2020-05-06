package com.wangchu.controller;

import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.Page;
import com.wangchu.dal.entity.User;
import com.wangchu.service.DiscussPostService;
import com.wangchu.service.LikeService;
import com.wangchu.service.UserService;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    UserService userService;
    @Autowired
    LikeService likeService;
    @Autowired
    HostHolder hostHolder;

    @RequestMapping("/index")
    public String findDiscussPost(Model model, Page page){
        // 方法调用钱,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        //前端传过来的Page参数只有当前是多少页
        page.setPath("/index");
        //根据current获取起始行数
        int offset = page.getOffset();
        int totalItems = discussPostService.findDiscussPostNumByUserId(0);
        page.setTotalItems(totalItems);
        int showItems = page.getShowItems();

        //应该返回帖子和用户的相关信息
        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        List<DiscussPost> discussPostList = discussPostService.findDiscussPostByUserId(0,offset,showItems);
        for(DiscussPost discussPost:discussPostList){
            int userId = discussPost.getUserId();
            User user = userService.selectUserById(userId);
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("post",discussPost);
            map.put("user",user);
            //点赞处理
            long likeCount = likeService.findLikeCount(CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());
            User users = hostHolder.getUsers();
            int likeStatus = users==null?0:likeService.findLikeStatus(users.getId(),CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());
            map.put("postLikeCount",likeCount);
            map.put("postLikeStatus",likeStatus);

            list.add(map);
        }
        model.addAttribute("postlist",list);



        model.addAttribute("page",page);
        return "index";
    }

    @RequestMapping(path="/error",method = RequestMethod.GET)
    public String error(){
        return "/error/500";
    }


}
