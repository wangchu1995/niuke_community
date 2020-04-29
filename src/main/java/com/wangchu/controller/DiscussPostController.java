package com.wangchu.controller;

import com.wangchu.dal.entity.Comment;
import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.Page;
import com.wangchu.dal.entity.User;
import com.wangchu.service.CommentService;
import com.wangchu.service.DiscussPostService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    UserService userService;
    @Autowired
    CommentService commentService;

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
    public String findOnePost(Model model, @PathVariable("postId") int id, Page page){
        DiscussPost post = discussPostService.selectOneDiscussPost(id);
        int userId = post.getUserId();
        User user = userService.selectUserById(userId);
        model.addAttribute("post",post);
        model.addAttribute("user",user);

        //处理评论的相关信息
        /*数据结构List<回帖-Map<key,value>>:
          key-user value user
          key-post value comment
          key-List<回复map> map
             key-user
             key-comment
        * */
        page.setPath("/discuss/detail/"+post.getId());  //分页的访问路径虽然相同，但是每次访问携带的page不同
        page.setShowItems(5);
        page.setTotalItems(commentService.selectCountComment(CommunityConstant.ENTITY_TYPE_POST, post.getId()));
        List<Map<String,Object>> commentList = new LinkedList<>();
        List<Comment> comments = commentService.selectComments(CommunityConstant.ENTITY_TYPE_POST, post.getId(),page.getOffset(),5);
        for(Comment c:comments){
            Map<String,Object> commentMap = new HashMap<>();
            commentMap.put("comment",c);
            commentMap.put("user",userService.selectUserById(c.getUserId()));
            List<Map<String,Object>> replyList = new LinkedList<>();
            List<Comment> replys = commentService.selectComments(CommunityConstant.ENTITY_TYPE_COMMENT, c.getId(),0,Integer.MAX_VALUE);
            int count = commentService.selectCountComment(CommunityConstant.ENTITY_TYPE_COMMENT,c.getId());
            commentMap.put("replyCount",count);
            for(Comment r:replys){
                Map<String,Object> replyMap = new HashMap<>();
                replyMap.put("reply",r);
                replyMap.put("user",userService.selectUserById(r.getUserId()));
                replyMap.put("targetUser",userService.selectUserById(r.getTargetId()));
                replyList.add(replyMap);
            }
            commentMap.put("replys",replyList);
            commentList.add(commentMap);
        }
        model.addAttribute("commentList",commentList);
        return "/site/discuss-detail";
    }
}
