package com.moneymatters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

/**
 * MoneyMatters: Financial Calculator + Portfolio Tracker
 * Spring Boot Application
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class MoneyMattersApplication {

    private static final Logger logger = LoggerFactory.getLogger(MoneyMattersApplication.class);

    public static void main(String[] args) {
        // Fix deprecated "Asia/Calcutta" timezone that Railway PostgreSQL rejects
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

        var ctx = SpringApplication.run(MoneyMattersApplication.class, args);
        String port = ctx.getEnvironment().getProperty("server.port", "8082");
        String contextPath = ctx.getEnvironment().getProperty("server.servlet.context-path", "/api");
        logger.info("MoneyMatters application started successfully!");
        logger.info("API available at: http://localhost:{}{}", port, contextPath);
        logger.info("Swagger UI at: http://localhost:{}{}/swagger-ui/index.html", port, contextPath);
    }
}