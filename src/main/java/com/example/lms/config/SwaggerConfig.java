package com.example.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger/OpenAPI configuration for the LMS application.
 * 
 * This class configures the OpenAPI documentation for the REST API endpoints,
 * including security schemes for JWT authentication.
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
            .info(new Info()
                .title("LMS REST API")
                .description("Learning Management System API documentation")
                .version("1.0")
                .contact(new Contact()
                    .name("MST Team")
                    .email("sajashawawra@gmail.com")
                    .url("https://lms.example.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
    }
    
    /**
     * Create a security scheme for JWT authentication
     * 
     * @return SecurityScheme for JWT Bearer token
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer");
    }
}