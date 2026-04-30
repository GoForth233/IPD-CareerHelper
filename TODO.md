# CareerLoop — 待办事项

> 更新于 2026-04-30。Sprint A/B/C/D 全部完成（详见 `feature/product-completion-2026-04`）。
> 后续技术债 / V2 需求迁移到 `BACKLOG.md`。
> 部署事宜暂不在此列。

---

## 完成情况

### Sprint C 尾项

- C-1 摄像头帧分析 / Body Language 维度 ✅
  - Python sidecar 方案（FastAPI + MediaPipe）落在 `body-lang-sidecar/`
  - Spring Boot 通过 `BodyLanguageService` HTTP 调用 sidecar，缓存帧分数
  - 前端 `pages/interview/room.vue` 每 3 秒截帧上传，`pages/interview/report.vue`
    雷达图扩成 6 维（Body Language 维度可空缺时自动降级）

### Sprint D — 行为流量层 + B 端骨架

- D-1 7 日打卡 ✅
  - `check_ins` 表 + `CheckInService` + `CheckInController`
  - 测评 / 面试 / 技能节点完成时自动触发打卡
  - `home/index.vue` 打卡卡片 + `pages/checkin/index.vue` 日历视图

- D-2 周报推送 ✅
  - `WeeklyReportService` + `WeeklyReportJob`（每周一 09:00 Asia/Shanghai）
  - Qwen 生成 50 字进步文案 → `notifications.type=WEEKLY_REPORT`
  - 管理员接口 `POST /api/admin/weekly-report/run`、`/run-user` 可手动触发

- D-3 面经市场 ✅
  - `interview_questions` 表 + `QuestionBankController`（`/api/questions/*`）
  - 面试开场提示词概率从题库抽题
  - `pages/interview/report.vue` 贡献入口 + `pages/market/index.vue` 题库浏览/点赞

- D-4 B 端管理控制台骨架 ✅
  - `admin-frontend/`（独立 Vite + Vue3 + Element Plus 项目）
  - 登录 → Dashboard（机构雷达均值/薄弱维度 TOP3）→ 学生列表
  - 后端 `AdminController` + `AdminAuthService`，沿用 C 端 JWT 但 `requireAdmin()` 把控

- D-5 A 端 Career Paths 节点 + 题库 CRUD ✅
  - `AdminController` 内置 `/career-paths`、`/career-paths/nodes`、`/questions` CRUD
  - 前端 admin 页 `views/SkillMap.vue`、`views/QuestionBank.vue`

---

## 主页 Bilibili 改造（产品需求）

- 后端 `BilibiliClient` 实现 wbi 签名 + `HomeContentRefreshJob`（每日 03:00）
- 新表：`home_videos`、`home_articles`、`home_consultations`
- `HomepageController` 返回 `videos / articles / consultations / careerCards`
  四个 section，视频按 `userId + dayOfYear` 抽样，「每日切批次但同一天稳定」
- 前端 `pages/home/index.vue` 重写：横滑视频区 + 文章 + 资讯 + 下拉刷新
- 微信白名单清单见 `WECHAT_MP_CONFIG.md`

---

## 已知关键 Bug 修复（10 项）

1. ✅ IDOR — `AssessmentController.getRecord` 校验所有权
2. ✅ 越权 — `ChatHistoryController` 所有 endpoint 加 `assertOwnership`
3. ✅ JWT 弱默认 — `JwtUtils` 缺 secret 直接抛 `IllegalStateException`
4. ✅ AI 系统提示重叠 — 仅当首条非 system 时注入
5. ✅ 流式 chat 丢历史 — `ChatController.streamMessage` 解析 `historyJson`
6. ✅ 面试报告维度对不上 — `extractDimensions` 改读 `radarChart{}`
7. ✅ 通知中心点旧通知崩溃 — 缺 `interviewId` 跳 history + toast
8. ✅ 面试历史 ONGOING 路由错 — `Interview.mode` 字段 + 前端按 mode 跳
9. ✅ 游客模式冷启动被踢回登录 — `enterGuestMode` + `isGuest`
10. ✅ OSS 孤儿文件 — `ResumeServiceImpl.deleteResume` 调用 `fileService.deleteObject`

---

> 后续技术债（数字人皮肤、需补强自动联动等）见 [BACKLOG.md](./BACKLOG.md)。
