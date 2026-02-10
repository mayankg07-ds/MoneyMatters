package com.moneymatters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MoneyMatters: Financial Calculator + Portfolio Tracker
 * Spring Boot Application
 */
@SpringBootApplication
@EnableScheduling
public class MoneyMattersApplication {

    private static final Logger logger = LoggerFactory.getLogger(MoneyMattersApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MoneyMattersApplication.class, args);
        logger.info("MoneyMatters application started successfully!");
        logger.info("API available at: http://localhost:8080/api");
        logger.info("Swagger UI at: http://localhost:8080/api/swagger-ui.html");
    }
}