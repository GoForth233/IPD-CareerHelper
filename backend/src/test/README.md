# Backend Testing Suite

## 📊 Overview

This directory contains comprehensive automated tests for the Career Platform backend application.

**Total Tests**: 45  
**Coverage**: Service Layer (100%), Controller Layer (100%), Integration Tests (✅)

---

## 📁 Directory Structure

```
test/
├── java/com/group1/career/
│   ├── controller/          # API endpoint tests (21 tests)
│   │   ├── CareerControllerTest.java
│   │   ├── FileControllerTest.java
│   │   ├── InterviewControllerTest.java
│   │   ├── ResumeControllerTest.java
│   │   └── UserControllerTest.java
│   │
│   ├── service/             # Business logic tests (21 tests)
│   │   ├── CareerServiceTest.java
│   │   ├── FileServiceTest.java
│   │   ├── InterviewServiceTest.java
│   │   ├── ResumeServiceTest.java
│   │   └── UserServiceTest.java
│   │
│   └── integration/         # End-to-end tests (3 tests)
│       └── UserIntegrationTest.java
│
└── resources/
    └── application-test.yml  # Test environment configuration
```

---

## 🚀 Quick Start

### Run All Tests (IntelliJ IDEA)
1. Right-click on `src/test/java` folder
2. Select "Run 'Tests in 'java''"

### Run Specific Test
1. Open test file (e.g., `UserServiceTest.java`)
2. Click the green play button next to the class/method name

### Run via Maven
```bash
cd backend
mvn test                              # Run all tests
mvn test -Dtest=UserServiceTest       # Run specific test class
```

---

## 📚 Documentation

For detailed information, please refer to:

- **[TEST_STRATEGY.md](../../TEST_STRATEGY.md)** - Comprehensive testing strategy
- **[TESTING_GUIDE.md](../../TESTING_GUIDE.md)** - Quick start guide
- **[TEST_SUMMARY.md](../../TEST_SUMMARY.md)** - Completion summary

---

## ✅ Test Coverage

| Module | Tests | Status |
|--------|-------|--------|
| UserService/Controller | 5 | ✅ |
| ResumeService/Controller | 6 | ✅ |
| CareerService/Controller | 13 | ✅ |
| InterviewService/Controller | 11 | ✅ |
| FileService/Controller | 7 | ✅ |
| Integration Tests | 3 | ✅ |
| **Total** | **45** | **✅** |

---

## 🎯 What's Tested

### ✅ Service Layer
- Business logic
- Data validation
- Database interactions
- Redis caching (hit/miss scenarios)
- Error handling

### ✅ Controller Layer
- HTTP endpoints
- Request/response mapping
- JSON serialization
- API response format
- Status codes

### ✅ Integration
- Complete user flows
- Multi-layer interactions
- Database transactions

---

## 🛠 Testing Stack

- **JUnit 5** - Test framework
- **Mockito** - Mocking
- **Spring Test** - Spring Boot testing
- **MockMvc** - HTTP testing

---

## 💡 Example Test

```java
@Test
@DisplayName("Test User Registration - Successfully insert data")
public void testRegister_Success() {
    // Arrange
    when(userRepository.save(any(User.class))).thenReturn(mockUser);
    
    // Act
    User result = userService.register(nickname, type, identifier, credential);
    
    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getUserId());
    verify(userRepository, times(1)).save(any(User.class));
}
```

---

## 🔍 Need Help?

- Check **TESTING_GUIDE.md** for step-by-step instructions
- Review existing tests as examples
- Ensure MySQL, MongoDB, and Redis are running for integration tests

---

**Last Updated**: December 2025

