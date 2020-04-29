import com.wangchu.NiukeApplication;
import com.wangchu.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = NiukeApplication.class)
public class SensitiveTest {
    @Autowired
    SensitiveFilter sensitiveFilter;

    private static final Logger logger = LoggerFactory.getLogger(SensitiveTest.class);

    @Test
    public void testSensitive(){
        String s1 = "赌大博，抽烟，开票，都来吧！";
        s1 = sensitiveFilter.filter(s1);
        System.out.println(s1);
        String s2 = "☆赌☆博啊☆，拉票☆啊，都有啊☆";
        System.out.println(sensitiveFilter.filter(s2));
        logger.debug("测试一下logger");
    }


    @Test
    public void testRandom(){
        Random random = new Random();
        for(int i=0;i<6;i++){
            double r = random.nextDouble()*10;
            System.out.println(r);
        }
    }
}
