package com.wangchu.controller;

import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.Page;
import com.wangchu.service.ElasticsearchService;
import com.wangchu.service.LikeService;
import com.wangchu.service.UserService;
import com.wangchu.util.CommunityConstant;
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
public class SearchController {
    @Autowired
    ElasticsearchService elasticsearchService;
    @Autowired
    UserService userService;
    @Autowired
    LikeService likeService;

    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        org.springframework.data.domain.Page<DiscussPost> posts = elasticsearchService.searchDiscusspost(keyword, page.getCurrent() - 1, page.getShowItems());
        //聚合数据
        List<Map<String,Object>> dicussPosts = new ArrayList<>();
        if(posts!=null){
            for(DiscussPost post:posts){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                map.put("user",userService.selectUserById(post.getUserId()));
                map.put("likeCount",likeService.findLikeCount(CommunityConstant.ENTITY_TYPE_POST,post.getId()));
                dicussPosts.add(map);
            }
            model.addAttribute("discussPosts",dicussPosts);
            //get方式,需要存储keyword返回给model
            model.addAttribute("keyword",keyword);
            page.setPath("/search?keyword="+keyword);
            page.setTotalItems((int) posts.getTotalElements());
        }

        return "/site/search";
    }
}
