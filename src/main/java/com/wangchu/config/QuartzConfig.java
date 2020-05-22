package com.wangchu.config;

import com.wangchu.quartz.AlphaJob;
import com.wangchu.quartz.DeleteShareJob;
import com.wangchu.quartz.PostScoreJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {
    //@Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean fb = new JobDetailFactoryBean();
        fb.setJobClass(AlphaJob.class);
        fb.setName("alphaJob");
        fb.setGroup("alphaGroup");
        fb.setDurability(true);   //任务是否是长期保存的
        fb.setRequestsRecovery(true);  //任务是否是可恢复的
        return fb;
    }

    //@Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean stfb = new SimpleTriggerFactoryBean();
        stfb.setJobDetail(alphaJobDetail);
        stfb.setName("alphaTrigger");
        stfb.setGroup("alphaTriggerGroup");
        stfb.setRepeatInterval(3000);
        stfb.setJobDataAsMap(new JobDataMap());
        return stfb;
    }

    @Bean
    public JobDetailFactoryBean postScoreJobDetail(){
        JobDetailFactoryBean fb = new JobDetailFactoryBean();
        fb.setJobClass(PostScoreJob.class);
        fb.setName("postScoreJob");
        fb.setGroup("postScoreJobGroup");
        fb.setDurability(true);   //任务是否是长期保存的
        fb.setRequestsRecovery(true);  //任务是否是可恢复的
        return fb;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreTrigger(JobDetail postScoreJobDetail){
        SimpleTriggerFactoryBean stfb = new SimpleTriggerFactoryBean();
        stfb.setJobDetail(postScoreJobDetail);
        stfb.setName("postScoreTrigger");
        stfb.setGroup("postScoreTriggerGroup");
        stfb.setRepeatInterval(1000*60*5);
        stfb.setJobDataAsMap(new JobDataMap());
        return stfb;
    }

    @Bean
    public JobDetailFactoryBean deleteShareJobDetail(){
        JobDetailFactoryBean fb = new JobDetailFactoryBean();
        fb.setJobClass(DeleteShareJob.class);
        fb.setName("deleteShareJob");
        fb.setGroup("deleteShareJobGroup");
        fb.setDurability(true);   //任务是否是长期保存的
        fb.setRequestsRecovery(true);  //任务是否是可恢复的
        return fb;
    }

    @Bean
    public SimpleTriggerFactoryBean deleteShareTrigger(JobDetail deleteShareJobDetail){
        SimpleTriggerFactoryBean stfb = new SimpleTriggerFactoryBean();
        stfb.setJobDetail(deleteShareJobDetail);
        stfb.setName("deleteShareTrigger");
        stfb.setGroup("deleteShareTriggerGroup");
        stfb.setRepeatInterval(1000*60*4);
        stfb.setJobDataAsMap(new JobDataMap());
        return stfb;
    }
}
