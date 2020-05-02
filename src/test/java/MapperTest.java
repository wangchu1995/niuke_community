import com.wangchu.NiukeApplication;
import com.wangchu.dal.entity.Comment;
import com.wangchu.dal.entity.DiscussPost;
import com.wangchu.dal.entity.LoginTicket;
import com.wangchu.dal.entity.Message;
import com.wangchu.dao.mapper.CommentMapper;
import com.wangchu.dao.mapper.DiscussPostMapper;
import com.wangchu.dao.mapper.LoginTicketMapper;
import com.wangchu.dao.mapper.MessageMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.LinkedList;
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
    @Autowired
    MessageMapper messageMapper;

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

    @Test
    public void selectMessageTets(){
        //1.查询页面显示会话消息
        //111->131和131->111 都是111_131
        List<Message> list = messageMapper.selectConversationsByUser(111, 0, 20);
        for(Message m:list){
            System.out.println(m);
        }
        //2.查询会话详情的消息
        //111_131指的是111和131之间的全部对话，而不仅仅是111对131的
        list = messageMapper.selectLettersByConversationId("111_131",0,20);
        for(Message m:list){
            System.out.println(m);
        }
        //3.查询用户的会话数量
        int i = messageMapper.selectConversationCount(111);
        System.out.println(i);
        //4.查询会话中的消息数量
        i=messageMapper.selectConversationUnreadCount("111_131");
        System.out.println(i);
        //5.查询用户的未读消息数量
        i=messageMapper.selectLetterUnreadCount(111,null);
        System.out.println(i);
        //6.查询用户在一个会话中的未读消息数量
        //指明111给131的信息数量
        i=messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(i);
    }

    @Test
    public void insertMessageTest(){
        Message message = new Message();
        message.setFromId(111);
        message.setToId(131);
        message.setConversationId("111_131");
        message.setContent("傻狗左文涛");
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageMapper.insertMessage(message);
    }

    @Test
    public void updateMessageStatus(){
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        messageMapper.updateLetterRead(list,1);
    }
}
