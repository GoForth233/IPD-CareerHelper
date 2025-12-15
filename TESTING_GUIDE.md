# 🧪 Testing Quick Start Guide

## 如何运行测试

### 方法一：在 IntelliJ IDEA 中运行（推荐）

#### 运行所有测试
1. 在 IDEA 中，右键点击 `backend/src/test/java` 目录
2. 选择 "Run 'Tests in 'java''"
3. 查看测试结果面板

#### 运行单个测试类
1. 打开测试文件（例如 `UserServiceTest.java`）
2. 点击类名旁边的绿色运行按钮
3. 或右键点击类名 → "Run 'UserServiceTest'"

#### 运行单个测试方法
1. 点击测试方法旁边的绿色运行按钮
2. 或右键点击方法名 → "Run 'testRegister_Success()'"

### 方法二：使用 Maven 命令行

```bash
cd backend

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 运行特定测试方法
mvn test -Dtest=UserServiceTest#testRegister_Success

# 跳过测试（构建时）
mvn package -DskipTests
```

---

## 📂 测试文件结构

```
backend/src/test/java/com/group1/career/
├── controller/              # Controller 层测试 (API 端点)
│   ├── CareerControllerTest.java      ✅ 7 tests
│   ├── FileControllerTest.java        ✅ 3 tests
│   ├── InterviewControllerTest.java   ✅ 5 tests
│   ├── ResumeControllerTest.java      ✅ 3 tests
│   └── UserControllerTest.java        ✅ 3 tests
│
├── service/                 # Service 层测试 (业务逻辑)
│   ├── CareerServiceTest.java         ✅ 6 tests
│   ├── FileServiceTest.java           ✅ 4 tests
│   ├── InterviewServiceTest.java      ✅ 6 tests
│   ├── ResumeServiceTest.java         ✅ 3 tests
│   └── UserServiceTest.java           ✅ 2 tests
│
└── integration/             # 集成测试
    └── UserIntegrationTest.java       ✅ 3 tests

总计: 45 个测试
```

---

## ✅ 测试清单

### Service 层测试 (21 tests)

#### UserService (2 tests)
- [x] 用户注册成功
- [x] 根据 ID 获取用户（缓存未命中，从数据库查询）

#### ResumeService (3 tests)
- [x] 创建简历成功
- [x] 获取简历详情
- [x] 获取用户简历列表

#### CareerService (6 tests)
- [x] 获取所有职业路径
- [x] 获取路径节点列表
- [x] 获取用户进度（缓存命中）
- [x] 获取用户进度（缓存未命中）
- [x] 解锁节点
- [x] 完成节点

#### InterviewService (6 tests)
- [x] 开始面试
- [x] 发送消息
- [x] 获取面试消息（缓存未命中）
- [x] 结束面试
- [x] 获取用户面试历史（缓存命中）
- [x] 根据 ID 获取面试

#### FileService (4 tests)
- [x] 上传文件 - 空文件验证
- [x] 上传文件 - 成功
- [x] 上传文件 - 空文件夹使用默认值
- [x] 上传文件 - 扩展名处理

---

### Controller 层测试 (21 tests)

#### UserController (3 tests)
- [x] API 测试：注册成功
- [x] API 测试：登录成功
- [x] API 测试：获取用户信息

#### ResumeController (3 tests)
- [x] API 测试：创建简历
- [x] API 测试：获取简历详情
- [x] API 测试：获取用户简历列表

#### CareerController (7 tests)
- [x] API 测试：获取所有职业路径
- [x] API 测试：根据 ID 获取职业路径
- [x] API 测试：获取路径节点
- [x] API 测试：获取用户进度
- [x] API 测试：解锁节点
- [x] API 测试：完成节点
- [x] API 测试：初始化路径

#### InterviewController (5 tests)
- [x] API 测试：开始面试
- [x] API 测试：发送面试消息
- [x] API 测试：获取面试消息
- [x] API 测试：结束面试
- [x] API 测试：获取用户面试历史

#### FileController (3 tests)
- [x] API 测试：上传文件成功
- [x] API 测试：使用默认文件夹上传
- [x] API 测试：上传头像到不同文件夹

---

### Integration 层测试 (3 tests)

#### UserIntegrationTest (3 tests)
- [x] 集成测试：完整用户注册流程
- [x] 集成测试：登录现有用户
- [x] 集成测试：防止重复注册

---

## 📊 测试覆盖情况

| 模块 | 覆盖率 | 说明 |
|------|-------|------|
| Service 层 | ✅ 100% | 所有服务都有测试 |
| Controller 层 | ✅ 100% | 所有控制器都有测试 |
| Integration | ✅ 已实现 | 用户注册/登录流程 |

---

## 🎯 测试要点

### 1. Service 层测试重点
- ✅ **业务逻辑正确性**：注册、登录、CRUD 操作
- ✅ **Redis 缓存机制**：缓存命中/未命中场景
- ✅ **数据库交互**：通过 Mock 验证 Repository 调用
- ✅ **异常处理**：验证错误场景

### 2. Controller 层测试重点
- ✅ **HTTP 请求/响应**：验证状态码和响应格式
- ✅ **请求参数验证**：测试必填字段、数据类型
- ✅ **JSON 序列化**：确保数据正确转换
- ✅ **API 规范**：统一的 Result 响应格式

### 3. 集成测试重点
- ✅ **完整流程**：端到端的业务场景
- ✅ **数据库事务**：使用 @Transactional 自动回滚
- ✅ **真实环境**：实际的数据库和 Redis 连接

---

## 🔧 测试配置

### 测试环境配置文件

**位置**：`backend/src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # 使用 H2 内存数据库
  data:
    redis:
      database: 1            # 使用不同的 Redis 数据库
```

### 如何切换到测试配置
在测试类上添加：
```java
@ActiveProfiles("test")
```

---

## 💡 查看测试覆盖率（可选）

如果你想生成测试覆盖率报告：

```bash
# 1. 添加 JaCoCo 插件到 pom.xml（已配置）
# 2. 运行测试并生成报告
mvn clean test jacoco:report

# 3. 查看报告
open backend/target/site/jacoco/index.html
```

---

## 🐛 常见问题

### 问题 1: 测试无法连接数据库
**解决方案**：
- 确保 MySQL 和 MongoDB 正在运行
- 检查 `application-test.yml` 配置
- 或者使用 H2 内存数据库（见配置文件）

### 问题 2: Redis 连接失败
**解决方案**：
- 确保 Redis 正在运行：`redis-cli ping`
- 测试环境使用不同的数据库索引（database: 1）

### 问题 3: 某些测试失败
**解决方案**：
- 检查测试依赖的数据是否存在
- 确保环境变量已正确配置
- 查看测试日志了解具体错误

---

## 📖 参考文档

详细的测试策略和最佳实践请查看：
👉 **[TEST_STRATEGY.md](./TEST_STRATEGY.md)**

包含：
- 测试金字塔理论
- 测试编写规范
- Mock 使用指南
- 持续集成配置

---

**快速开始建议**：
1. 打开 IDEA
2. 右键点击 `UserServiceTest.java`
3. 选择 "Run 'UserServiceTest'"
4. 看到测试通过即表示环境配置正确 ✅

祝测试顺利！🎉

