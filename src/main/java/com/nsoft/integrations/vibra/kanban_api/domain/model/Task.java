package com.nsoft.integrations.vibra.kanban_api.domain.model;

import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskPriority;
import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String title;

  private String description;

  @Enumerated(EnumType.STRING)
  private TaskStatus status;

  @Enumerated(EnumType.STRING)
  private TaskPriority priority;

  @Version private Long version;
}
