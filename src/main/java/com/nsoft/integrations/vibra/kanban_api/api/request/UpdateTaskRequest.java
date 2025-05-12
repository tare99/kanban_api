package com.nsoft.integrations.vibra.kanban_api.api.request;

import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskRequest(
    @NotBlank String title,
    String description,
    @NotNull TaskStatus status,
    @NotNull TaskPriority priority,
    @NotNull Long version) {}
