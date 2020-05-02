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
}
