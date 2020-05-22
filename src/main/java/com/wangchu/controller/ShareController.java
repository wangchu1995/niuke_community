package com.wangchu.controller;

import com.wangchu.dal.entity.Event;
import com.wangchu.event.EventProducer;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController {
    @Autowired
    EventProducer eventProducer;
    @Value("${community.context-path}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${wk.image.storage}")
    private String storagePath;
    @Value("${qiniu.shareBucket.name}")
    private String qiniuShareName;
    @Value("${qiniu.shareBucket.url}")
    private String qiniuShareUrl;

    private final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @RequestMapping(path = "/share",method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl){
        //异步生成长图
        String fileName = CommonUtils.getUUID();
        Event event = new Event()
                .setTopic(CommunityConstant.TOPIC_SHARE)
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                .setData("suffix",".png");
        eventProducer.sendMessage(event);
        //需要给请求者返回可以访问长图的访问路径
        Map<String,Object> map = new HashMap<>();
//        map.put("shareUrl",domain+contextPath+"/share/image/"+fileName);
        map.put("shareUrl",qiniuShareUrl+"/"+fileName);
        return CommonUtils.getJSONString(0,null,map);
    }

    //获取长图的方法，废弃，改为七牛云
    @RequestMapping(path = "/share/image/{fileName}",method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //从本地读取长图返回图片信息给请求者
        if(StringUtils.isBlank(fileName)) throw new IllegalArgumentException("文件名不能为空！");
        try {
            response.setContentType("image/png");
            File file = new File(storagePath+fileName+".png");
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            int b = 0;
            byte[] buffer = new byte[1024];
            while((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败");
            e.printStackTrace();
        }

    }
}
