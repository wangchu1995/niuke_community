package com.wangchu.dao.mapper;

import com.wangchu.dal.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    //1.根据用户查询所有会话，每次会话显示最后一次的消息
    List<Message> selectConversationsByUser(@Param("userId") int userId, @Param("offset") int offset,@Param("limit") int limit);
    //2.根据用户查询所有会话数
    int selectConversationCount(int userId);
    //3.根据会话id查询消息数量
    int selectConversationUnreadCount(String conversationId);
    //4.根据用户查询所有未读消息数//会话未读消息数，两个功能合并,对用户来说的
    int selectLetterUnreadCount(@Param("userId") int userId,@Param("conversationId") String conversationId);
    //5.根据会话id查询会话详情
    List<Message> selectLettersByConversationId(@Param("conversationId") String conversationId,@Param("offset") int offset,@Param("limit") int limit);

    //6.发送一条私信
    int insertMessage(Message message);
    //7.修改私信为已读
    int updateLetterRead(@Param("ids") List<Integer> ids,@Param("status") int status);

    //8.查询某个主题下最新的通知
    Message findLatestTopicMessage(@Param("userId")int userId,@Param("topic")String topic);
    //9.查询某个主题所包含的通知的数量
    int findTopicMessageNum(@Param("userId")int userId,@Param("topic")String topic);
    //10.查询某个主题下未读的通知的数量
    int findUnreadTopicNum(@Param("userId")int userId,@Param("topic")String topic);

    //11.查询某个主题下的所有通知
    List<Message> findTopicMessage(@Param("userId")int userId,@Param("topic")String topic,@Param("offset")int offset,@Param("limit")int limit);

}
