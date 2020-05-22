package com.wangchu.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.Event;
import com.wangchu.dal.entity.Message;
import com.wangchu.dao.mapper.MessageMapper;
import com.wangchu.service.DiscussPostService;
import com.wangchu.service.ElasticsearchService;
import com.wangchu.util.CommonUtils;
import com.wangchu.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer {
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    ElasticsearchService elasticsearchService;
    @Autowired
    DiscussPostService discussPostService;
    @Value("${wk.image.command}")
    private String wkCommand;
    @Value("${wk.image.storage}")
    private String storagePath;
    @Value("${qiniu.access.key}")
    private String qiniuAccessKey;
    @Value("${qiniu.secret.key}")
    private String qiniuSecretKey;
    @Value("${qiniu.shareBucket.name}")
    private String qiniuShareName;
    @Value("${qiniu.shareBucket.url}")
    private String qiniuShareUrl;

    @Autowired
    ThreadPoolTaskScheduler taskScheduler;

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = {CommunityConstant.TOPIC_COMMENT,CommunityConstant.TOPIC_LIKE,CommunityConstant.TOPIC_FOLLOW})
    public void handleMessage(ConsumerRecord record){
        if(record==null||record.value()==null) {
            logger.error("消息为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null) {
            logger.error("事件为空");
            return;
        }
        Message message = new Message();
        message.setFromId(CommunityConstant.SYSTEM_COMMENT_ID);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());

        Map<String,Object> content = new HashMap<>();
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        content.put("userId",event.getUserId());
        if(event.getData()!=null&&!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageMapper.insertMessage(message);
    }

    //消费发帖事件
    @KafkaListener(topics = {CommunityConstant.TOPIC_PUBLISH})
    public void handlePublish(ConsumerRecord record){
        if(record==null||record.value()==null) {
            logger.error("消息为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null) {
            logger.error("事件为空");
            return;
        }
        //帖子发布的时候已经插入数据库
        DiscussPost post = discussPostService.selectOneDiscussPost(event.getEntityId());
        elasticsearchService.saveDicusspost(post);
    }

    //消费删除帖子事件
    @KafkaListener(topics = {CommunityConstant.TOPIC_DELETEPOST})
    public void handleDelete(ConsumerRecord record){
        if(record==null||record.value()==null) {
            logger.error("消息为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null) {
            logger.error("事件为空");
            return;
        }
        elasticsearchService.deleteDicusspost(event.getEntityId());}

    //消费存储长图
    @KafkaListener(topics = {CommunityConstant.TOPIC_SHARE})
    public void handleShare(ConsumerRecord record){
        if(record==null||record.value()==null) {
            logger.error("消息为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null) {
            logger.error("事件为空");
            return;
        }
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        try {
            Runtime.getRuntime().exec(wkCommand+" --quality 75 "+htmlUrl+" "+storagePath+fileName+suffix);
            logger.info("生成长图成功");
        } catch (IOException e) {
            logger.info("生成长图失败");
            e.printStackTrace();
        }
        UploadTask task = new UploadTask(fileName,suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable{

        private String fileName;
        private String suffix;
        private Future future;

        private long startTime;
        private int uploadTimes = 0;

        public UploadTask(String fileName,String suffix){
            this.fileName = fileName;
            this.suffix =suffix;
            startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }


        @Override
        public void run() {
            //超时或者超次数，报告失败
            if(System.currentTimeMillis()-startTime>30000){
                logger.error(String.format("超时,上传失败,第%d次,file:[%s]",uploadTimes,fileName));
                future.cancel(true);
                return;
            }
            if(uploadTimes>3){
                logger.error("超出上传次数,上传失败,第%d次,file:[%s]",uploadTimes,fileName);
                future.cancel(true);
                return;
            }
            String path = storagePath+"/"+fileName+suffix;
            File file = new File(path);
            if(file.exists()){
                logger.info(String.format("开始第%d次上传[%s]",++uploadTimes,fileName));
                StringMap policy = new StringMap();
                policy.put("returnBody", CommonUtils.getJSONString(0));
                Auth auth = Auth.create(qiniuAccessKey,qiniuSecretKey);
                String uploadToken = auth.uploadToken(qiniuShareName,fileName, 3600, policy);
                //指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone2()));
                try {
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/png" , false);
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if(json==null||json.get("code")==null||!json.get("code").toString().equals("0")){
                        logger.error(String.format("第%d次上传文件[%s]失败",uploadTimes,fileName));
                    }else{
                        logger.info(String.format("第%d次上传文件[%s]成功",uploadTimes,fileName));
                        future.cancel(true);
                        return;
                    }
                } catch (QiniuException e) {
                    logger.error(String.format("第%d次上传文件[%s]失败",uploadTimes,fileName));
                    e.printStackTrace();
                }
            }else{
             logger.info("等待图片生成...["+fileName+"]");
            }
        }
    }

}

