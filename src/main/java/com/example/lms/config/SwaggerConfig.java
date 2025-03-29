package com.example.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.Arrays;
import java.util.Collections;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 * This class sets up the Swagger documentation for the REST API.
 */
@Configuration
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

    /**
     * Configures the OpenAPI documentation for the application.
     *
     * @return The configured OpenAPI object
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LMS Progress API")
                        .description("API endpoints for tracking and managing student progress in the Learning Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LMS Support Team")
                                .email("support@lms-example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Development server"),
                        new Server().url("https://api.lms-example.com").description("Production server")))
                .components(new Components()
                        .securitySchemes(Collections.singletonMap("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from the authentication endpoint"))))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}