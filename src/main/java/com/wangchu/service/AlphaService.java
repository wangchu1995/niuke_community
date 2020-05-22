package com.wangchu.service;

import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.User;
import com.wangchu.dao.mapper.DiscussPostMapper;
import com.wangchu.dao.mapper.UserMapper;
import com.wangchu.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.Random;

@Service
public class AlphaService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    private final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    @Transactional(isolation = Isolation.READ_UNCOMMITTED,propagation = Propagation.REQUIRED)
    public String save01(){
        //1.创建用户
        User user = new User();
        user.setUsername("alpha");
        user.setPassword("123");
        user.setSalt(CommonUtils.getUUID().substring(0, 5));
        user.setPassword(CommonUtils.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommonUtils.getUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertOneUser(user);
        //2.发布帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("test transaction");
        post.setContent("想见你");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);
        //人造错误
        Integer.valueOf("abc");
        return "ok";
    }

    public String save02(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<String>() {
            @Override
            public String doInTransaction(TransactionStatus transactionStatus) {
                //1.创建用户
                User user = new User();
                user.setUsername("alpha");
                user.setPassword("123");
                user.setSalt(CommonUtils.getUUID().substring(0, 5));
                user.setPassword(CommonUtils.md5(user.getPassword() + user.getSalt()));
                user.setType(0);
                user.setStatus(0);
                user.setActivationCode(CommonUtils.getUUID());
                user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
                user.setCreateTime(new Date());
                userMapper.insertOneUser(user);
                //2.发布帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("test transaction");
                post.setContent("想见你");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);
                //人造错误
                Integer.valueOf("abc");
                return "ok";
            }
        });
    }

    @Async
    public void execute(){
        logger.info("多线程异步调用，不用写runnable");
    }

//    @Scheduled(initialDelay = 10000,fixedRate = 1000)
//    public void execute02(){
//        logger.info("自动定时任务，啥也不用管");
//    }
}
