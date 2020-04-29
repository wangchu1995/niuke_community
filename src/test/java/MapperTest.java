import com.wangchu.NiukeApplication;
import com.wangchu.dal.entity.Comment;
import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.LoginTicket;
import com.wangchu.dao.mapper.CommentMapper;
import com.wangchu.dao.mapper.DiscussPostMapper;
import com.wangchu.dao.mapper.LoginTicketMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = NiukeApplication.class)
public class MapperTest {
    @Autowired
    LoginTicketMapper loginTicketMapper;
    @Autowired
    DiscussPostMapper discussPostMapper;
    @Autowired
    CommentMapper commentMapper;

    @Test
    public void insertTicketTest(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(2589520);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1200*10));
        int i = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(i);
    }

    @Test
    public void updateAndSelectTicketTest(){
        LoginTicket loginTicket = loginTicketMapper.selectLoginTicketByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateLoginTicketStatus("abc",1);
        loginTicket = loginTicketMapper.selectLoginTicketByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void insertDiscussPostTest(){
        DiscussPost post = new DiscussPost();
        post.setUserId(250);
        post.setContent("testContent");
        post.setTitle("test");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);
    }

    @Test
    public void selectCommentTest(){
        List<Comment> comments = commentMapper.selectComments(1, 228, 0, 5);
        for(Comment c:comments){
            System.out.println(c);
        }
    }

    @Test
    public void selectCountCommentTest(){
        int count = commentMapper.selectCommentCount(1, 228);
        System.out.println(count);
    }
}
