package com.heladeria.tpv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HeladeriaTpvApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeladeriaTpvApplication.class, args);
    }
}
