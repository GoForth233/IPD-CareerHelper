-- V8 F28a: Expand official question bank (+80 questions, total ~100)
-- All questions: source=OFFICIAL, review_status=PUBLISHED, status=APPROVED
-- Uses INSERT IGNORE to be idempotent on re-run.

-- ── Java Backend (10) ─────────────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Java Backend Developer', 'Easy',   'Java 中 final、finally、finalize 的区别？', 'final修饰类/方法/变量表示不可变/继承/修改；finally是try-catch中必定执行的块（System.exit除外）；finalize是Object的方法，GC回收前调用，已废弃不推荐使用。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Easy',   '什么是自动装箱和拆箱？有什么坑？', '编译器自动将基本类型和包装类互转。坑：Integer缓存范围-128~127，超出范围==比较返回false；null拆箱时抛NullPointerException；频繁装箱拆箱影响性能。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Normal', 'HashMap 的底层结构和扩容机制？', 'JDK8：数组+链表+红黑树。链表长度≥8且数组容量≥64时转红黑树。默认容量16，负载因子0.75，扩容2倍，重新哈希。多线程下put可能死循环（JDK7链表头插），JDK8改尾插但仍不安全。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Normal', 'volatile 关键字的作用和局限？', '保证可见性（写后刷主存、读前清缓存）和有序性（禁止指令重排）；但不保证原子性。适合状态标志位、DCL单例的instance变量。i++不是原子操作，volatile无法替代synchronized。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Normal', 'Java 线程池的核心参数和拒绝策略？', 'corePoolSize/maxPoolSize/keepAliveTime/unit/workQueue/threadFactory/handler。4种拒绝策略：AbortPolicy(抛异常)、CallerRunsPolicy(调用者执行)、DiscardPolicy(静默丢弃)、DiscardOldestPolicy(丢最旧任务)。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Normal', '什么是 CAS？ABA 问题如何解决？', 'Compare-And-Swap：比较内存值与预期值，相等则更新，否则重试。原子性由CPU指令保证。ABA问题：值从A改B再改回A，CAS无法感知。解决：AtomicStampedReference加版本号/时间戳。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Hard',   'Java 内存模型（JMM）核心概念？', '定义线程与主存的抽象关系。每线程有工作内存（寄存器/缓存副本），操作主存需通过read/load/use/assign/store/write 8种原子操作。happens-before规则：程序顺序/监视器锁/volatile/线程start/join。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Hard',   'G1 垃圾收集器的工作原理？', 'JDK9+默认收集器。将堆分为大小相等的Region（E/S/O/H）。优先回收垃圾最多Region（Garbage-First）。三色标记+SATB解决并发标记漏标。Mixed GC混合回收老年代，可设停顿时间目标。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Hard',   '如何排查 Java 进程 CPU 飙高问题？', '①top找高CPU进程PID ②top -Hp {pid}找高CPU线程TID ③printf "%x" {tid}转16进制 ④jstack {pid}|grep {16进制tid}定位栈帧 ⑤分析是死循环/锁竞争/GC过频。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Java Backend Developer', 'Hard',   '描述 AQS 的核心原理？', 'AbstractQueuedSynchronizer：int state（同步状态）+ CLH双向队列（等待线程）。子类实现tryAcquire/tryRelease。独占模式：ReentrantLock；共享模式：Semaphore/CountDownLatch。park/unpark挂起/唤醒线程。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── Spring / Spring Boot (8) ──────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Spring Backend Developer', 'Easy',   'Spring IoC 和 DI 的区别？', 'IoC（控制反转）是设计思想：对象不自己创建依赖，由容器负责。DI（依赖注入）是IoC的具体实现方式：构造器注入/Setter注入/字段注入。Spring的BeanFactory/ApplicationContext是IoC容器。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Easy',   '@Component、@Service、@Repository、@Controller 的区别？', '均派生自@Component（语义不同，功能基本相同）。@Repository额外处理数据访问异常转换；@Controller配合@RequestMapping处理MVC；@Service无额外语义，标注业务层；@RestController=@Controller+@ResponseBody。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Normal', 'Spring AOP 的实现原理？', '动态代理：①目标类实现接口→JDK动态代理（Proxy.newProxyInstance）②无接口→CGLIB字节码子类代理。切面=通知（Before/After/Around等）+切点（Pointcut表达式）。AspectJ编译期织入性能更好但需编译器支持。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Normal', 'Spring Bean 的作用域有哪些？', 'singleton（默认，容器单例）、prototype（每次getBean新建）、request（HTTP请求级）、session（会话级）、application（ServletContext级）、websocket。prototype不受容器管理销毁。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Normal', 'Spring MVC 请求处理完整流程？', 'DispatcherServlet→HandlerMapping找Handler→HandlerAdapter调用Controller方法→Controller返回ModelAndView→ViewResolver解析→View渲染→响应。@ResponseBody时跳过视图解析，直接HttpMessageConverter序列化。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Hard',   'Spring Security 认证与授权流程？', '认证：FilterChain→UsernamePasswordAuthenticationFilter→AuthenticationManager→UserDetailsService→密码匹配→SecurityContext存认证结果。授权：FilterSecurityInterceptor→AccessDecisionManager→Voter投票。JWT无状态：每次从token解析Authentication存入SecurityContext。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Hard',   'Spring 事务传播行为有哪几种？最常见的两种？', '7种：REQUIRED（默认，加入或新建）、REQUIRES_NEW（总新建，挂起外层）、NESTED（嵌套）、SUPPORTS、NOT_SUPPORTED、MANDATORY、NEVER。最常用：REQUIRED（业务方法）、REQUIRES_NEW（日志/消息发送需独立提交）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Spring Backend Developer', 'Hard',   'Spring Boot Actuator 有哪些常用端点？', '/health（健康）/info（信息）/metrics（指标）/env（环境变量）/beans（所有Bean）/mappings（URL映射）/threaddump/heapdump。生产建议关闭/限制/env/beans等敏感端点，通过management.endpoints.web.exposure.include配置。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── MySQL 扩展 (8) ────────────────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Backend Developer (MySQL)', 'Easy',   'MySQL 事务的四个特性（ACID）？', '原子性（Atomicity）：事务全成功或全回滚；一致性（Consistency）：事务前后数据完整性约束保持；隔离性（Isolation）：并发事务互不干扰；持久性（Durability）：提交后数据永久保存。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Easy',   'MySQL 的四种隔离级别分别解决了什么问题？', '读未提交（脏读√）→读已提交（解决脏读）→可重复读（解决不可重复读，MySQL默认）→串行化（解决幻读，但并发性最差）。InnoDB通过MVCC+Gap Lock在可重复读级别也基本解决幻读。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Normal', '索引的最左前缀原则是什么？', '联合索引(a,b,c)：查询条件必须从最左列开始且不跳过。a、a+b、a+b+c可走索引；b、b+c、c单独查不走。a+c只有a列走索引。中间列用范围查询时右侧列失效。EXPLAIN的key_len可验证。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Normal', '什么是覆盖索引？有什么好处？', '查询所需所有列都在索引树上，无需回表查主键。好处：减少I/O，EXPLAIN显示Extra: Using index。实践：SELECT的列+WHERE列全部在联合索引中；count(*)走覆盖索引性能好。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Normal', 'Explain 输出中哪些字段最重要？', 'type（访问类型，好→差：system>const>eq_ref>ref>range>index>ALL，ALL全表扫需优化）；key（实际用到的索引）；rows（预估扫描行数）；Extra（Using filesort/temporary需关注）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Hard',   'InnoDB 的行锁算法有哪些？', 'Record Lock（锁单行）；Gap Lock（锁间隙，防幻读）；Next-Key Lock=Record+Gap（默认，锁当前行+之前间隙）。唯一索引等值查询退化为Record Lock；范围查询用Next-Key。间隙锁只在可重复读以上级别生效。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Hard',   '如何分析和解决 MySQL 死锁？', '①SHOW ENGINE INNODB STATUS查最近死锁日志 ②分析加锁顺序，事务1锁A→B，事务2锁B→A形成环 ③解决：统一加锁顺序；缩小事务范围；SELECT...FOR UPDATE替代悲观锁分散加锁。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (MySQL)', 'Hard',   'binlog 的三种格式区别？', 'STATEMENT：记录SQL语句，小但某些函数（NOW/UUID）不安全；ROW：记录行变更前后数据，安全但量大；MIXED：默认STATEMENT，遇不安全语句自动切ROW。主从复制和数据恢复推荐ROW。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── Redis 扩展 (6) ────────────────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Backend Developer (Redis)', 'Easy',   'Redis 的五种基本数据类型及应用场景？', 'String（计数器/缓存/Session）；List（消息队列/最新列表）；Hash（对象存储/购物车）；Set（去重/共同好友）；ZSet（排行榜/延迟队列）。扩展：HyperLogLog（UV统计）/Bitmap（签到）/Geo（地理位置）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (Redis)', 'Normal', 'Redis 持久化 RDB 和 AOF 的区别？', 'RDB：快照（fork子进程bgsave），恢复快文件小，可能丢最后一次快照数据。AOF：记录写命令（appendfsync everysec），数据更完整但文件大，AOFRW压缩。Redis 7支持RDB+AOF混合（默认开启），兼顾二者优点。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (Redis)', 'Normal', 'Redis 过期删除策略和内存淘汰策略？', '过期删除：惰性删除（访问时检查）+定期删除（每100ms随机抽样）。内存淘汰（maxmemory-policy）：noeviction/allkeys-lru/volatile-lru/allkeys-lfu/volatile-ttl等8种，缓存场景推荐allkeys-lru。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (Redis)', 'Hard',   'Redis 哨兵和 Cluster 的区别？', '哨兵：主从复制+自动故障转移，单节点数据量受限，适合中小规模。Cluster：数据分片（16384 slot），横向扩展，每个节点主从，自动故障转移，适合大数据量高并发。Cluster不支持多key跨slot原子操作。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (Redis)', 'Hard',   'Redis 大 Key 问题如何发现和处理？', '发现：redis-cli --bigkeys；SCAN+DEBUG OBJECT；RDB分析工具rdb-tools。危害：内存不均/网络阻塞/集群迁移慢。处理：拆分（Hash按字段分桶/List分页）；异步删除UNLINK；定期清理过期。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Backend Developer (Redis)', 'Hard',   '如何用 Redis 实现延迟队列？', 'ZSet方案：score=执行时间戳，ZADD加任务，消费者ZRANGEBYSCORE取到期任务处理，ZREM删除（需原子化防重复消费，用Lua或ZPOPMIN）。对比MQ：轻量但不支持消息持久化和ACK，适合轻量场景。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── 前端扩展 (10) ─────────────────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Frontend Developer (Vue)', 'Easy',   'v-show 和 v-if 的区别？', 'v-if：条件为假时DOM不渲染（惰性，适合不频繁切换）；v-show：始终渲染，display:none切换（适合频繁切换）。v-if切换开销更大；v-show初始渲染开销更大。列表中v-for优先级高于v-if（Vue3中v-if优先）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (Vue)', 'Normal', 'Vue 3 响应式原理（Proxy vs defineProperty）？', 'Vue2用Object.defineProperty劫持属性，无法检测新增/删除属性和数组索引修改，需$set/$delete。Vue3用Proxy代理整个对象，可检测所有操作，配合Reflect转发。reactive底层是Proxy；ref对基本类型用{value}包装再Proxy。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (Vue)', 'Normal', 'Vue 3 中 watch 和 watchEffect 的区别？', 'watch：明确指定侦听源，懒执行（首次不触发），可获取新旧值，适合有副作用的异步操作。watchEffect：自动追踪依赖，立即执行，无旧值，适合同步副作用。两者都返回停止函数，可在onUnmounted中调用。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (Vue)', 'Normal', 'Vue 3 组件通信方式有哪些？', 'props/$emit（父子）；provide/inject（跨层级）；ref/expose（父获子实例）；Pinia/Vuex（全局状态）；事件总线（mitt）；$attrs穿透。组合式API中defineProps/defineEmits/defineExpose替代选项式写法。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (Vue)', 'Hard',   'Vue 3 虚拟 DOM 和 diff 算法优化？', 'Vue3 VNode加PatchFlag标记动态节点（编译时分析），diff跳过静态节点。Fragment支持多根。Block Tree：只收集动态节点做diff，复杂度从O(n)降至O(动态节点数)。比Vue2快2-3倍。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (JavaScript)', 'Easy',   'let、const、var 的区别？', 'var：函数作用域，变量提升（undefined），可重复声明。let：块级作用域，暂时性死区（TDZ），不可重复声明。const：块级+声明时必须赋值+不可重新赋值（引用类型内容可变）。推荐：默认const，需变化用let，不用var。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (JavaScript)', 'Normal', '什么是闭包？常见应用场景？', '函数能访问其词法作用域外变量的特性，即内部函数保持对外部函数变量的引用。应用：①防抖节流（保存timer）②模块私有变量③柯里化④React hooks（useState每次渲染闭包捕获当前值）。副作用：过度使用导致内存泄漏。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Developer (JavaScript)', 'Normal', 'Promise、async/await 的错误处理？', 'Promise链：.catch()捕获链上任意rejection，或每个.then第二参数。async/await：try-catch包裹await语句；也可用.catch附在async函数调用上。Promise.allSettled处理多个请求部分失败。未捕获rejection触发unhandledRejection。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Engineer', 'Normal', '浏览器从输入URL到页面显示经历了什么？', 'DNS解析→TCP三次握手→HTTP请求→服务器响应→浏览器解析HTML（DOM树）→解析CSS（CSSOM）→构建Render Tree→Layout（回流）→Paint（重绘）→Composite（合成层）。JS阻塞解析，async/defer异步加载。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Frontend Engineer', 'Hard',   '什么是 CSS 重排（Reflow）和重绘（Repaint）？如何优化？', '重排：几何属性变化（width/position/font-size）触发布局重算，开销大。重绘：视觉属性变化（color/background）无需重排，开销较小。优化：①transform/opacity走合成层不触发重排②批量DOM操作用DocumentFragment③避免强制同步布局（读写分离）④will-change提示GPU加速。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── 通用 HR / 行为面试 (8) ───────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('General (HR)', 'Easy',   '请做一个自我介绍。', '结构：基本情况（姓名/学校/专业）→核心经历（最亮眼的1-2段实习/项目）→与岗位的契合点→一句结语表达意愿。时长1-2分钟，提前准备并练习，不要背稿但要熟悉框架。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('General (HR)', 'Easy',   '你的优点和缺点分别是什么？', '优点选与岗位相关且有具体事例支撑的。缺点选真实但不致命（如"有时过于追求完美导致效率下降，现在学会了 80分就行动"），并说明改进措施。避免说"我的缺点是太努力了"这类套话。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('General (HR)', 'Normal', '你为什么选择我们公司/这个岗位？', '三层结构：①公司维度（产品/技术/文化具体点，查过JD和官网）②岗位维度（与自己经历的匹配点）③个人成长（能在此岗位获得什么能力）。避免说"贵公司很有名"等泛泛之谈。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('General (HR)', 'Normal', '描述一次你解决困难问题的经历。（STAR法则）', 'STAR框架：Situation（背景）→Task（任务/目标）→Action（你的具体行动，用"我"不用"我们"）→Result（量化结果）。选能体现主动性和技术能力的案例，结果尽量数字化（提升XX%/减少XX小时）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('General (HR)', 'Normal', '你对未来 3-5 年的职业规划是什么？', '分阶段作答：1-2年（扎实技术基础，成为所在方向的熟练开发/分析/产品）；3-5年（横向拓展或纵深专家路线，承担更大模块或带初级成员）。方向要与应聘公司/岗位方向一致，展现思考而非随口说。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('General (HR)', 'Hard',   '描述一次与同事或上级发生分歧的经历，你是如何处理的？', '重点：展示沟通能力和成熟度，不抱怨对方。结构：①描述分歧背景②你的处理方式（主动沟通/换位思考/用数据说话）③最终达成共识或各自让步④学到什么。避免选与领导激烈冲突的案例。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('General (HR)', 'Hard',   '你有哪些问题想问我们？（反问环节）', '好的反问：①"新人入职后通常如何快速上手，有哪些资源？"②"团队当前技术栈演进方向？"③"您认为这个岗位成功的关键是什么？"。避免问薪酬福利（除非HR主动）和问已在JD写明的内容。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('General (HR)', 'Normal', '如何介绍你最有挑战性的项目？', 'PAMS框架：Problem（项目背景和核心挑战）→Approach（你的解决思路和技术选型理由）→My role（你在其中的具体职责）→Scale/Result（项目规模和结果）。突出个人贡献，数字量化，1-2分钟内说完。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── 数据结构与算法 (10) ───────────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Algorithm & Data Structure', 'Easy',   '数组和链表的时间复杂度对比？', '数组：随机访问O(1)，头部插入/删除O(n)，尾部O(1)。链表：随机访问O(n)，已知节点插入/删除O(1)，头部O(1)。数组内存连续，缓存友好；链表内存分散，指针开销。实际中ArrayList vs LinkedList。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Easy',   '栈和队列的区别及应用场景？', '栈：LIFO（后进先出）。应用：函数调用栈/括号匹配/浏览器前进后退/DFS迭代实现。队列：FIFO（先进先出）。应用：BFS/消息队列/线程池任务队列。双端队列（Deque）兼备两端O(1)操作。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Normal', '什么是二叉搜索树（BST）？有什么问题？', 'BST：左子树所有节点<根<右子树所有节点，查找/插入/删除平均O(logN)。问题：有序插入退化成链表O(N)。解决：AVL树（严格平衡，旋转多）、红黑树（弱平衡，插删快，HashMap/TreeMap用）、B+树（磁盘友好，DB索引用）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Normal', '常用排序算法的时间复杂度？稳定性？', '快排：O(NlogN)平均，不稳定；归并：O(NlogN)稳定；堆排：O(NlogN)不稳定；冒泡/插入：O(N²)稳定。TimSort（Java Arrays.sort/Python）：归并+插入，O(NlogN)稳定。大数据场景：外排序（归并分治）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Normal', '什么是动态规划？适合解决什么问题？', '将大问题拆成有重叠子问题，记录子问题解（记忆化/dp数组）避免重复计算。三要素：状态定义、状态转移方程、初始条件。适合：最优子结构+重叠子问题。典型：背包/最长公共子序列/编辑距离/股票买卖。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Normal', 'BFS 和 DFS 的适用场景？', 'BFS（队列）：求最短路径/层序遍历/拓扑排序。DFS（栈/递归）：全路径枚举/连通分量/回溯（全排列/N皇后）。图的BFS时间O(V+E)；树的BFS空间O(最宽层节点数)，DFS空间O(深度)。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Hard',   '如何检测链表是否有环？找环入口？', '快慢指针（Floyd）：slow每次1步，fast每次2步，相遇则有环。找入口：相遇后slow回head，两指针同速前进，再次相遇即入口（数学证明：head到入口=相遇点到入口）。空间O(1)，时间O(N)。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Hard',   '什么是 LRU 缓存？如何用 O(1) 实现？', 'Least Recently Used：淘汰最久未访问的。O(1)实现：HashMap+双向链表。HashMap的key映射链表节点；get/put时将节点移到链表头；超容量删链表尾节点并从Map移除。Java：LinkedHashMap(capacity, 0.75f, true)可直接用。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Hard',   '什么是一致性哈希？解决了什么问题？', '普通哈希取模，节点增删导致大量key重新映射（缓存雪崩）。一致性哈希：节点和key映射到同一哈希环，key顺时针找最近节点。节点增删只影响相邻区间。虚拟节点解决数据倾斜。分布式缓存/负载均衡常用。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Algorithm & Data Structure', 'Hard',   'Trie 树（前缀树）的原理和应用？', '多叉树，每个节点代表一个字符，从根到叶的路径表示一个完整字符串。插入/查找O(L)（L=字符串长度）。应用：搜索框自动补全/词频统计/IP路由最长前缀匹配/拼写检查。空间换时间，节点数=所有字符串字符数。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── 产品经理 (8) ─────────────────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Product Manager', 'Easy',   '请描述你对产品经理核心职责的理解。', '发现并定义用户真实需求→将需求转化为可落地的产品方案→推动研发/设计/运营跨部门协作→上线后度量效果并迭代。核心是"解决正确问题"，而非只是"写需求文档"。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Product Manager', 'Easy',   '如何区分用户需求和伪需求？', '需求来源：用户调研/数据分析/竞品/运营反馈。验证真需求：①规模（有多少人有此问题）②频率（多高频）③强度（有多痛）④是否有替代方案。伪需求：用户嘴上说要但行为上不用（亨利·福特"更快的马"）。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Product Manager', 'Normal', '如何做竞品分析？', '五层框架：①战略层（目标用户/商业模式）②范围层（核心功能矩阵）③结构层（信息架构/流程）④框架层（交互和布局）⑤表现层（视觉设计）。结论要说"所以我们应该做X而不做Y"，而不只是罗列差异。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Product Manager', 'Normal', '什么是 MVP？何时使用？', 'Minimum Viable Product：以最小成本验证核心假设。适用：不确定市场/用户是否接受新功能时，先用最简版本（甚至人工模拟后端）验证用户是否愿意使用/付费。重点：验证假设，而非做出残缺品。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Product Manager', 'Normal', '如何确定功能优先级？', '常用框架：RICE（Reach/Impact/Confidence/Effort）；ICE；Kano模型（基础必备/期望型/兴奋型/无差异/反向）；MoSCoW（Must/Should/Could/Won\'t）。最终结合战略目标、ROI、技术可行性和时间窗口综合判断。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Product Manager', 'Hard',   '如何设计一款"XX类"产品的冷启动方案？', '用供给侧/需求侧分析：①供给侧（内容/商品/服务提供者）从哪里来——邀请/BD/补贴 ②需求侧如何获取种子用户——垂直社区/裂变/KOL ③如何解决鸡蛋问题——先聚焦一个细分市场做深 ④最小闭环验证PMF。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Product Manager', 'Hard',   '如何定义和拆解一个核心北极星指标？', '北极星指标（NSM）：反映用户从产品获得核心价值的单一指标（如日活/GMV/完课率）。拆解：NSM = 用户数×活跃率×单用户价值。每层再拆：用户数=新增+留存-流失。找到可影响的叶子节点指标做实验。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Product Manager', 'Hard',   '你负责的产品某核心指标突然下降30%，怎么排查？', '①确认数据准确性（埋点/统计口径是否变化）②时间线对比（是否与上线/节假日/外部事件重合）③分层拆解（哪个用户群/渠道/功能路径降幅最大）④漏斗分析（哪步流失增多）⑤用户访谈⑥提出假设并验证。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');

-- ── 数据分析 (8) ─────────────────────────────────────────────────────
INSERT IGNORE INTO `interview_questions` (position, difficulty, content, answer, source, review_status, status) VALUES
('Data Analyst', 'Easy',   'SQL 中 INNER JOIN、LEFT JOIN、RIGHT JOIN 的区别？', 'INNER JOIN：只返回两表都有匹配的行。LEFT JOIN：返回左表所有行，右表无匹配填NULL。RIGHT JOIN：返回右表所有行，左表无匹配填NULL。FULL OUTER JOIN（MySQL不直接支持，用UNION模拟）：返回两表所有行。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Data Analyst', 'Easy',   '什么是数据库的窗口函数？', '窗口函数（Window Function）在不折叠行的情况下计算聚合。OVER()定义窗口。常用：ROW_NUMBER()行号；RANK()/DENSE_RANK()排名；LAG/LEAD前后行取值；SUM/AVG OVER(PARTITION BY...)分组累计。比子查询性能好。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Data Analyst', 'Normal', '什么是 A/B 测试？如何设计一个有效的实验？', 'A/B测试：对照实验，将用户随机分组，比较不同版本的效果。设计步骤：①明确假设和北极星指标 ②计算样本量（统计功效≥80%，显著性水平5%）③随机分组（避免污染）④设定实验周期 ⑤用T检验/卡方检验判断显著性 ⑥警惕多重检验问题。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Data Analyst', 'Normal', '如何用 SQL 计算用户留存率？', 'SELECT DATE(first_date) AS cohort, COUNT(DISTINCT user_id) AS day0, COUNT(DISTINCT CASE WHEN DATE(event_date)-DATE(first_date)=1 THEN user_id END)/COUNT(DISTINCT user_id) AS day1_retention FROM (用户首次行为表LEFT JOIN事件表 ON user_id) GROUP BY cohort。重点：同期群分析，按注册日期分组。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Data Analyst', 'Normal', '均值和中位数分别适合什么场景？', '均值：数据对称分布且无极端值（如身高/标准化考试分数）。中位数：数据偏态或有极端值（如薪资/房价/用户在线时长），能真实反映典型用户行为。实际中两者都报，差异大时说明数据偏态严重。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Data Analyst', 'Hard',   '什么是辛普森悖论？在数据分析中如何避免？', '分组数据的趋势与汇总数据相反。例：A方案分男女看都比B好，但总体看B更好（因为男女在两方案中的比例不同）。避免：①分析前明确分层变量 ②汇总前检查各子组样本量是否均衡 ③对重要结论做分层验证。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Data Analyst', 'Hard',   '如何判断两个指标之间是相关还是因果？', '相关≠因果。建立因果需要：①时序（原因在先）②相关性 ③排除混淆变量。方法：RCT（随机对照实验）是金标准；准实验：DID（差分中的差分）/IV（工具变量）/RDD（断点回归）。实际工作中要说"X与Y相关，我们尝试A/B测试验证因果"。', 'OFFICIAL', 'PUBLISHED', 'APPROVED'),
('Data Analyst', 'Hard',   '什么是漏斗分析？如何找到流失最严重的环节？', '漏斗分析：追踪用户完成多步骤目标（注册→激活→付费）的转化率。找流失：①计算各步骤转化率，找跌幅最大的步骤 ②对该步骤做细分（设备/渠道/用户画像）看哪类用户流失最多 ③结合用户访谈和录屏分析行为。', 'OFFICIAL', 'PUBLISHED', 'APPROVED');
