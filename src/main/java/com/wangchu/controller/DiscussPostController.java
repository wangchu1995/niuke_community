package com.wangchu.controller;

import com.wangchu.dal.entity.*;
import com.wangchu.event.EventProducer;
import com.wangchu.service.CommentService;
import com.wangchu.service.DiscussPostService;
import com.wangchu.service.LikeService;
import com.wangchu.service.UserService;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.HostHolder;
import com.wangchu.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    LikeService likeService;
    @Autowired
    EventProducer producer;
    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addPost(String title,String content){
        User user = hostHolder.getUsers();
        if(user==null) return CommonUtils.getJSONString(403,"账户未登录");
        if(StringUtils.isBlank(content)||StringUtils.isBlank(title)) return CommonUtils.getJSONString(403,"缺少内容或标题");
        DiscussPost post = new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setUserId(user.getId());
        post.setScore(0d);
        discussPostService.insertDiscustPost(post);

        //添加帖子到搜索引擎,接触KAFKA完成异步添加
        Event postEvent = new Event().setTopic(CommunityConstant.TOPIC_PUBLISH)
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(post.getId())
                .setUserId(user.getId());
        producer.sendMessage(postEvent);

        //修改了帖子,统计入缓存,定时重新计算帖子分数
        String redisKey = RedisKeyUtil.getPostScore();
        //设置key的过期时间
        redisTemplate.expire(redisKey,1000*3600*10, TimeUnit.MILLISECONDS);
        redisTemplate.opsForSet().add(redisKey,post.getId());
        return CommonUtils.getJSONString(0,"发布成功");
    }

    @RequestMapping("/detail/{postId}")
    public String findOnePost(Model model, @PathVariable("postId") int id, Page page){
        //处理帖子
        DiscussPost post = discussPostService.selectOneDiscussPost(id);
        int userId = post.getUserId();
        User user = userService.selectUserById(userId);
        model.addAttribute("post",post);
        model.addAttribute("user",user);

        //点赞的处理
        long likeCount = likeService.findLikeCount(CommunityConstant.ENTITY_TYPE_POST,post.getId());
        User users = hostHolder.getUsers();
        int likeStatus = users==null?0:likeService.findLikeStatus(users.getId(),CommunityConstant.ENTITY_TYPE_POST,post.getId());
        model.addAttribute("postLikeCount",likeCount);
        model.addAttribute("postLikeStatus",likeStatus);
        //处理评论的相关信息
        /*数据结构List<回帖-Map<key,value>>:
          key-user value user
          key-post value comment
          key-List<回复map> map
             key-user
             key-comment
        * */
        //分页的处理
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
            //点赞处理
            likeCount = likeService.findLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT,c.getId());
            likeStatus = users==null?0:likeService.findLikeStatus(users.getId(),CommunityConstant.ENTITY_TYPE_COMMENT,c.getId());
            commentMap.put("postLikeCount",likeCount);
            commentMap.put("postLikeStatus",likeStatus);
            for(Comment r:replys){
                Map<String,Object> replyMap = new HashMap<>();
                replyMap.put("reply",r);
                replyMap.put("user",userService.selectUserById(r.getUserId()));
                replyMap.put("targetUser",userService.selectUserById(r.getTargetId()));

                //点赞处理
                likeCount = likeService.findLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT,r.getId());
                likeStatus = users==null?0:likeService.findLikeStatus(users.getId(),CommunityConstant.ENTITY_TYPE_COMMENT,r.getId());
                replyMap.put("postLikeCount",likeCount);
                replyMap.put("postLikeStatus",likeStatus);
                replyList.add(replyMap);
            }
            commentMap.put("replys",replyList);
            commentList.add(commentMap);
        }
        model.addAttribute("commentList",commentList);
        return "/site/discuss-detail";
    }

    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        User user = hostHolder.getUsers();
        //帖子状态被修改，需要同步到elasticaSearch搜索引擎
        Event postEvent = new Event().setTopic(CommunityConstant.TOPIC_PUBLISH)
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id)
                .setUserId(user.getId());
        producer.sendMessage(postEvent);
        return CommonUtils.getJSONString(0);
    }

    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        User user = hostHolder.getUsers();
        //帖子状态被修改，需要同步到elasticaSearch搜索引擎
        Event postEvent = new Event().setTopic(CommunityConstant.TOPIC_PUBLISH)
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id)
                .setUserId(user.getId());
        producer.sendMessage(postEvent);

        //修改了帖子,统计入缓存,定时重新计算帖子分数
        String redisKey = RedisKeyUtil.getPostScore();
        //设置key的过期时间
        redisTemplate.expire(redisKey,1000*3600*10, TimeUnit.MILLISECONDS);
        redisTemplate.opsForSet().add(redisKey,id);

        return CommonUtils.getJSONString(0);
    }

    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        User user = hostHolder.getUsers();
        //帖子状态被修改，需要同步到elasticaSearch搜索引擎
        Event postEvent = new Event().setTopic(CommunityConstant.TOPIC_DELETEPOST)
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id)
                .setUserId(user.getId());
        producer.sendMessage(postEvent);
        return CommonUtils.getJSONString(0);
    }
}
