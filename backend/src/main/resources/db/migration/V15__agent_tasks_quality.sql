ALTER TABLE `agent_tasks`
  ADD COLUMN `difficulty`        VARCHAR(20)  NULL AFTER `source`,
  ADD COLUMN `estimated_minutes` INT          NULL AFTER `difficulty`,
  ADD COLUMN `parent_task_id`    BIGINT       NULL AFTER `estimated_minutes`,
  ADD COLUMN `sub_index`         INT          NULL AFTER `parent_task_id`,
  ADD KEY `idx_agent_tasks_parent` (`parent_task_id`);
