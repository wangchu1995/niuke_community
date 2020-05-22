import com.wangchu.WCApplication;
import com.wangchu.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WCApplication.class)
public class MailTest {
    @Autowired
    MailClient mailClient;
    @Autowired
    TemplateEngine templateEngine;

    @Test
    public void sendMailTest() throws MessagingException {
        Context context = new Context();
        context.setVariable("username","老王");
        String content = templateEngine.process("/mail/demo",context);
        mailClient.sendMail("18845092131@163.com","html",content);
    }
}
