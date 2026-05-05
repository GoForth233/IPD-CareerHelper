package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_tasks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_agent_task_user_day_key", columnNames = {"user_id", "due_date", "task_key"})
}, indexes = {
        @Index(name = "idx_agent_tasks_user_due", columnList = "user_id, due_date"),
        @Index(name = "idx_agent_tasks_user_status", columnList = "user_id, status")
})
public class AgentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_key", nullable = false, length = 120)
    private String taskKey;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "task_type", nullable = false, length = 40)
    private String taskType;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "TODO";

    @Column(name = "target", length = 200)
    private String target;

    @Column(name = "source", nullable = false, length = 40)
    @Builder.Default
    private String source = "DAILY_AGENT";

    /** EASY | MEDIUM | HARD — set by TaskDecomposer (E5). */
    @Column(name = "difficulty", length = 20)
    private String difficulty;

    /** Estimated completion time in minutes, set by TaskDecomposer. */
    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    /** Non-null for sub-tasks; references the parent agent_tasks.task_id. */
    @Column(name = "parent_task_id")
    private Long parentTaskId;

    /** Ordering index within sibling sub-tasks (0-based). */
    @Column(name = "sub_index")
    private Integer subIndex;

    @Column(name = "due_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
