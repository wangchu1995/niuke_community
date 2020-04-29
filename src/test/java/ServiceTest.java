import com.wangchu.NiukeApplication;
import com.wangchu.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = NiukeApplication.class)
public class ServiceTest {
    @Autowired
    AlphaService alphaService;

    @Test
    public void save01(){
        String s = alphaService.save01();
        System.out.println(s);
    }

    @Test
    public void save02(){
        String s = alphaService.save02();
        System.out.println(s);
    }
}
