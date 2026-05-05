package com.group1.career.service;

import com.group1.career.model.entity.AgentTask;

import java.util.List;

public interface TaskDecomposer {

    /**
     * Break a parent task into concrete sub-tasks and persist them.
     * Idempotent: if sub-tasks already exist for {@code parentTaskId} the
     * existing list is returned without creating duplicates.
     *
     * @param userId     owner (used for ownership guard)
     * @param parentTaskId task to decompose
     * @return list of persisted sub-tasks, ordered by sub_index
     */
    List<AgentTask> decompose(Long userId, Long parentTaskId);
}
