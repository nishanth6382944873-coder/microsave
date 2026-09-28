package com.microsave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MicroSave – Self-Help Group Savings Tracker
 * Main Spring Boot Application Entry Point.
 */
@SpringBootApplication
public class MicroSaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroSaveApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" MicroSave Backend is running on port 8080!");
        System.out.println(" Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println(" OpenAPI Docs: http://localhost:8080/v3/api-docs");
        System.out.println("==================================================================");
    }
}
