package com.wangchu.controller;

import com.wangchu.dal.entity.Message;
import com.wangchu.dal.entity.Page;
import com.wangchu.dal.entity.User;
import com.wangchu.service.MessageService;
import com.wangchu.service.UserService;
import com.wangchu.util.CommonUtils;
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
public class MessageController {
    @Autowired
    MessageService messageService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    UserService userService;

    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getConversations(Model model, Page page){
//        Integer.valueOf("abc");
        //1.处理分页插件
        User user = hostHolder.getUsers();
        page.setShowItems(5);
        page.setPath("/letter/list");
        page.setTotalItems(messageService.countConversation(user.getId()));
        //2.调用查询会话
        List<Message> conversations = messageService.findConversations(user.getId(), page.getOffset(), page.getShowItems());
        List<Map<String,Object>> conversationsInfo = new ArrayList<>();
        //每个map作为一个会话，需要保存的信息:目标用户，未读消息，消息总数
        //model需要保存的信息:未读会话数，未读消息数，
        if(conversations!=null){
        for(Message m:conversations){
            Map<String,Object> map = new HashMap<>();
            int targetUserId = m.getFromId()==user.getId()?m.getToId():m.getFromId();
            User targetUser = userService.selectUserById(targetUserId);
            map.put("conversation",m);
            map.put("targetUser",targetUser);
            map.put("unreadLetter",messageService.countUnread(user.getId(),m.getConversationId()));
            map.put("totalLetter",messageService.countLetters(m.getConversationId()));
            conversationsInfo.add(map);
        }

        }
        model.addAttribute("conversations",conversationsInfo);
        model.addAttribute("unreadLetter",messageService.countUnread(user.getId(),null));
       return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}")
    public String getLetters(@PathVariable("conversationId") String conversationId,Model model,Page page){
        //1.处理分页
        page.setShowItems(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setTotalItems(messageService.countLetters(conversationId));

        //2.查询消息信息
        User user = hostHolder.getUsers();
        List<Message> letters = messageService.findLetters(conversationId, page.getOffset(), page.getShowItems());
        List<Map<String,Object>> list = new ArrayList<>();
        List<Integer> updateIds = new LinkedList<>();
        if(letters!=null){
            for(Message m:letters){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",m);
                map.put("user",userService.selectUserById(m.getFromId()));
                list.add(map);
                if(m.getStatus()==0&&m.getToId()==user.getId()){
                    updateIds.add(m.getId());
                }
            }
        }
        model.addAttribute("list",list);

        //来自xxx的微信
        int fromid = getFromId(conversationId,user.getId());
        User fromUser = userService.selectUserById(fromid);
        model.addAttribute("fromUser",fromUser);

        //更新私信已读状态
        if(!updateIds.isEmpty()){
            messageService.updateMessage(updateIds);
        }
        return "/site/letter-detail";
    }

    private int getFromId(String conversationId,int userId){
        String[] users = conversationId.split("_");
        int fromId = Integer.valueOf(users[0])==userId?Integer.valueOf(users[1]):Integer.valueOf(users[0]);
        return fromId;
    }

    @RequestMapping(path="/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        Integer.valueOf("abc");
        User toUser = userService.findUserByName(toName);
        if(toUser==null)
            return CommonUtils.getJSONString(0,"目标用户不存在");
        User fromUser = hostHolder.getUsers();
        Message message = new Message();
        message.setFromId(fromUser.getId());
        message.setContent(content);
        message.setToId(toUser.getId());
        message.setStatus(0);
        message.setCreateTime(new Date());
        message.setConversationId(getConversationId(message.getFromId(),message.getToId()));
        messageService.insertMessage(message);
        return CommonUtils.getJSONString(0);
    }


    private String getConversationId(int fromId,int toId){
        return fromId<=toId?fromId+"_"+toId:toId+"_"+fromId;
    }

    @RequestMapping(path = "/letter/delete",method = RequestMethod.GET)
    public String deleteLetter(int letterId){
        List<Integer> ids = new LinkedList<>();
        ids.add(letterId);
        messageService.deleteMessage(ids);
        return "redirect:/site/letter-detail";
    }
}
