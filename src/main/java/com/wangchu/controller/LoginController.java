package com.wangchu.controller;

import com.google.code.kaptcha.Producer;
import com.wangchu.dal.entity.User;
import com.wangchu.service.UserService;
import com.wangchu.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController {
    @Autowired
    UserService userService;
    @Autowired
    Producer kaptcha;

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

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        String text = kaptcha.createText();
        session.setAttribute("kaptcha",text);
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



}
