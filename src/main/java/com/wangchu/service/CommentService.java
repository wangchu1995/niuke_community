package com.wangchu.service;

import com.wangchu.dal.entity.Comment;
import com.wangchu.dao.mapper.CommentMapper;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentMapper commentMapper;
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    SensitiveFilter filter;

    public List<Comment> selectComments(int entityType, int entityId,int offset,int limit){
        return commentMapper.selectComments(entityType,entityId,offset,limit);
    }

    public int selectCountComment(int entityType,int entityId){
        return commentMapper.selectCommentCount(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int intsertOneComment(Comment comment){
        //过滤html标签和敏感词
        if(comment==null) throw new IllegalArgumentException("参数不能为空");
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(filter.filter(comment.getContent()));
        int i = commentMapper.insertOneComment(comment);
        if(comment.getEntityType()== CommunityConstant.ENTITY_TYPE_POST){
            //只有评论的是帖子时，才更新评论数
            int count = commentMapper.selectCommentCount(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getTargetId(),count);
        }
        return i;
    }

    public List<Comment> selectCommentByUserId(int userId,int entityType,int offset,int limit){
        return commentMapper.selectCommentsByUserId(userId,entityType,offset,limit);
    }

    public int selectCountByUserId(int userId,int entityType){
        return commentMapper.selectCountByUserId(userId,entityType);
    }
}
