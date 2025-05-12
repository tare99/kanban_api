package com.nsoft.integrations.vibra.kanban_api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class KanbanApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(KanbanApiApplication.class, args);
  }
}
