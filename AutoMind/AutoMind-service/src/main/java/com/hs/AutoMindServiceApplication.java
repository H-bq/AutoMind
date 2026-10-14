package com.hs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.hs.mapper")
@EnableScheduling
@EnableAspectJAutoProxy
public class AutoMindServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoMindServiceApplication.class, args);
    }

}
