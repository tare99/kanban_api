package com.nsoft.integrations.vibra.kanban_api.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.exception.MissingVersionException;
import com.nsoft.integrations.vibra.kanban_api.domain.exception.TaskNotFoundException;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import com.nsoft.integrations.vibra.kanban_api.domain.repository.TaskRepository;
import com.nsoft.integrations.vibra.kanban_api.domain.repository.TaskRepository.TaskSpecifications;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

@Service
@Slf4j
@Transactional
public class TaskService {
  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public Page<Task> findAll(TaskStatus status, Pageable pageable) {
    Specification<Task> spec = Specification.where(TaskSpecifications.hasStatus(status));

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Page<Task> all = taskRepository.findAll(spec, pageable);
    stopWatch.stop();
    log.info("Time: {}", stopWatch.getTotalTimeMillis());
    return all;
  }

  public Task findById(Long id) {
    return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
  }

  public Task create(Task t) {
    t.setId(null);
    return taskRepository.save(t);
  }

  public Task update(Long id, Task task) {
    findById(id);
    task.setId(id);
    return taskRepository.save(task);
  }

  public Task patch(Long id, String patch) throws JsonPatchException, IOException {
    Task existingTask = findById(id);
    Task patchedTask = mergePatch(existingTask, patch, Task.class);
    patchedTask.setId(id);
    return taskRepository.save(patchedTask);
  }

  public void delete(Long id) {
    taskRepository.deleteById(id);
  }

  private Task mergePatch(Task t, String patch, Class<Task> clazz)
      throws IOException, JsonPatchException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.convertValue(t, JsonNode.class);
    JsonNode patchNode = mapper.readTree(patch);
    if (!patchNode.has("version")) {
      throw new MissingVersionException(
          "PATCH request must include 'version' field for optimistic locking.");
    }
    com.github.fge.jsonpatch.mergepatch.JsonMergePatch mergePatch =
        com.github.fge.jsonpatch.mergepatch.JsonMergePatch.fromJson(patchNode);
    node = mergePatch.apply(node);
    return mapper.treeToValue(node, clazz);
  }
}
