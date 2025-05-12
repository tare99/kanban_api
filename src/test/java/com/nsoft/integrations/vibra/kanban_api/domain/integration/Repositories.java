package com.nsoft.integrations.vibra.kanban_api.domain.integration;

import com.nsoft.integrations.vibra.kanban_api.domain.repository.TaskRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Slf4j
abstract class Repositories {

  @Autowired protected TaskRepository taskRepository;
  @Autowired protected JdbcTemplate jdbcTemplate;

  @AfterEach
  void clearTables() {
    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");

    List<String> tableNames =
        jdbcTemplate.queryForList(
            """
					SELECT table_name
					FROM information_schema.tables
					WHERE table_schema = DATABASE()
					AND table_name NOT IN ('flyway_schema_history');
					""",
            String.class);

    for (String tableName : tableNames) {
      jdbcTemplate.execute("TRUNCATE TABLE " + tableName + ";");
    }
    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
    log.info("Test database cleaned up.");
  }
}
