package com.wangchu.service;

import com.wangchu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    RedisTemplate redisTemplate;

    /**
     *
     * @param userId  点赞帖子/评论的用户id
     * @param entityType 帖子/评论类型
     * @param entityId  帖子/评论的id
     * @param entityUserId 帖子/评论发布人的id,用于统计发布人收到了多少赞
     */
    public void like(int userId,int entityType,int entityId,int entityUserId){
//        //点赞：获取Key,判断点没点过
//        String key = RedisKeyUtil.getLikeKey(entityType,entityId);
//        //key:type:id--->userId
//        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
//        if(isMember){
//            //点过
//            redisTemplate.opsForSet().remove(key,userId);
//        }else {
//            redisTemplate.opsForSet().add(key,userId);
//        }

        //检查是否点过赞
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String likeKey = RedisKeyUtil.getLikeKey(entityType,entityId);
                String userKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean isMember = operations.opsForSet().isMember(likeKey, userId);
                operations.multi();
                if(!isMember){
                    operations.opsForSet().add(likeKey,userId);
                    operations.opsForValue().increment(userKey);
                }else{
                    operations.opsForSet().remove(likeKey,userId);
                    operations.opsForValue().decrement(userKey);
                }
                return operations.exec();
            }
        });
    }

    //查询帖子/评论点赞数量
    public long findLikeCount(int entityType,int entityId){
        String key = RedisKeyUtil.getLikeKey(entityType,entityId);
        Long size = redisTemplate.opsForSet().size(key);
        return size;
    }
    //查询用户的总点赞数量
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Object count =  redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:(int)count;
    }

    //查询某人对某实体点赞状态
    public int findLikeStatus(int userId,int entityType,int entityId){
        String key = RedisKeyUtil.getLikeKey(entityType,entityId);
        Boolean member = redisTemplate.opsForSet().isMember(key, userId);
        return member?1:0;
    }
}
