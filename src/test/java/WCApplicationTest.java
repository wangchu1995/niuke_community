import com.wangchu.WCApplication;
import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.User;
import com.wangchu.dao.mapper.DiscussPostMapper;
import com.wangchu.dao.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WCApplication.class)
public class WCApplicationTest {
    //springrunner.class由spring-boot-starter-test提供

    @Autowired
    UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    private static final Logger logger = LoggerFactory.getLogger(WCApplicationTest.class);


    @Test
    public void testUserMapperSelect(){
        User user = userMapper.selectUserById(1);
        System.out.println(user);
    }

    @Test
    public void testUserMapperInsert(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("213");
        user.setCreateTime(new Date());
        user.setHeaderUrl("http://images.nowcoder.com/head/112t.png");
        user.setActivationCode("123");
        user.setStatus(1);
        user.setType(1);
        int i = userMapper.insertOneUser(user);
        System.out.println(i);
    }

    @Test
    public void testUserMapperUpdate(){
        int i = userMapper.updateStatusById(0, 1);
        System.out.println(i);
    }

    @Test
    public void testDiscussMapper(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPostByUserId(101,1,10,0);
        for(DiscussPost d:discussPosts){
            System.out.println(d);
        }
        System.out.println(discussPostMapper.selectDiscussPostNumByUserId(0));
    }

    @Test
    public void testLogger(){
        logger.info("info log");
        logger.debug("debug log");
    }
}
