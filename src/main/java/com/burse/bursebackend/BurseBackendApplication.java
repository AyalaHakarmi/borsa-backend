package com.burse.bursebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BurseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BurseBackendApplication.class, args);
    }

}

