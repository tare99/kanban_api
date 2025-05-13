package com.nsoft.integrations.vibra.kanban_api.domain.config;

import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;

@Configuration
@RequiredArgsConstructor
public class DatabaseWarmup implements ApplicationRunner {

  private final TaskService taskService;

  @Override
  public void run(ApplicationArguments args) {
    taskService.findAll(TaskStatus.TO_DO, PageRequest.of(0, 1));
  }
}
