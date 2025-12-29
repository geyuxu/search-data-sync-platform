package com.example.searchmiddleware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories(basePackages = "com.example.searchmiddleware.repository.es")
@org.springframework.data.mongodb.repository.config.EnableMongoRepositories(basePackages = "com.example.searchmiddleware.repository.mongo")
public class SearchMiddlewareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchMiddlewareApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer corsConfigurer() {
        return new org.springframework.web.servlet.config.annotation.WebMvcConfigurer() {
            @Override
            public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
            }
        };
    }
}
