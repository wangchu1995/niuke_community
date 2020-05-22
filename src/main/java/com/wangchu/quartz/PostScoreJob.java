package com.wangchu.quartz;

import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.service.DiscussPostService;
import com.wangchu.service.ElasticsearchService;
import com.wangchu.service.LikeService;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreJob implements Job {
    //定时取出缓存中的更改热点数据，重新统计分数
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    LikeService likeService;
    @Autowired
    ElasticsearchService elasticsearchService;
    //常量,时间初始纪元,计算分数用
    private static final Date epoch;
    static {
        //只初始化初始纪元一次
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化纪元失败",e);
        }

    }


    private final Logger logger = LoggerFactory.getLogger(PostScoreJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScore();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if(operations==null||operations.size()==0){
            logger.info("没有需要重新统计分数的帖子");
            return;
        }
        Long size = operations.size();
        while(operations.size()>0){
            logger.info("任务开始,正在刷新帖子分数");
            this.refresh((Integer)operations.pop());
        }
        logger.info("任务结束，刷新完成帖子分数  "+size);
    }

    private void refresh(int postId){
        DiscussPost post = discussPostService.selectOneDiscussPost(postId);
        if(post==null){
            //帖子算分之前被管理员删除
            logger.info("帖子不存在 ");
            return;
        }
        boolean isWonderful = post.getType()==1?true:false;
        int commentCount = post.getCommentCount();
        long likeCount = likeService.findLikeCount(CommunityConstant.ENTITY_TYPE_POST,postId);
        double w = (isWonderful?75:0)+commentCount*10+likeCount*2;
        double score = Math.log10(Math.max(w,1)+(
                (post.getCreateTime().getTime()-epoch.getTime())/(1000*3600*24)
                ));
        post.setScore(score);
        discussPostService.updateScore(postId,score);
        elasticsearchService.saveDicusspost(post);
    }
}
