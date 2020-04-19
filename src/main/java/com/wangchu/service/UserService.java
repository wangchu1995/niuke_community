package com.wangchu.service;

import com.wangchu.dal.entity.User;
import com.wangchu.dao.mapper.UserMapper;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;
    @Value("${community.context-path}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    MailClient mailClient;

    public User selectUserById(int id){
        User user = userMapper.selectUserById(id);
        return user;
    }

    public Map<String,Object> register(User user) throws MessagingException {
        //l.空值判断
        if(user==null) throw new IllegalArgumentException("参数不能为空");
        Map<String,Object> map = new HashMap<String, Object>();
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空");
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
        }
        //2.重复值判断
        User u = userMapper.selectUserByUsername(user.getUsername());
        if(u!=null) map.put("usernameMsg","用户名已存在");
        u=userMapper.selectUserByEmail(user.getEmail());
        if(u!=null) map.put("emailMsg","邮箱已存在");

        //3.注册账号
        if(map==null||map.isEmpty()) {
            user.setSalt(CommonUtils.getUUID().substring(0, 5));
            user.setPassword(CommonUtils.md5(user.getPassword() + user.getSalt()));
            user.setType(0);
            user.setStatus(0);
            user.setActivationCode(CommonUtils.getUUID());
            //images.nowcode.com/head/0t.png
            user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
            user.setCreateTime(new Date());
            userMapper.insertOneUser(user);

            //4.发送激活邮件
            Context context = new Context();
            context.setVariable("email", user.getEmail());
            System.out.println("userId=  "+user.getId());
            String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
            context.setVariable("url", url);
            String content = templateEngine.process("/mail/activation", context);
            mailClient.sendMail(user.getEmail(),"牛客网-激活邮件",content);
        }
        return map;
    }

    public int Activation(String userId,String code){
        User user = userMapper.selectUserById(Integer.parseInt(userId));
        if(user.getStatus()==1){
            return CommunityConstant.ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatusById(1,user.getId());
            return CommunityConstant.ACTIVATION_SUCCESS;
        }else{
            return CommunityConstant.ACTIVATION_FALIURE;
        }
    }


}
