package com.wangchu.service;

import com.wangchu.dal.entity.User;
import com.wangchu.util.CommunityConstant;
import com.wangchu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    UserService userService;

    //关注
    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                redisTemplate.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey,userId,System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    //取消关注
    public void unfollow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                redisTemplate.opsForZSet().remove(followeeKey,entityId,System.currentTimeMillis());
                redisTemplate.opsForZSet().remove(followerKey,userId,System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    //查询某用户已关注的某实体的数量
    public long findFolloweeNum(int userId,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询某实体的关注的用户数量
    public long findFollowerNum(int entityId,int entityType){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询某实体的已关注状态
    public boolean findHasFollowed(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }

    //查询某用户的关注用户列表
    public List<Map<String,Object>> findFollowees(int userId,int offset,int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, CommunityConstant.ENTITY_TYPE_USER);
        Set<Integer> set = redisTemplate.opsForZSet().range(followeeKey, offset, offset + limit - 1);
        List<Map<String,Object>> list = new ArrayList<>();
        if(set!=null){
            for(Integer targetId:set){
                Map<String,Object> map = new HashMap<>();
                User user = userService.selectUserById(targetId);
                map.put("user",user);
                Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
                map.put("followTime",new Date(score.longValue()));
                list.add(map);
            }
        }
        return list;
    }

    //查询某用户的粉丝列表
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(CommunityConstant.ENTITY_TYPE_USER,userId);
        Set<Integer> set = redisTemplate.opsForZSet().range(followerKey, offset, offset + limit - 1);
        List<Map<String,Object>> list = new ArrayList<>();
        if(set!=null){
            for(Integer targetId:set){
                Map<String,Object> map = new HashMap<>();
                User user = userService.selectUserById(targetId);
                map.put("user",user);
                Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
                map.put("followTime",new Date(score.longValue()));
                list.add(map);
            }
        }
        return list;
    }
}
