package com.wangchu.service;

import com.wangchu.dal.entity.LoginTicket;
import com.wangchu.dal.entity.User;
import com.wangchu.dao.mapper.UserMapper;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.MailClient;
import com.wangchu.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;
//    @Autowired
//    LoginTicketMapper loginTicketMapper;
    @Value("${community.context-path}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    MailClient mailClient;
    @Autowired
    RedisTemplate redisTemplate;


    public User selectUserById(int id){
//        User user = userMapper.selectUserById(id);
        User user = getRedisUser(id);
        if(user==null){
            user=initRedisUser(id);
        }
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
//        User user = userMapper.selectUserById(Integer.parseInt(userId));
        User user = getRedisUser(Integer.parseInt(userId));
        if(user==null){
            user=initRedisUser(Integer.parseInt(userId));
        }
        if(user.getStatus()==1){
            return CommunityConstant.ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            delRedisUser(Integer.parseInt(userId));
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

        //3.创建ticket登录凭证,存储入redis(之前是mysql)
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        String ticket = CommonUtils.getUUID();
        loginTicket.setTicket(ticket);
        map.put("ticket",ticket);
        int ticketSeconds = rememberme?CommunityConstant.LONG_TICKETTIME:CommunityConstant.DEFAULT_TICKETTIME;
        map.put("expired",ticketSeconds);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+ticketSeconds*1000));
        String redisKey = RedisKeyUtil.getLoginTicket(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket,3600, TimeUnit.SECONDS);
//        loginTicketMapper.insertLoginTicket(loginTicket);


        return map;
    }

    public void logout(String ticket){
        String redisKey = RedisKeyUtil.getLoginTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket,3600, TimeUnit.SECONDS);
//        loginTicketMapper.updateLoginTicketStatus(ticket,1);

        //退出登录，清除Security中的数据
        SecurityContextHolder.clearContext();
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

    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getLoginTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
//        LoginTicket loginTicket = loginTicketMapper.selectLoginTicketByTicket(ticket);
        return loginTicket;
    }

    public int updateUserHeaderUrl(String url,int userId){
        delRedisUser(userId);
        return userMapper.updataHeaderUrlById(url,userId);
    }

    public Map<String,Object> updatePassword(String oldPassword,String newPassword,int userId){
        User user = getRedisUser(userId);
        if(user==null){
            user=initRedisUser(userId);
        }
        Map<String,Object> map = new HashMap<String, Object>();
        oldPassword = CommonUtils.md5(oldPassword+user.getSalt());
        if(!user.getPassword().equals(oldPassword)){
            map.put("oldPasswordMsg","旧密码验证错误");
            return map;
        }
        newPassword = CommonUtils.md5(newPassword+user.getSalt());
        userMapper.updatePasswordById(newPassword,userId);
        delRedisUser(userId);
        return map;
    }

    public User findUserByName(String username){
        return userMapper.selectUserByUsername(username);
    }

    //1.缓存取值
    public User getRedisUser(int userId){
        String redisKey =RedisKeyUtil.getUserKey(userId);
        User user = (User) redisTemplate.opsForValue().get(redisKey);
        return user;
    }
    //2.缓存初始化
    public User initRedisUser(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        User user = userMapper.selectUserById(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600,TimeUnit.SECONDS);
        return user;
    }
    //3.修改删除缓存
    public void delRedisUser(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    //获取用户的权限列表
    public Collection<? extends GrantedAuthority> getAuthorites(int userId){
        User user = userMapper.selectUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 2:
                        return CommunityConstant.AUTHORITY_MODERATOR;
                    case 1:
                        return CommunityConstant.AUTHORITY_ADMIN;
                    default:
                        return CommunityConstant.AUTHORITY_USER;
                }
            }
        };
        list.add(grantedAuthority);
        return list;
    }
}
