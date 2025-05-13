package com.nsoft.integrations.vibra.kanban_api.api.model;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.github.fge.jsonpatch.JsonPatchException;
import com.nsoft.integrations.vibra.kanban_api.api.controller.TaskController;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import java.io.IOException;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Relation(itemRelation = "task", collectionRelation = "tasks")
public class TaskModel extends RepresentationModel<TaskModel> {
  private Long id;

  private String title;

  private String description;

  private TaskStatus status;

  private TaskPriority priority;

  private Long version;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    TaskModel taskModel = (TaskModel) o;
    return Objects.equals(id, taskModel.id)
        && Objects.equals(title, taskModel.title)
        && Objects.equals(description, taskModel.description)
        && status == taskModel.status
        && priority == taskModel.priority
        && Objects.equals(version, taskModel.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id, title, description, status, priority, version);
  }

  @Component
  public static class TaskModelAssembler implements RepresentationModelAssembler<Task, TaskModel> {

    @Override
    public @NonNull TaskModel toModel(@NonNull Task task) {
      TaskModel model = new TaskModel();
      BeanUtils.copyProperties(task, model);
      model.add(
          linkTo(methodOn(TaskController.class).getTask(task.getId()))
              .withSelfRel()
              .withType(HttpMethod.GET.name()));
      model.add(
          linkTo(methodOn(TaskController.class).create(null))
              .withRel("create")
              .withType(HttpMethod.POST.name()));
      model.add(
          linkTo(methodOn(TaskController.class).update(task.getId(), null))
              .withRel("update")
              .withType(HttpMethod.PUT.name()));
      try {
        model.add(
            linkTo(methodOn(TaskController.class).patch(task.getId(), null))
                .withRel("patch")
                .withType(HttpMethod.PATCH.name()));
      } catch (JsonPatchException | IOException e) {
        throw new RuntimeException(e);
      }
      model.add(
          linkTo(methodOn(TaskController.class).delete(task.getId()))
              .withRel("delete")
              .withType(HttpMethod.DELETE.name()));
      return model;
    }
  }

  @Component
  public static class PagedTaskModelAssembler
      implements RepresentationModelAssembler<Task, TaskModel> {

    @Override
    public @NonNull TaskModel toModel(@NonNull Task task) {
      TaskModel model = new TaskModel();
      BeanUtils.copyProperties(task, model);
      model.add(linkTo(methodOn(TaskController.class).getTask(task.getId())).withSelfRel());
      return model;
    }
  }
}
