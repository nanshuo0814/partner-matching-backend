package com.nanshuo.partnermatching;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主要应用程序（项目启动入口）
 *
 * @author nanshuo
 * @date 2023/12/23 00:00:00
 */
@Slf4j
@EnableScheduling
@EnableAspectJAutoProxy
@MapperScan("com.nanshuo.partnermatching.mapper")
@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
        log.info("Project started successfully！");
    }

}
