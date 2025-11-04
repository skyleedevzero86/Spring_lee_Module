package com.sleekydz86.ocrstudy1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.sleekydz86.ocrstudy1")
public class Ocrstudy1Application {

    public static void main(String[] args) {
        SpringApplication.run(Ocrstudy1Application.class, args);
    }

}
