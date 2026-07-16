package com.sleekydz86.catalogflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CatalogCommandServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogCommandServiceApplication.class, args);
	}
}
