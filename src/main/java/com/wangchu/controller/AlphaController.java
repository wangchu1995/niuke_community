package com.wangchu.controller;

import com.wangchu.service.DiscussPostService;
import com.wangchu.service.UserService;
import com.wangchu.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    UserService userService;
    @Autowired
    DiscussPostService discussPostService;

    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    public String Ajax(String name,int age){
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("age",age);
        String jsonString = CommonUtils.getJSONString(0, "发送成功", map);
        return jsonString;
    }


}
