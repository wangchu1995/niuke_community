package com.wangchu.dao.mapper;

import com.wangchu.dal.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //根据userId查询帖子
    List<DiscussPost> selectDiscussPostByUserId(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);
    //根据userId查询帖子数
    int selectDiscussPostNumByUserId(@Param("userId") int userId);
}
