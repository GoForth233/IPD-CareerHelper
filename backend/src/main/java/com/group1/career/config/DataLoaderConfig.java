package com.group1.career.config;

import com.group1.career.service.CareerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Data Loader Configuration
 * 
 * This configuration initializes default career paths when the application starts.
 * It is disabled in test profile to avoid dependencies on external services.
 */
@Configuration
@Profile("!test") // Do not load in test profile
public class DataLoaderConfig {

    @Bean
    public CommandLineRunner dataLoader(CareerService careerService) {
        return args -> {
            careerService.initializeDefaultPaths();
        };
    }
}
