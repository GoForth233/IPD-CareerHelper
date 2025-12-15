package com.group1.career;

import com.group1.career.service.CareerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CareerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(CareerService careerService) {
        return args -> {
            careerService.initializeDefaultPaths();
        };
    }
}
