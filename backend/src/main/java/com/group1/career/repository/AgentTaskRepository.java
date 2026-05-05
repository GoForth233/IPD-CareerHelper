package com.group1.career.repository;

import com.group1.career.model.entity.AgentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentTaskRepository extends JpaRepository<AgentTask, Long> {
    List<AgentTask> findByUserIdAndDueDateOrderByCreatedAtDesc(Long userId, LocalDate dueDate);
    List<AgentTask> findByUserIdAndStatusOrderByDueDateAscCreatedAtDesc(Long userId, String status);
    List<AgentTask> findByUserIdAndDueDateBetweenOrderByDueDateDescCreatedAtDesc(Long userId, LocalDate from, LocalDate to);
    Optional<AgentTask> findByUserIdAndDueDateAndTaskKey(Long userId, LocalDate dueDate, String taskKey);

    List<AgentTask> findByParentTaskIdOrderBySubIndexAsc(Long parentTaskId);

    boolean existsByParentTaskId(Long parentTaskId);
}
