package com.wangchu.service;

import com.wangchu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    @Autowired
    RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //储存UV
    public void recordUV(String ip){
        redisTemplate.opsForHyperLogLog().add(RedisKeyUtil.getUVKey(df.format(new Date())),ip);
    }
    //统计UV
    public long calculateUV(Date start,Date end){
        //1.判空
        if(start==null||end==null) throw new IllegalArgumentException("参数为空");
        //整理日期范围内的KEY
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        //合并数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(start),df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    //储存DAU
    public void recordDAU(int userId){
        String key = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(key,userId,true);
    }
    //统计DAU
    public long calculateDAU(Date start,Date end){
        //1.判空
        if(start==null||end==null) throw new IllegalArgumentException("参数为空");
        //整理日期范围内的KEY
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        //合并数据
        String redisKey = RedisKeyUtil.getDAUKey(df.format(start),df.format(end));
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(),
                        keyList.toArray(new byte[0][0]));
                return  connection.bitCount(redisKey.getBytes());
            }
        });
        return (long)obj;
    }
}
