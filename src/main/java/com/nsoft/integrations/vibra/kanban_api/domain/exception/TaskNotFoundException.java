package com.nsoft.integrations.vibra.kanban_api.domain.exception;

public class TaskNotFoundException extends RuntimeException {
  public TaskNotFoundException(Long id) {
    super("Task with id " + id + " not found");
  }
}
