package com.AI_SQBS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiSmartQueueBookingSystem {

    public static void main(String[] args) {
        SpringApplication.run(AiSmartQueueBookingSystem.class, args);
    }

}