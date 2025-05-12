package com.nsoft.integrations.vibra.kanban_api.api.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import com.nsoft.integrations.vibra.kanban_api.domain.service.TaskService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TaskGraphQLController {

  private final TaskService taskService;

  @QueryMapping
  public TaskDto task(@Argument Long id) {
    return TaskDto.from(taskService.findById(id));
  }

  @QueryMapping
  public PagedTasks tasks(
      @Argument TaskStatus status,
      @Argument int page,
      @Argument int size,
      @Argument List<String> sort) {
    Sort sortSpec = Sort.by(sort.stream().map(Sort.Order::by).toList());
    Pageable pageable = PageRequest.of(page, size, sortSpec);
    Page<Task> result = taskService.findAll(status, pageable);
    return new PagedTasks(result.map(TaskDto::from).getContent(), new PageInfo(result));
  }

  @MutationMapping
  public TaskDto createTask(@Argument CreateTaskInput input) {
    Task task = new Task();
    task.setTitle(input.getTitle());
    task.setDescription(input.getDescription());
    task.setStatus(Optional.ofNullable(input.getStatus()).orElse(TaskStatus.TO_DO));
    task.setPriority(input.getPriority());
    return TaskDto.from(taskService.create(task));
  }

  @MutationMapping
  public TaskDto updateTask(@Argument Long id, @Argument UpdateTaskInput input) {
    Task task = new Task();
    task.setId(id);
    task.setTitle(input.getTitle());
    task.setDescription(input.getDescription());
    task.setStatus(input.getStatus());
    task.setPriority(input.getPriority());
    task.setVersion(input.getVersion());
    return TaskDto.from(taskService.update(id, task));
  }

  @MutationMapping
  public TaskDto patchTask(@Argument PatchTaskInput input) throws IOException, JsonPatchException {
    return TaskDto.from(taskService.patch(input.getId(), input.getPatchJson()));
  }

  @MutationMapping
  public boolean deleteTask(@Argument Long id) {
    taskService.delete(id);
    return true;
  }

  @Data
  public static class TaskDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long version;

    public static TaskDto from(Task task) {
      TaskDto dto = new TaskDto();
      dto.setId(task.getId());
      dto.setTitle(task.getTitle());
      dto.setDescription(task.getDescription());
      dto.setStatus(task.getStatus());
      dto.setPriority(task.getPriority());
      dto.setVersion(task.getVersion());
      return dto;
    }
  }

  @Data
  public static class CreateTaskInput {
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
  }

  @Data
  public static class UpdateTaskInput {
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long version;
  }

  @Data
  public static class PatchTaskInput {
    private Long id;
    private String patchJson;
  }

  @Data
  @AllArgsConstructor
  public static class PagedTasks {
    private List<TaskDto> content;
    private PageInfo pageInfo;
  }

  @Data
  @AllArgsConstructor
  public static class PageInfo {
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;

    public PageInfo(Page<?> page) {
      this.page = page.getNumber();
      this.size = page.getSize();
      this.totalElements = (int) page.getTotalElements();
      this.totalPages = page.getTotalPages();
    }
  }
}
