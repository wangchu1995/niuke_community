package com.wangchu.controller;

import com.google.code.kaptcha.Producer;
import com.wangchu.dal.entity.User;
import com.wangchu.service.UserService;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {
    @Autowired
    UserService userService;
    @Autowired
    Producer kaptcha;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    RedisTemplate redisTemplate;

    private  static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String register(){
        return "site/register";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user) throws MessagingException {

        Map<String,Object> map = userService.register(user);
        if(map==null||map.isEmpty()){
            //注册成功,跳转提示页面
            model.addAttribute("msg","注册成功，请登录邮箱激活账号");
            model.addAttribute("target","/index");
            return "site/operate-result";
        }else{
            //注册失败,跳转登录页面
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "site/register";
        }


    }

    @RequestMapping(path = "/activation/{userId}/{code}")
    public String activation(@PathVariable("userId")String userId,@PathVariable("code") String code,Model model){
        int result = userService.Activation(userId,code);
        if(result== CommunityConstant.ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，请登录账号");
            model.addAttribute("target","/login");
        }else if(result==CommunityConstant.ACTIVATION_REPEAT){
            model.addAttribute("msg","您的账号已经激活，请不要重复激活账号");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，验证码不正确");
            model.addAttribute("target","/index");
        }
        return "site/operate-result";

    }

    @RequestMapping(path = "/login")
    public String login(){

        return "site/login";
    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,Model model,HttpSession session,HttpServletResponse response,
                        @CookieValue("kaptcha")String kaptchaString){
        //1.检查验证码
//        String storeCode = (String) session.getAttribute("kaptcha");
        String storeCode = null;
        if(StringUtils.isNotBlank(kaptchaString)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaString);
            storeCode = (String) redisTemplate.opsForValue().get(redisKey);
        }


        if(StringUtils.isBlank(code)||!code.equalsIgnoreCase(storeCode)||StringUtils.isBlank(storeCode)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        //2.检查用户名，密码
        Map<String,Object> map = userService.login(username,password,rememberme);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge((Integer) map.get("expired"));
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        String text = kaptcha.createText();
//        session.setAttribute("kaptcha",text);
        String kaptchaSting = CommonUtils.getUUID();
        Cookie cookie = new Cookie("kaptcha",kaptchaSting);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        String redisKey  = RedisKeyUtil.getKaptchaKey(kaptchaSting);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        BufferedImage image = kaptcha.createImage(text);
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("验证码生成错误");
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/index";
    }

    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String forget(){
        return "/site/forget";
    }

    //登录时,忘记密码，邮箱找回功能--->发送验证码邮件
    @RequestMapping(path="/passwordKaptcha",method = RequestMethod.GET)
    public String passwordKptcha(Model model,String mail,HttpSession session) throws MessagingException {
        String text = kaptcha.createText();
        session.setAttribute("passwordKaptcha",text);
        Map<String, Object> map = userService.sendVerificationMail(mail,text);
        //回写信息
        if(map!=null){
            model.addAttribute("mailMsg",map.get("mailMsg"));
        }
        return "/site/forget::mail";
    }

    //登录时,忘记密码，邮箱找回功能--->验证并找回密码
    @RequestMapping(path = "/forget",method = RequestMethod.POST)
    public String forget(HttpSession session,String mail,String code,String newPassword,Model model){
        //1.判空
        if(StringUtils.isBlank(code)){
            model.addAttribute("codeMsg","验证码为空");
            return "/site/forget";
        }
        String passwordKaptcha = (String) session.getAttribute("passwordKaptcha");
        if(!passwordKaptcha.equals(code)){
            model.addAttribute("codeMsg","验证码错误");
            return "/site/forget";
        }
        Map<String, Object> map = userService.forget(mail, code, newPassword);
        if(map==null||map.isEmpty()){
            return "redirect:/index";
        }
        for(String key:map.keySet()){
            model.addAttribute(key,map.get(key));
        }
        return "/site/forget";

    }



}
