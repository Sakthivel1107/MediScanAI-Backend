package com.mediscan.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Swagger / OpenAPI documentation, including the JWT Bearer security scheme
 * so "Authorize" in Swagger UI correctly attaches the token to protected endpoints.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI mediScanOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediScan AI Backend API")
                        .description("REST API for MediScan AI - Intelligent Medicine Strip Reader and Assistant. "
                                + "Handles authentication, medicine strip scanning (OCR + AI), scan history, "
                                + "and a medicine catalog for the MediScan Flutter mobile app.")
                        .version("1.0.0")
                        .contact(new Contact().name("MediScan AI Team")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
