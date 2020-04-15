package com.wangchu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController {

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String goToRegister(){
        return "/site/register.html";
    }

}
