package com.sleekydz86.productrecommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductRecommendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductRecommendApplication.class, args);
	}
}
