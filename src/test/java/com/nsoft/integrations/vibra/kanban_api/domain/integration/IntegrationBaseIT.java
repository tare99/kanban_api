package com.nsoft.integrations.vibra.kanban_api.domain.integration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;

@ExtendWith(SpringExtension.class)
public class IntegrationBaseIT extends Repositories {

  static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0-oracle");

  static {
    mysql.withReuse(true).start();
  }

  @DynamicPropertySource
  static void propertySetup(DynamicPropertyRegistry registry) {
    registry.add("db-url", mysql::getJdbcUrl);
    registry.add("app-user", mysql::getUsername);
    registry.add("app-password", mysql::getPassword);
    registry.add("flyway-user", mysql::getUsername);
    registry.add("flyway-password", mysql::getPassword);
  }
}
