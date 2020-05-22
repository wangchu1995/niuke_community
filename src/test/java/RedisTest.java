import com.wangchu.WCApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WCApplication.class)
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
        String redisKey = "test:string";
        redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.set(2);
        System.out.println(operations.get());
        System.out.println(operations.increment());
        System.out.println(operations.decrement());
    }

    @Test
    public void testHash(){
        String redisKey = "test:hash";
        BoundHashOperations operations = redisTemplate.boundHashOps(redisKey);
        operations.put("id",1);
        operations.put("name","zhangsan");
        System.out.println(operations.get("name"));
    }

    @Test
    public void testLists(){
        String redisKey = "test:list";
        BoundListOperations operations = redisTemplate.boundListOps(redisKey);
        operations.leftPush("101");
        operations.leftPush("102");
        operations.leftPush("103");
        System.out.println(operations.size());
        System.out.println(operations.index(2));
        System.out.println(operations.range(0,2));
        System.out.println(operations.leftPop());
    }

    @Test
    public void testSet(){
        String redisKey = "test:set";
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        operations.add("内蒙古","北京","上海","深圳");
        System.out.println(operations.pop());
        System.out.println(operations.size());
        System.out.println(operations.members());
    }

    @Test
    public void testZSet(){
        String redisKey = "test:zset";
        BoundZSetOperations operations = redisTemplate.boundZSetOps(redisKey);
        operations.add("wangwu",5);
        operations.add("zhangsan",3);
        operations.add("lisi",4);
        System.out.println(operations.zCard());
        System.out.println(operations.score("wangwu"));
        System.out.println(operations.rank("wangwu"));
        System.out.println(operations.range(0,2));
    }

    @Test
    public void global(){
        redisTemplate.delete("test:string");
        redisTemplate.hasKey("test:string");
        redisTemplate.expire("test:list",10, TimeUnit.SECONDS);
    }

    @Test
    public void testTransaction(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                operations.multi();
                operations.opsForValue().set(redisKey,"事务1");
                System.out.println(redisTemplate.opsForValue().get(redisKey));

                List exec = operations.exec();
                System.out.println(redisTemplate.opsForValue().get(redisKey));
                return exec;
            }
        });
        System.out.println(obj);
    }

    @Test
    public void testHyperLogLog(){
        //统计独立总数
        String redisKey01 = "test:hhl:01";
        for (int i = 1; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey01,i);
        }
        for (int i = 1; i < 100000; i++) {
            int r = new Random().nextInt(100000)+1;
            redisTemplate.opsForHyperLogLog().add(redisKey01,r);
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey01));
    }

    @Test
    public void testHyperLogLog01(){
        //统计独立总数
        String redisKey02 = "test:hhl:02";
        for (int i = 1; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey02,i);
        }

        String redisKey03 = "test:hhl:03";
        for (int i = 5001; i < 150000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey03,i);
        }

        String redisKey04 = "test:hhl:04";
        for (int i = 10001; i < 200000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey04,i);
        }

        String redisKey = "test:hhl:union";
        redisTemplate.opsForHyperLogLog().union(redisKey,redisKey02,redisKey03,redisKey04);
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));

    }

    @Test
    public void testBitMap(){
        //位存储 ,存储获取统计
        String redisKey = "test:bm:01";
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,4));

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }

    @Test
    public void testBitMap02(){
        //位存储 ,存储获取统计
        String redisKey01 = "test:bm:01";
        redisTemplate.opsForValue().setBit(redisKey01,1,true);
        redisTemplate.opsForValue().setBit(redisKey01,2,true);
        String redisKey02 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey02,2,true);
        redisTemplate.opsForValue().setBit(redisKey02,3,true);
        redisTemplate.opsForValue().setBit(redisKey02,4,true);
        String redisKey03 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey03,3,true);
        redisTemplate.opsForValue().setBit(redisKey03,4,true);
        redisTemplate.opsForValue().setBit(redisKey03,5,true);

        String redisKey = "test:bm:04";

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Long bitOp = connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), redisKey01.getBytes(),
                        redisKey02.getBytes(), redisKey03.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }
}
