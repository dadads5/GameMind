package com.zuel.springtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SpringtestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringtestApplication.class, args);
    }

}
