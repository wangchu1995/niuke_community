package com.wangchu.dao.mapper;

import com.wangchu.dal.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    //1.查询分页评论信息
    List<Comment> selectComments(@Param("entityType") int entityType, @Param("entityId") int entityId,
                                 @Param("offset") int offset,@Param("limit") int limit);
    //2.查询评论数量
    int selectCommentCount(@Param("entityType") int entityType, @Param("entityId") int entityId);

    //3.插入一条评论
    int insertOneComment(Comment comment);

    //根据userId和entityType查询评论
    List<Comment> selectCommentsByUserId(@Param("userId") int userId,@Param("entityType") int entityType,@Param("offset") int offset,@Param("limit") int limit);

    int selectCountByUserId(@Param("userId") int userId,@Param("entityType") int entityType);
}
