package com.wangchu.service;

import com.wangchu.dal.entity.Message;
import com.wangchu.dao.mapper.MessageMapper;
import com.wangchu.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    SensitiveFilter filter;

    public List<Message> findConversations(int userId,int offset,int limit){
        return messageMapper.selectConversationsByUser(userId,offset,limit);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit){
        return messageMapper.selectLettersByConversationId(conversationId,offset,limit);
    }

    public int countConversation(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public int countLetters(String conversationId){
        return messageMapper.selectConversationUnreadCount(conversationId);
    }

    public int countUnread(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    public int insertMessage(Message message){
        //1.发送私信，存储到数据库，将文本剔除HTML和敏感词
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(filter.filter(message.getContent()));
        int i = messageMapper.insertMessage(message);
        return i;
    }



    public int updateMessage(List<Integer> ids){
        return messageMapper.updateLetterRead(ids,1);
    }

    public int deleteMessage(List<Integer> ids){
        return messageMapper.updateLetterRead(ids,2);
    }

    /**
     * 查询某主题下最新的系统通知
     * @param userId 系统发给某用户，某用户的id
     */
    public Message findLatestTopicMessage(int userId,String topic){
        return messageMapper.findLatestTopicMessage(userId, topic);
    }

    /**
     * 查询某主题下共几条会话
     * @param userId 统发给某用户，某用户的id
     */
    public int findTopicNum(int userId,String topic){
        return messageMapper.findTopicMessageNum(userId, topic);
    }

    /**
     * 查询某主题下共几条未读会话
     * @param userId 统发给某用户，某用户的id.
     * @param topic 主题为null,查询所有主题未读数量
     */
    public int findTopicUnreadNum(int userId,String topic){
        return messageMapper.findUnreadTopicNum(userId,topic);
    }

    /**
     * 查询某主题下的所有系统通知
     */
    public List<Message> findTopicMessage(int userId,String topic,int offset,int limit){
        return messageMapper.findTopicMessage(userId,topic,offset,limit);
    }
}
