package com.sleekydz86.catalogflow;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class CatalogQueryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogQueryServiceApplication.class, args);
	}
}
