package com.sleekydz86.monitoring.logstack_s3;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.mapper")
public class LogstackS3Application {

    public static void main(String[] args) {
        SpringApplication.run(LogstackS3Application.class, args);
    }
}
