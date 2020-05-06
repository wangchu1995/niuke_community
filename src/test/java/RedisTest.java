import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
@SpringBootTest
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
}
