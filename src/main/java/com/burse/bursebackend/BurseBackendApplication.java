package com.burse.bursebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.burse.bursebackend.entities")
public class BurseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BurseBackendApplication.class, args);
    }

}

