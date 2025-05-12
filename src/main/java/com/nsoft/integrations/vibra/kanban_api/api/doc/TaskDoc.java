package com.nsoft.integrations.vibra.kanban_api.api.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "TaskDoc", description = "Representation of a Kanban task with HATEOAS links")
@Setter
@Getter
public class TaskDoc {
  @Schema(example = "1")
  private Long id;

  @Schema(example = "Task title")
  private String title;

  @Schema(example = "Custom description")
  private String description;

  @Schema(example = "TO_DO")
  private TaskStatus status;

  @Schema(example = "LOW")
  private TaskPriority priority;

  @Schema(example = "0")
  private Long version;

  @Schema(description = "Resource links")
  @JsonProperty("_links")
  private LinkDoc _links;
}
