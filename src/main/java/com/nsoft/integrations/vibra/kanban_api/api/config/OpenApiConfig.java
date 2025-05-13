package com.nsoft.integrations.vibra.kanban_api.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    Components components =
        new Components()
            .addSecuritySchemes(
                "bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter your JWT token as: **Bearer <token>**"));

    SecurityRequirement requirement = new SecurityRequirement().addList("bearerAuth");

    return new OpenAPI()
        .components(components)
        .addSecurityItem(requirement)
        .info(new Info().title("My API").version("v1"));
  }
}
