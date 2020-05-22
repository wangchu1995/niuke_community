import com.wangchu.WCApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WCApplication.class)
public class QuartzTest {
    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDelete(){
        boolean b = false;
        try {
            b = scheduler.deleteJob(new JobKey("postScoreJob", "postScoreJobGroup"));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        System.out.println(b);

    }
}
