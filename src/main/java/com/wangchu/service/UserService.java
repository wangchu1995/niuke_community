package com.wangchu.service;

import com.wangchu.dal.entity.LoginTicket;
import com.wangchu.dal.entity.User;
import com.wangchu.dao.mapper.LoginTicketMapper;
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
    @Autowired
    LoginTicketMapper loginTicketMapper;
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

    public Map<String,Object> login(String username,String password,boolean rememberme){
        //验证码在session域，在controller中判断
        Map<String,Object> map = new HashMap<String, Object>();
        //1.判空判断
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //2.用户名密码判断
        User user = userMapper.selectUserByUsername(username);
        if(user==null){
            map.put("usernameMsg","用户名不存在");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","用户未激活");
            return map;
        }
        System.out.println(CommonUtils.md5(password+user.getSalt()));
        if(!user.getPassword().equals(CommonUtils.md5(password+user.getSalt()))){
            map.put("passwordMsg","密码不正确");
            return map;
        }

        //3.创建ticket登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        String ticket = CommonUtils.getUUID();
        loginTicket.setTicket(ticket);
        map.put("ticket",ticket);
        int ticketSeconds = rememberme?CommunityConstant.LONG_TICKETTIME:CommunityConstant.DEFAULT_TICKETTIME;
        map.put("expired",ticketSeconds);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+ticketSeconds*1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateLoginTicketStatus(ticket,1);
    }

    public Map<String,Object> sendVerificationMail(String mail,String kaptcha) throws MessagingException {
        Map<String,Object> map = new HashMap<String, Object>();
        //1.空值判断
        if(StringUtils.isBlank(mail)){
            map.put("mailMsg","邮箱为空值");
            return map;
        }
        //2.查询邮箱用户
        User user = userMapper.selectUserByEmail(mail);
        if(user==null){
            map.put("mailMsg","用户邮箱不存在");
            return map;
        }
        //3.发送验证邮件
        Context context = new Context();
        context.setVariable("kaptcha", kaptcha);
        context.setVariable("mail",mail);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(user.getEmail(),"牛客网-忘记密码验证",content);
        return map;
    }

    public Map<String,Object> forget(String mail,String code,String newPassword){
        Map<String,Object> map = new HashMap<String, Object>();
        //1.空值判断
        if(StringUtils.isBlank(mail)){
            map.put("mailMsg","邮箱为空值");
            return map;
        }
        if(StringUtils.isBlank(code)){
            map.put("codeMsg","验证码为空值");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("passwordMsg","密码为空值");
            return map;
        }
        //2.查询邮箱用户
        User user = userMapper.selectUserByEmail(mail);
        if(user==null){
            map.put("mailMsg","用户邮箱不存在");
            return map;
        }
        //3.修改密码
        String password = CommonUtils.md5(newPassword+user.getSalt());
        userMapper.updatePasswordById(password,user.getId());
        return map;
    }
}
