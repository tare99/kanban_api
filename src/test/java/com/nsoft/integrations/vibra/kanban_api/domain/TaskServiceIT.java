package com.nsoft.integrations.vibra.kanban_api.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.exception.MissingVersionException;
import com.nsoft.integrations.vibra.kanban_api.domain.exception.TaskNotFoundException;
import com.nsoft.integrations.vibra.kanban_api.domain.integration.IntegrationBaseIT;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import com.nsoft.integrations.vibra.kanban_api.domain.service.TaskService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class TaskServiceIT extends IntegrationBaseIT {
  @Autowired private TaskService taskService;

  private Task createAndSaveSampleTask() {
    Task t = new Task();
    t.setTitle("Sample");
    t.setDescription("desc");
    t.setStatus(TaskStatus.TO_DO);
    t.setPriority(TaskPriority.LOW);
    return taskRepository.save(t);
  }

  @Test
  void findAll_shouldReturnPage() {
    createAndSaveSampleTask();
    Page<Task> result = taskService.findAll(TaskStatus.TO_DO, PageRequest.of(0, 10));
    assertThat(result.getTotalElements()).isPositive();
  }

  @Test
  void findById_shouldReturnTask() {
    Task saved = createAndSaveSampleTask();
    Task found = taskService.findById(saved.getId());
    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(saved.getId());
  }

  @Test
  void findById_invalid_shouldThrow() {
    assertThrows(TaskNotFoundException.class, () -> taskService.findById(999L));
  }

  @Test
  void create_shouldPersistAndReturn() {
    Task t = new Task();
    t.setTitle("New");
    t.setStatus(TaskStatus.TO_DO);
    t.setPriority(TaskPriority.HIGH);
    Task created = taskService.create(t);
    assertThat(created.getId()).isNotNull();
  }

  @Test
  void update_shouldUpdateExisting() {
    Task t = createAndSaveSampleTask();
    t.setTitle("Updated");
    Task updated = taskService.update(t.getId(), t);
    assertThat(updated.getTitle()).isEqualTo("Updated");
  }

  @Test
  void patch_shouldUpdateField_whenVersionPresent() throws Exception {
    Task original = createAndSaveSampleTask();
    String patchJson =
        String.format("{\"title\":\"Patched\",\"version\":%d}", original.getVersion());
    Task patched = taskService.patch(original.getId(), patchJson);
    assertThat(patched.getTitle()).isEqualTo("Patched");
  }

  @Test
  void patch_shouldThrowMissingVersionException() {
    Task original = createAndSaveSampleTask();
    String patchJson = "{\"title\":\"Invalid\"}";
    assertThrows(
        MissingVersionException.class, () -> taskService.patch(original.getId(), patchJson));
  }

  @Test
  void delete_shouldRemoveEntity() {
    Task t = createAndSaveSampleTask();
    taskService.delete(t.getId());
    Optional<Task> deleted = taskRepository.findById(t.getId());
    assertThat(deleted).isEmpty();
  }
}
