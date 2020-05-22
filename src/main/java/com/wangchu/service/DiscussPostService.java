package com.wangchu.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dao.mapper.DiscussPostMapper;
import com.wangchu.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    @Autowired
    DiscussPostMapper discussPostMapper;
    @Autowired
    SensitiveFilter filter;
    @Value("${caffeine.posts.max-size}")
    private int caffeineMaxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private long caffeineExpireSeconds;
    private final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    private LoadingCache<String,List<DiscussPost>> postsCache;
    private LoadingCache<Integer,Integer> rowsCache;

    @PostConstruct
    public void init(){
        postsCache = Caffeine.newBuilder().maximumSize(caffeineMaxSize).expireAfterWrite(caffeineExpireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key==null||key.length()==0) throw new RuntimeException("key值为空");
                        String[] split = key.split(":");
                        int offset = Integer.valueOf(split[0]);
                        int limit = Integer.valueOf(split[1]);
                        logger.info("load post list from DB.");
                        return discussPostMapper.selectDiscussPostByUserId(0,offset,limit,1);
                    }
                });
        rowsCache = Caffeine.newBuilder().maximumSize(caffeineMaxSize).expireAfterWrite(caffeineExpireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        if(key==null&&key!=0) throw new RuntimeException("key值为空");
                        logger.info("load post list from DB.");
                        return discussPostMapper.selectDiscussPostNumByUserId(0);
                    }
                });
    }

    public List<DiscussPost> findDiscussPostByUserId(int userid,int offset,int limit,int orderMode){
        if(userid==0&&orderMode==1){
            String key = offset + ":" +limit;
            return postsCache.get(key);
        }
        logger.info("load post list from DB.");
        return discussPostMapper.selectDiscussPostByUserId(userid,offset,limit,orderMode);
    }

    public int findDiscussPostNumByUserId(int userid){
        if(userid==0){
            return rowsCache.get(1);
        }
        logger.info("load post list from DB.");
        return discussPostMapper.selectDiscussPostNumByUserId(userid);
    }

    public int insertDiscustPost(DiscussPost post){
        if(post==null) throw new IllegalArgumentException("参数不能为空");
        //1.转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //2.过滤敏感词
        post.setTitle(filter.filter(post.getTitle()));
        post.setContent(filter.filter(post.getContent()));

        int i = discussPostMapper.insertDiscussPost(post);
        return i;
    }

    public DiscussPost selectOneDiscussPost(int id){
        return discussPostMapper.selectOneDiscussPost(id);
    }

    public int updateCommentCount(int postId,int count){
        return discussPostMapper.updateCommentCount(postId,count);
    }

    public int updateType(int postId,int type){
        return discussPostMapper.updateType(postId,type);
    }

    public int updateStatus(int postId,int status){
        return discussPostMapper.updateStatus(postId,status);
    }

    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }
}
