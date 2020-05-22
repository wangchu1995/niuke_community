package com.wangchu.dao.mapper;

import com.wangchu.dal.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //根据userId查询帖子
    List<DiscussPost> selectDiscussPostByUserId(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit,
                                                @Param("orderMode") int orderMode);
    //根据userId查询帖子数
    int selectDiscussPostNumByUserId(@Param("userId") int userId);
    //根据帖子ID查询单个帖子详情
    DiscussPost selectOneDiscussPost(int id);
    //插入新帖子
    int insertDiscussPost(DiscussPost discussPost);

    //修改评论数量
    int updateCommentCount(@Param("postId") int postid,@Param("count") int count);

    int updateType(@Param("postId")int postId,@Param("type")int type);

    int updateStatus(@Param("postId")int postId,@Param("status")int status);

    //修改帖子分数
    int updateScore(@Param("id")int id,@Param("score")double score);
}
