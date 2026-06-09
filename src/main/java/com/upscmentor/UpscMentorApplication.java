package com.upscmentor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UpscMentorApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpscMentorApplication.class, args);
    }
}