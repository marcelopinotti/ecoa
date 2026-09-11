package com.marcelodev.ecoa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EcoaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoaApplication.class, args);
    }

}
