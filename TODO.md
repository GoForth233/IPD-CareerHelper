# CareerLoop — 待办事项

> 更新于 2026-04-30。Sprint A/B 已完成，Sprint C 最后一项待做，Sprint D 全部待做。
> 部署事宜暂不在此列。

---

## Sprint C 尾项

### C-1 摄像头帧分析 / Body Language 维度 ❌
**目标**：面试过程中每 2 秒截一帧，分析用户的眼神接触、表情、姿态稳定性，写入面试 report 第 6 维度 "Body Language"。

**方案选项（需决策）**：
- **方案 A — Python sidecar**：独立 FastAPI 微服务，接收 base64 帧，用 MediaPipe / OpenCV 返回分数；Spring Boot 通过 HTTP 调用。
- **方案 B — Java + OpenCV**：在 Spring Boot 内直接用 `org.openpgp:openpgputils` 或 `nu.pattern:opencv`，省去独立服务，但 JNI 库部署麻烦。

**涉及文件**：
- 后端：新增 `BodyLanguageService` + `BodyLanguageController`（接收帧）
- 后端：`InterviewController` 面试结束时汇总帧分析得分写入 report
- 前端：`room.vue` 加定时截帧逻辑（每 2s 用 `<camera>` 的 `takePhoto` 接口抓图，上传帧）
- 前端：`interview/report` 页展示新增的 Body Language 维度雷达图

---

## Sprint D — 行为流量层 + B 端骨架

### D-1 7 日打卡 ❌
**目标**：用户完成"测评 1 次 + 面试 3 次 + 技能节点 2 个"可解锁"求职闭环"徽章，推动每日回访。

**涉及文件**：
- 后端：新建 `check_in` 表 + `CheckInService` + `CheckInController`（`POST /api/checkin`，`GET /api/checkin/status`）
- 后端：测评 / 面试 / 技能节点完成时自动触发打卡写入
- 前端：`home/index.vue` 加打卡进度条卡片（今日进度 / 连续天数 / 徽章展示）
- 前端：新增 `pages/checkin/index.vue`（历史日历视图）
- 数据库：`CREATE TABLE check_ins (id, user_id, date, actions JSON, streak INT)`

### D-2 周报推送 ❌
**目标**：每周一推送"本周 vs 上周"面试进步对比到消息中心，基于真实 report 数据。

**涉及文件**：
- 后端：`WeeklyReportJob.java`（Spring `@Scheduled`，每周一 09:00）
- 后端：查询用户近两周 interview report，计算各维度均值差值，生成自然语言文案（调 Qwen）
- 后端：写入 `notifications` 表，`type = WEEKLY_REPORT`
- 前端：`messages/index.vue` 已有通知列表，周报会自动出现在里面（无需改前端）

### D-3 面经市场 ❌
**目标**：面试结束后弹窗"贡献本次题目？"，将 AI 生成的问题匿名入库；新用户面试时有概率抽到真实面经题。

**涉及文件**：
- 后端：新建 `interview_questions` 表（`id, position, difficulty, question_text, source, used_count`）
- 后端：`QuestionBankController`（`POST /api/questions/contribute`，`GET /api/questions/random?position=&difficulty=`）
- 后端：`InterviewController.startInterview` 在生成开场问题前，先从题库随机抽一道加入 prompt（概率 50%）
- 前端：`room.vue` / `chat.vue` 面试结束后弹"贡献题目"弹窗
- 前端：新增 `pages/market/index.vue`（题库浏览页，可按岗位/难度筛选）

### D-4 B 端管理控制台骨架 ❌
**目标**：独立部署的 Vue3 + Element Plus Web 应用（非小程序），给机构/学校管理员用。

**涉及文件**（全部新建，独立于 `frontend/` 目录）：
- `admin-frontend/`：`vite` + `vue3` + `element-plus` 项目
  - `pages/login.vue`（管理员账号密码登录）
  - `pages/dashboard.vue`（机构内学生面试雷达均值大盘）
  - `pages/students.vue`（学生列表 + 面试记录查看）
- 后端：新建 `AdminController`（`/api/admin/*`，需 `ADMIN` 角色 JWT）
- 后端：`AdminService`（按机构 org_id 聚合学生数据）
- 数据库：`organizations` 表 + `users.org_id` 外键

### D-5 A 端 Career Paths 节点 + 题库 CRUD ❌
**目标**：在 B 端控制台里让管理员可以编辑技能地图节点和测评题库，不用直接改数据库。

**涉及文件**：
- 后端：`CareerPathAdminController`（`POST/PUT/DELETE /api/admin/career-paths`）
- 后端：`AssessmentAdminController`（`POST/PUT/DELETE /api/admin/assessments/questions`）
- 前端（admin）：`pages/career-paths.vue`（节点树形编辑器）
- 前端（admin）：`pages/question-bank.vue`（题目 CRUD 表格）

---

## 已知技术债（不影响主流程，记录备用）

| 项 | 说明 |
|----|------|
| OSS 文件孤儿 | 删除简历时只删 DB 记录，不删 OSS 文件。需在 `ResumeServiceImpl.deleteResume` 里加 `fileService.deleteObject(resume.getFileUrl())` |
| 数字人面试官皮肤 | 当前只有一套蓝色 CSS 头像。V2 需提供 3-5 套预设皮肤供用户在 `start.vue` 选择 |
| 面试报告维度 | 当前 5 维，Body Language (Sprint C-1 完成后) 变 6 维，`report.vue` 雷达图需同步扩展 |
| 技能地图"需补强"标记 | 面试报告弱维度应该自动在 `map/index.vue` 对应节点加"需补强"标签，目前未接通 |
| WeChat mini-program silent mode 提示 | `room.vue` 虽已加 `obeyMuteSwitch=false`，但建议在面试开始前加一个"请确认音量已开"提示 |
