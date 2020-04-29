package com.wangchu.controller;

import com.wangchu.dal.entity.Comment;
import com.wangchu.dal.entity.User;
import com.wangchu.service.CommentService;
import com.wangchu.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;


@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    HostHolder hostHolder;
    @Autowired
    CommentService commentService;

    @RequestMapping(path = "/add/{postId}",method = RequestMethod.POST)
    public String addPost(@PathVariable("postId")int postId, Comment comment){
        //补充comment信息
        User user = hostHolder.getUsers();
        comment.setUserId(user.getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.intsertOneComment(comment);

        return "redirect:/discuss/detail/"+postId;
    }
}

