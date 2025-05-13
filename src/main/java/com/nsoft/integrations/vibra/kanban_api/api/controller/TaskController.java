package com.nsoft.integrations.vibra.kanban_api.api.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.github.fge.jsonpatch.JsonPatchException;
import com.nsoft.integrations.vibra.kanban_api.api.doc.PagedDoc;
import com.nsoft.integrations.vibra.kanban_api.api.doc.TaskDoc;
import com.nsoft.integrations.vibra.kanban_api.api.event.TaskEvent;
import com.nsoft.integrations.vibra.kanban_api.api.model.TaskModel;
import com.nsoft.integrations.vibra.kanban_api.api.model.TaskModel.PagedTaskModelAssembler;
import com.nsoft.integrations.vibra.kanban_api.api.model.TaskModel.TaskModelAssembler;
import com.nsoft.integrations.vibra.kanban_api.api.request.CreateTaskRequest;
import com.nsoft.integrations.vibra.kanban_api.api.request.UpdateTaskRequest;
import com.nsoft.integrations.vibra.kanban_api.api.response.BadRequestResponse;
import com.nsoft.integrations.vibra.kanban_api.api.response.ErrorResponse;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import com.nsoft.integrations.vibra.kanban_api.domain.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "API for managing kanban tasks")
public class TaskController {

  private final TaskService taskService;
  private final TaskModelAssembler taskModelAssembler;
  private final PagedTaskModelAssembler pagedTaskModelAssembler;
  private final PagedResourcesAssembler<Task> pagedResourcesAssembler;
  private final SimpMessagingTemplate simpMessagingTemplate;

  @Operation(summary = "List tasks with optional status filter and pagination")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of tasks retrieved",
            content = @Content(schema = @Schema(implementation = PagedDoc.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping
  public ResponseEntity<PagedModel<TaskModel>> getTasks(
      @Parameter(description = "Filter by task status") @RequestParam(required = false)
          TaskStatus status,
      @ParameterObject
          @PageableDefault(size = 20)
          @SortDefault(sort = "id")
          @SortDefault(sort = "status", direction = Direction.DESC)
          Pageable pageable) {
    Page<Task> page = taskService.findAll(status, pageable);
    PagedModel<TaskModel> model = pagedResourcesAssembler.toModel(page, pagedTaskModelAssembler);
    model.add(
        linkTo(methodOn(TaskController.class).create(null)).withRel("create").withType("POST"));
    return ResponseEntity.ok(model);
  }

  @Operation(summary = "Get a single task by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task found",
            content = @Content(schema = @Schema(implementation = TaskDoc.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping("/{id}")
  public ResponseEntity<TaskModel> getTask(
      @Parameter(description = "ID of the task to retrieve", required = true) @PathVariable
          Long id) {
    return ResponseEntity.ok().body(taskModelAssembler.toModel(taskService.findById(id)));
  }

  @Operation(summary = "Create a new task")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Task created",
            content = @Content(schema = @Schema(implementation = TaskDoc.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = BadRequestResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @PostMapping
  public ResponseEntity<TaskModel> create(
      @Parameter(description = "Task create request payload", required = true) @RequestBody @Valid
          CreateTaskRequest createTaskRequest) {
    Task taskInput = toEntity(createTaskRequest);
    Task task = taskService.create(taskInput);
    TaskModel taskModel = taskModelAssembler.toModel(task);
    simpMessagingTemplate.convertAndSend("/topic/tasks", new TaskEvent("CREATED", task));
    return ResponseEntity.created(taskModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
        .body(taskModel);
  }

  @Operation(summary = "Update an existing task completely")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task updated",
            content = @Content(schema = @Schema(implementation = TaskDoc.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = BadRequestResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @PutMapping("/{id}")
  public ResponseEntity<TaskModel> update(
      @Parameter(description = "ID of the task to update", required = true) @PathVariable Long id,
      @Parameter(description = "Task update request payload", required = true) @Valid @RequestBody
          UpdateTaskRequest updateTaskRequest) {
    Task task = toEntity(id, updateTaskRequest);
    Task updatedTask = taskService.update(id, task);
    TaskModel taskModel = taskModelAssembler.toModel(updatedTask);
    simpMessagingTemplate.convertAndSend("/topic/tasks", new TaskEvent("UPDATED", updatedTask));
    return ResponseEntity.ok(taskModel);
  }

  @Operation(summary = "Partially update a task using JSON Merge Patch")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task patched",
            content = @Content(schema = @Schema(implementation = TaskDoc.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
  public ResponseEntity<TaskModel> patch(
      @Parameter(description = "ID of the task to patch", required = true) @PathVariable Long id,
      @Parameter(description = "JSON Merge Patch payload", required = true)
          @RequestBody
          @Schema(implementation = UpdateTaskRequest.class)
          String patchJson)
      throws JsonPatchException, IOException {
    Task patched = taskService.patch(id, patchJson);
    TaskModel model = taskModelAssembler.toModel(patched);
    simpMessagingTemplate.convertAndSend("/topic/tasks", new TaskEvent("PATCHED", patched));
    return ResponseEntity.ok(model);
  }

  @Operation(summary = "Delete a task by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID of the task to delete", required = true) @PathVariable Long id) {
    taskService.delete(id);
    simpMessagingTemplate.convertAndSend("/topic/tasks", new TaskEvent("DELETED", id));
    return ResponseEntity.noContent().build();
  }

  private Task toEntity(Long id, UpdateTaskRequest updateTaskRequest) {
    Task task = new Task();
    task.setId(id);
    BeanUtils.copyProperties(updateTaskRequest, task);
    return task;
  }

  private Task toEntity(CreateTaskRequest createTaskRequest) {
    Task task = new Task();
    BeanUtils.copyProperties(createTaskRequest, task);
    return task;
  }
}
