package com.wangchu.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.wangchu.annotation.LoginRequired;
import com.wangchu.dal.entity.Comment;
import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.Page;
import com.wangchu.dal.entity.User;
import com.wangchu.service.*;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @Value("${community.path.upload}/")
    String uploadPath;
    @Autowired
    LikeService likeService;
    @Autowired
    FollowService followService;
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    CommentService commentService;
    @Value("${qiniu.access.key}")
    private String qiniuAccessKey;
    @Value("${qiniu.secret.key}")
    private String qiniuSecretKey;
    @Value("${qiniu.headerBucket.name}")
    private String qiniuHeaderName;
    @Value("${qiniu.headerBucket.url}")
    private String qiniuHeaderUrl;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    //获取用户设置页面
    //因为在用户设置页面上传头像，所以七牛云的配置参数都在这里提前设置好，发给七牛云
    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        String fileName = CommonUtils.getUUID();
        //获取上传七牛的配置信息，发给浏览器
        StringMap policy = new StringMap();
        //用户上传成功，七牛与返回json,在这里设置
        policy.put("returnBody", CommonUtils.getJSONString(0));
        Auth auth = Auth.create(qiniuAccessKey,qiniuSecretKey);
        //设置返回给页面的key的有效时间
        String uploadToken = auth.uploadToken(qiniuHeaderName, fileName, 3600, policy);
        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);
        return "/site/setting";
    }

    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeader(String fileName){
        if(StringUtils.isBlank(fileName)){
            throw new RuntimeException("文件名不能为空！");
        }
        String url = qiniuHeaderUrl+"/"+fileName;
        userService.updateUserHeaderUrl(url,hostHolder.getUsers().getId());
        return CommonUtils.getJSONString(0);
    }

    //上传头像
    ///废弃，改为直接上传给七牛云服务器，而不是存储在服务器上了
    //请求改为通知你更改header路径
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


    //废弃，获取头像去七牛云获取
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

    //修改密码
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

    //获取用户详情页面
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId,Model model){
        User user = userService.selectUserById(userId);
        model.addAttribute("user",user);
        int userLikeCount = likeService.findUserLikeCount(user.getId());
        model.addAttribute("userLikeCount",userLikeCount);
        //查询关注数
        long followeeNum = followService.findFolloweeNum(user.getId(), CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeNum",followeeNum);
        //查询粉丝数
        long followerNum = followService.findFollowerNum(user.getId(), CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followerNum",followerNum);
        //查询是否关注
        boolean hasFollowed = false;
        if(hostHolder.getUsers()!=null){
            hasFollowed = followService.findHasFollowed(hostHolder.getUsers().getId(),CommunityConstant.ENTITY_TYPE_USER,user.getId());
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

    //查询我的帖子
    @RequestMapping(path = "/profile/my-post",method = RequestMethod.GET)
    public String getMyPost(Page page, Model model){
        User user = hostHolder.getUsers();
        //设置分页
        model.addAttribute("user",user);
        page.setPath("/profile/my-post");
        page.setTotalItems(discussPostService.findDiscussPostNumByUserId(user.getId()));
        page.setShowItems(5);

        //帖子查询,处理
        int postNumByUserId = discussPostService.findDiscussPostNumByUserId(user.getId());
        model.addAttribute("postNum",postNumByUserId);
        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        List<DiscussPost> discussPostList = discussPostService.findDiscussPostByUserId(user.getId(),page.getOffset(),page.getShowItems(),0);
        if(discussPostList!=null){
            for(DiscussPost discussPost:discussPostList){
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("post",discussPost);
                map.put("user",user);
                long likeCount = likeService.findLikeCount(CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());
                int likeStatus = user==null?0:likeService.findLikeStatus(user.getId(),CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());
                map.put("postLikeCount",likeCount);
                map.put("postLikeStatus",likeStatus);
                list.add(map);
            }
        }

        model.addAttribute("postlist",list);
        model.addAttribute("page",page);
        return "/site/my-post";
    }

    //查询我的回复
    @RequestMapping(path = "/profile/my-reply",method = RequestMethod.GET)
    public String getMyReply(Page page, Model model){
        User user = hostHolder.getUsers();
        model.addAttribute("user",user);
        //处理分页
        page.setPath("/profile/my-reply");  //分页的访问路径虽然相同，但是每次访问携带的page不同
        page.setShowItems(5);
        page.setTotalItems(commentService.selectCountByUserId(user.getId(),CommunityConstant.ENTITY_TYPE_COMMENT));

        //处理评论
        List<Map<String,Object>> list = new ArrayList<>();
        List<Comment> comments = commentService.selectCommentByUserId(user.getId(),CommunityConstant.ENTITY_TYPE_COMMENT,page.getOffset(),page.getShowItems());
        if(comments!=null){
            for(Comment c:comments){
                Map<String,Object> map = new HashMap<>();
                map.put("comment",c);
                int entityId = c.getEntityId();
                //查询的就是帖子，mapper里面写着条件entityType=1
                DiscussPost discussPost = discussPostService.selectOneDiscussPost(entityId);
                map.put("post",discussPost);
                list.add(map);
            }
        }
        model.addAttribute("cMapList",list);
        model.addAttribute("commentsNum",page.getShowItems());

        return "/site/my-reply";
    }
}
