package com.microsave.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3.0 Configuration.
 * Access interactive documentation at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI microSaveOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MicroSave – Self-Help Group Savings Tracker API")
                        .description("Backend REST APIs for Self-Help Groups (SHG) to track weekly savings, internal member loans, and repayments.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MicroSave Project Team")
                                .email("support@microsave.local"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
