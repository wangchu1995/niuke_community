import com.wangchu.WCApplication;
import com.wangchu.util.CommonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WCApplication.class)
public class UtilTest {

    @Test
    public void testJSONString(){
        Map<String,Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age",15);
        String jsonString = CommonUtils.getJSONString(0, "成功", map);
        System.out.println(jsonString);
    }
}
