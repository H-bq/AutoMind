package com.hs.simulator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.hs")
@MapperScan("com.hs.simulator.mapper")
@EnableScheduling
public class AutoMindSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoMindSimulatorApplication.class, args);
    }
}
