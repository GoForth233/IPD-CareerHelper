-- V4 F28a: Official interview question bank seed
-- source=OFFICIAL, review_status=PUBLISHED, status=APPROVED
-- Full 300-question expansion is generated weekly by QuestionAiGenerationJob
-- and can be added via scripts/seed_official_questions.sql

-- Backfill existing user-contributed questions
UPDATE `interview_questions`
SET `source` = 'USER', `review_status` = 'PUBLISHED'
WHERE (`source` IS NULL OR `source` = '')
  AND (`review_status` IS NULL OR `review_status` = '');

-- ── Java Backend (4) ─────────────────────────────────────────────────
INSERT INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Java Backend Developer', 'Easy',   'Java 中 == 和 equals() 有什么区别？', '==比较引用地址；equals()比较内容。String/Integer等重写了equals()按值比较。Integer缓存-128~127需注意==的陷阱。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Normal', 'synchronized 和 ReentrantLock 有何区别？何时选哪个？', 'synchronized是JVM关键字，可重入但不可中断；ReentrantLock是API，支持可中断/超时/公平模式/多Condition。需要高级特性时用ReentrantLock，否则synchronized更简洁。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Normal', '什么是 ThreadLocal？使用场景和内存泄漏风险？', '每线程独立变量副本，适合DB连接/用户身份隔离。线程池中必须在任务结束后remove()，否则弱引用key被GC后value泄漏。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Hard',   '描述 JVM 类加载机制和双亲委派模型，如何打破它？', '加载→验证→准备→解析→初始化。双亲委派：子加载器委托父加载器，保证核心类唯一。打破：重写loadClass（Tomcat隔离webapp、SPI的ContextClassLoader）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── Spring Framework (4) ─────────────────────────────────────────────
INSERT INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Spring Backend Developer', 'Easy',   'Spring Bean 的生命周期是什么？', '实例化→属性注入→Aware回调→BeanPostProcessor Before→@PostConstruct/afterPropertiesSet→BeanPostProcessor After→就绪→@PreDestroy/destroy-method→销毁。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Normal', '@Transactional 注解在哪些情况下会失效？', '①同类方法调用绕过代理 ②非public方法 ③受检异常未配rollbackFor ④多线程新线程操作DB ⑤吞异常未重抛 ⑥Bean非Spring管理 ⑦数据库不支持事务。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Normal', 'Spring 如何解决循环依赖问题？', '三级缓存（singletonObjects/earlySingletonObjects/singletonFactories）解决单例setter/字段注入循环依赖。构造器注入循环依赖无法自动解决，需@Lazy或重构。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Hard',   'Spring Boot 自动配置原理是什么？', '@EnableAutoConfiguration通过AutoConfigurationImportSelector从META-INF/spring/AutoConfiguration.imports加载候选配置类，@ConditionalOn*注解控制是否生效。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── MySQL (4) ────────────────────────────────────────────────────────
INSERT INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Backend Developer (MySQL)', 'Easy',   '什么是聚簇索引和非聚簇索引？', 'InnoDB聚簇索引叶子节点存完整行数据（按主键排序）；二级索引叶子存主键值，查询需回表。MyISAM只有非聚簇索引。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Normal', '什么是 MVCC？InnoDB 如何实现它？', 'InnoDB隐藏字段trx_id+roll_pointer（指向undo log）+ReadView实现。读已提交每次生成ReadView；可重复读事务开始时生成。读操作无需加锁。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Normal', '如何优化深分页查询（LIMIT M, N）？', '大OFFSET性能差（需扫M+N行丢M行）。优化：①延迟关联（先查主键分页再JOIN）②游标分页（WHERE id > last_id LIMIT N）③覆盖索引。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Hard',   '描述 MySQL 主从复制原理及延迟问题如何解决？', '主库写binlog，从库IO Thread读binlog写relay log，SQL Thread回放。延迟解决：MySQL 5.7+多线程并行回放、半同步复制、GTID快速恢复位点。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── Redis (4) ────────────────────────────────────────────────────────
INSERT INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Backend Developer (Redis)', 'Easy',   '缓存穿透、雪崩、击穿的区别和解决方案？', '穿透：查不存在key，用布隆过滤器+缓存空值；雪崩：大量缓存同时过期，TTL加随机抖动+高可用；击穿：热点key过期，互斥锁或逻辑过期。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (Redis)', 'Easy',   '如何用 Redis 实现分布式锁？', 'SET key uuid NX PX timeout_ms（原子加锁+超时）。释放用Lua脚本比较value再删（防误删他人锁）。Redisson提供watchdog自动续期。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (Redis)', 'Normal', 'Redis Cluster 数据分片原理？', '16384个slot，CRC16(key)%16384定位slot，每节点负责一部分slot。客户端路由：MOVED重定向。Gossip协议同步节点状态。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (Redis)', 'Hard',   'Redis 和 DB 的数据一致性如何保证？', 'Cache-Aside：写DB后删缓存（删而非更新）；延迟双删；Canal监听binlog异步更新（最终一致）。强一致场景以DB为唯一数据源，缓存只加速。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── Frontend (4) ─────────────────────────────────────────────────────
INSERT INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Frontend Developer (Vue)', 'Easy',   'Vue 2 和 Vue 3 的主要区别是什么？', 'Composition API、Proxy响应式（支持数组/新增属性）、性能提升（VDom重写/Tree-shaking）、TypeScript原生支持、Fragment/Teleport/Suspense新特性。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (Vue)', 'Normal', '什么是 Vue 3 的 Composition API？优势是什么？', '函数式组织组件逻辑。优势：①逻辑复用（composable函数替代mixin）②同一功能代码集中③TypeScript推断更好④更易单测。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (JavaScript)', 'Normal', '什么是事件循环（Event Loop）？', '同步代码（调用栈）→清空微任务队列（Promise.then）→执行一个宏任务（setTimeout/I/O）→再清微任务→渲染→循环。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Engineer', 'Normal', 'Webpack 和 Vite 的主要区别？', 'Webpack打包所有文件启动慢；Vite基于ESM按需编译，开发启动极快，利用esbuild预打包依赖，生产用Rollup打包。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');
