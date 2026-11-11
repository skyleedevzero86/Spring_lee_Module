package com.sleekydz86.storagemigration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class StorageMigrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageMigrationApplication.class, args);
    }
}
