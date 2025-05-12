package com.nsoft.integrations.vibra.kanban_api.domain.repository;

import com.nsoft.integrations.vibra.kanban_api.domain.enumeration.TaskStatus;
import com.nsoft.integrations.vibra.kanban_api.domain.model.Task;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class TaskSpecifications {
    public static Specification<Task> hasStatus(TaskStatus status) {
      return (root, cq, cb) ->
          status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
  }
}
