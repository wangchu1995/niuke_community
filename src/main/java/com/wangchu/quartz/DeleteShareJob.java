package com.wangchu.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class DeleteShareJob implements Job {
    @Value("${wk.image.storage}")
    private String wkPath;
    private final Logger logger = LoggerFactory.getLogger(DeleteShareJob.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("开始执行删除临时共享长图文件");
        File dir=new File(wkPath);
        if(dir.exists()){
            removeDir(dir);
        }

    }

    private void removeDir(File dir) {
        File[] files=dir.listFiles();
        for(File file:files){
            if(file.isDirectory()){
                removeDir(file);
            }else{
                logger.info(file+":"+file.delete());
            }
        }
        System.out.println(dir+":"+dir.delete());
    }

}
