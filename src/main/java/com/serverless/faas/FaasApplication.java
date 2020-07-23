package com.serverless.faas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;

@SpringBootApplication
public class FaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaasApplication.class, args);
        System.out.println(Instant.now().getEpochSecond()+ 15*60);
    }

}
