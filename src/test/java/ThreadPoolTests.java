import com.wangchu.WCApplication;
import com.wangchu.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WCApplication.class)
public class ThreadPoolTests {
    private final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    private ExecutorService executorService01 = Executors.newFixedThreadPool(5);
    private ScheduledExecutorService executorService02 = Executors.newScheduledThreadPool(5);

    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJDKThreadPool(){
        Runnable task01 = new Runnable() {
            @Override
            public void run() {
                logger.info("jdk ThreadPool - fixedThreadPool");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService01.submit(task01);
        }

        sleep(10000);
    }

    @Test
    public void testJDKThreadPool02(){
        Runnable task01 = new Runnable() {
            @Override
            public void run() {
                logger.info("jdk ThreadPool - fixedThreadPool");
            }
        };

        executorService02.scheduleAtFixedRate(task01,5000,1000,TimeUnit.MILLISECONDS);

        sleep(10000);
    }

    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Test
    public void testSpringThreadPool(){
        Runnable task01 = new Runnable() {
            @Override
            public void run() {
                logger.info("spring ThreadPool - fixedThreadPool");
            }
        };

        for (int i = 0; i < 10; i++) {
            executor.submit(task01);
        }

        sleep(10000);
    }

    @Test
    public void testSpringThreadPool02(){
        Runnable task01 = new Runnable() {
            @Override
            public void run() {
                logger.info("spring ThreadPool - fixedThreadPool");
            }
        };
        Date startTiem = new Date(System.currentTimeMillis()+10000);
        taskScheduler.scheduleAtFixedRate(task01,startTiem,1000);

        sleep(30000);
    }

    @Autowired
    AlphaService alphaService;

    @Test
    public void testSpringThreadPool03(){


        for (int i = 0; i < 10; i++) {
            alphaService.execute();
        }

        sleep(10000);
    }

    @Test
    public void testSpringThreadPool04(){
        sleep(20000);
    }
}
