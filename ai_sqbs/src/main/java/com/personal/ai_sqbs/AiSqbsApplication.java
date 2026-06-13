package com.personal.ai_sqbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiSqbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSqbsApplication.class, args);
    }

}
