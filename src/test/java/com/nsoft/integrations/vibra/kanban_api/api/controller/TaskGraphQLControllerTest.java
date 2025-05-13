package com.nsoft.integrations.vibra.kanban_api.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.github.fge.jsonpatch.JsonPatchException;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import com.nsoft.integrations.vibra.kanban_api.domain.service.TaskService;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(TaskGraphQLController.class)
class TaskGraphQLControllerTest {

  @Autowired private GraphQlTester graphQlTester;

  @MockitoBean private TaskService taskService;

  private Task sampleTask;

  @BeforeEach
  void setUp() {
    sampleTask = new Task();
    sampleTask.setId(1L);
    sampleTask.setTitle("Test Task");
    sampleTask.setDescription("Description");
    sampleTask.setStatus(TaskStatus.TO_DO);
    sampleTask.setPriority(TaskPriority.LOW);
    sampleTask.setVersion(0L);
  }

  @Test
  void testTaskById() {
    when(taskService.findById(1L)).thenReturn(sampleTask);

    String document = "query { task(id: 1) { id title description status priority version } }";

    graphQlTester
        .document(document)
        .execute()
        .path("task.id")
        .entity(Long.class)
        .isEqualTo(1L)
        .path("task.title")
        .entity(String.class)
        .isEqualTo("Test Task")
        .path("task.status")
        .entity(String.class)
        .isEqualTo("TO_DO");
  }

  @Test
  void testTasksPaged() {
    Page<Task> page = new PageImpl<>(List.of(sampleTask), PageRequest.of(0, 10, Sort.by("id")), 1);
    when(taskService.findAll(eq(TaskStatus.TO_DO), any())).thenReturn(page);

    String document =
        "query { tasks(status: TO_DO, page: 0, size: 10, sort: [\"id\"]) { "
            + "content { id title } pageInfo { page size totalElements totalPages } } }";

    graphQlTester
        .document(document)
        .execute()
        .path("tasks.content[0].id")
        .entity(Long.class)
        .isEqualTo(1L)
        .path("tasks.pageInfo.page")
        .entity(Integer.class)
        .isEqualTo(0)
        .path("tasks.pageInfo.totalElements")
        .entity(Integer.class)
        .isEqualTo(1);
  }

  @Test
  void testCreateTask() {
    Task created = sampleTask;
    created.setId(2L);
    created.setTitle("New");
    when(taskService.create(any(Task.class))).thenReturn(created);

    String mutation =
        "mutation { createTask(input: { title: \"New\", description: \"Desc\", status: TO_DO, priority: HIGH }) { id title description } }";

    graphQlTester
        .document(mutation)
        .execute()
        .path("createTask.id")
        .entity(Long.class)
        .isEqualTo(2L)
        .path("createTask.title")
        .entity(String.class)
        .isEqualTo("New");
  }

  @Test
  void testUpdateTask() {
    Task updated = sampleTask;
    updated.setTitle("Updated Title");
    when(taskService.update(eq(1L), any(Task.class))).thenReturn(updated);

    String mutation =
        "mutation { updateTask(id: 1, input: { title: \"Updated Title\", description: \"Desc\", status: IN_PROGRESS, priority: LOW, version: 0 }) { id title status } }";

    graphQlTester
        .document(mutation)
        .execute()
        .path("updateTask.title")
        .entity(String.class)
        .isEqualTo("Updated Title")
        .path("updateTask.status")
        .entity(String.class)
        .isEqualTo("TO_DO");
  }

  @Test
  void testPatchTask() throws IOException, JsonPatchException {
    Task patched = sampleTask;
    patched.setTitle("Patched Title");
    when(taskService.patch(eq(1L), anyString())).thenReturn(patched);

    String mutation =
        "mutation { patchTask(input: { id: 1, patchJson: \"{\\\"title\\\":\\\"Patched Title\\\"}\" }) { title } }";

    graphQlTester
        .document(mutation)
        .execute()
        .path("patchTask.title")
        .entity(String.class)
        .isEqualTo("Patched Title");
  }

  @Test
  void testDeleteTask() {
    doNothing().when(taskService).delete(1L);

    String mutation = "mutation { deleteTask(id: 1) }";

    graphQlTester
        .document(mutation)
        .execute()
        .path("deleteTask")
        .entity(Boolean.class)
        .isEqualTo(true);
  }
}
