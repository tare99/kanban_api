package com.nsoft.integrations.vibra.kanban_api.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsoft.integrations.vibra.kanban_api.api.config.SecurityConfig;
import com.nsoft.integrations.vibra.kanban_api.api.event.TaskEvent;
import com.nsoft.integrations.vibra.kanban_api.api.model.TaskModel;
import com.nsoft.integrations.vibra.kanban_api.api.model.TaskModel.PagedTaskModelAssembler;
import com.nsoft.integrations.vibra.kanban_api.api.model.TaskModel.TaskModelAssembler;
import com.nsoft.integrations.vibra.kanban_api.api.request.CreateTaskRequest;
import com.nsoft.integrations.vibra.kanban_api.api.request.UpdateTaskRequest;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import com.nsoft.integrations.vibra.kanban_api.domain.service.TaskService;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

  private final String jwt =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.1M5tjrKacNn1r2RKPv0ZezTXjETxqeKdoaidhdcpwKw";
  @Autowired private MockMvc mvc;
  @MockitoBean private TaskService taskService;
  @MockitoBean private TaskModelAssembler taskModelAssembler;
  @MockitoBean private PagedTaskModelAssembler pagedTaskModelAssembler;
  @MockitoBean private PagedResourcesAssembler<Task> pagedResourcesAssembler;
  @MockitoBean private SimpMessagingTemplate simpMessagingTemplate;
  @Autowired private ObjectMapper objectMapper;
  private Task sampleTask;
  private TaskModel sampleModel;

  @BeforeEach
  void setUp() {
    sampleTask = new Task();
    sampleTask.setId(1L);
    sampleTask.setTitle("Test Title");
    sampleTask.setDescription("Test Description");
    sampleTask.setStatus(TaskStatus.TO_DO);
    sampleTask.setPriority(TaskPriority.HIGH);
    sampleTask.setVersion(0L);

    sampleModel = new TaskModel();
    sampleModel.setId(1L);
    sampleModel.setTitle("Test Title");
    sampleModel.setDescription("Test Description");
    sampleModel.setStatus(TaskStatus.TO_DO);
    sampleModel.setPriority(TaskPriority.HIGH);
    sampleModel.add(linkTo(methodOn(TaskController.class).getTask(1L)).withSelfRel());
  }

  @Test
  void whenGetTasks_thenOk() throws Exception {
    Page<Task> page =
        new PageImpl<>(Collections.singletonList(sampleTask), PageRequest.of(0, 20), 1);
    PagedModel.PageMetadata metadata =
        new PagedModel.PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements());
    PagedModel<TaskModel> pagedModel =
        PagedModel.of(Collections.singletonList(sampleModel), metadata);

    when(taskService.findAll(eq(null), any())).thenReturn(page);
    when(pagedResourcesAssembler.toModel(eq(page), any(PagedTaskModelAssembler.class)))
        .thenReturn(pagedModel);

    mvc.perform(
            get("/api/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.tasks[0].id").value(1));
  }

  @Test
  void whenGetTaskById_thenOk() throws Exception {
    when(taskService.findById(1L)).thenReturn(sampleTask);
    when(taskModelAssembler.toModel(sampleTask)).thenReturn(sampleModel);

    mvc.perform(
            get("/api/tasks/1")
                .header("Authorization", "Bearer " + jwt)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void whenCreateTask_thenCreated() throws Exception {
    CreateTaskRequest req =
        new CreateTaskRequest(
            "Test Title", "Test Description", TaskStatus.TO_DO, TaskPriority.HIGH);

    when(taskService.create(any(Task.class))).thenReturn(sampleTask);
    when(taskModelAssembler.toModel(sampleTask)).thenReturn(sampleModel);
    doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), any(TaskEvent.class));

    mvc.perform(
            post("/api/tasks")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(
            header().string("Location", sampleModel.getRequiredLink("self").toUri().toString()));

    verify(taskService).create(any(Task.class));
    verify(simpMessagingTemplate).convertAndSend(eq("/topic/tasks"), any(TaskEvent.class));
  }

  @Test
  void whenUpdateTask_thenOk() throws Exception {
    UpdateTaskRequest req =
        new UpdateTaskRequest(
            "Updated Title", "Updated Description", TaskStatus.DONE, TaskPriority.LOW, 0L);

    sampleTask.setTitle("Updated Title");
    sampleTask.setStatus(TaskStatus.DONE);
    sampleTask.setPriority(TaskPriority.LOW);

    when(taskService.update(eq(1L), any(Task.class))).thenReturn(sampleTask);
    when(taskModelAssembler.toModel(sampleTask)).thenReturn(sampleModel);
    doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), any(TaskEvent.class));

    mvc.perform(
            put("/api/tasks/1")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Test Title"));

    verify(taskService).update(eq(1L), any(Task.class));
    verify(simpMessagingTemplate).convertAndSend(eq("/topic/tasks"), any(TaskEvent.class));
  }

  @Test
  void whenDeleteTask_thenNoContent() throws Exception {
    doNothing().when(taskService).delete(1L);
    doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), any(TaskEvent.class));

    mvc.perform(delete("/api/tasks/1").header("Authorization", "Bearer " + jwt))
        .andExpect(status().isNoContent());

    verify(taskService).delete(1L);
    verify(simpMessagingTemplate).convertAndSend(eq("/topic/tasks"), any(TaskEvent.class));
  }
}
