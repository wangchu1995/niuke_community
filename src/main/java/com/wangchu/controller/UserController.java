package com.wangchu.controller;

import com.wangchu.annotation.LoginRequired;
import com.wangchu.dal.entity.User;
import com.wangchu.service.LikeService;
import com.wangchu.service.UserService;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    HostHolder hostHolder;
    @Value("${community.context-path}")
    String domain;
    @Value("${server.servlet.context-path}")
    String contextPath;
    @Value("${community.path.upload}")
    String uploadPath;
    @Autowired
    LikeService likeService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeaderUrl(MultipartFile headerImage,Model model){
        //1.判空处理
        if(headerImage==null){
            model.addAttribute("error","上传文件为空");
            return "/site/setting";
        }
        //2.文件后缀判断
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //3.生成随机文件名
        String fileName = CommonUtils.getUUID()+suffix;

        try {
            //4.确定文件存储路径
            File file = new File(uploadPath+fileName);
            //5.存储文件
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.debug("存储文件错误"+e);
        }
        //6.修改用户头像地址
        User user = hostHolder.getUsers();
        String url = domain+contextPath+"/user/header/"+fileName;
        userService.updateUserHeaderUrl(url,user.getId());
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) throws IOException {
        //1.服务存放路径
        fileName = uploadPath+fileName;
        //2.解析后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/"+suffix.replace(".",""));
        String contentType = response.getContentType();
        //3.响应图片
        OutputStream os = null;
        FileInputStream fis = null;
        try {
            os = response.getOutputStream();
            fis = new FileInputStream(fileName);
            int b=0;
            byte[] buffer = new byte[1024];
            while((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            os.close();
            fis.close();
        }
    }

    @RequestMapping(path = "/password",method = RequestMethod.POST)
    public String updatePassword(String oldPassword,String newPassword,Model model){
        //1.判空
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg","旧密码为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg","新密码为空");
            return "/site/setting";
        }
        //2.调用修改密码的业务层
        User user = hostHolder.getUsers();
        Map<String, Object> map = userService.updatePassword(oldPassword, newPassword, user.getId());
        if(map==null||map.isEmpty()){
            return "redirect:/logout";
        }else{
            for(String key:map.keySet()){
                model.addAttribute(key,map.get(key));
            }
        }
        return "/site/setting";
    }

    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId,Model model){
        User user = userService.selectUserById(userId);
        model.addAttribute("user",user);
        int userLikeCount = likeService.findUserLikeCount(user.getId());
        model.addAttribute("userLikeCount",userLikeCount);
        return "/site/profile";
    }
}
