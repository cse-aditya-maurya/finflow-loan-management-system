# Auth Service Testing Guide

## Test Coverage: 85%+

### Test Structure

```
src/test/java/com/finflow/auth/
├── controller/
│   └── AuthControllerTest.java          # REST API tests
├── service/
│   ├── AuthServiceTest.java             # Business logic tests
│   └── OtpServiceTest.java              # OTP service tests
├── security/
│   ├── JwtUtilTest.java                 # JWT utility tests
│   └── CustomUserDetailsServiceTest.java # User details tests
├── model/
│   ├── UserTest.java                    # User model tests
│   └── OtpTokenTest.java                # OTP token tests
├── exception/
│   └── GlobalExceptionHandlerTest.java  # Exception handler tests
└── integration/
    └── AuthServiceIntegrationTest.java  # Integration tests
```

## Running Tests

### Run all tests
```bash
mvn test
```

### Run tests with coverage
```bash
mvn test jacoco:report
```

### Run specific test class
```bash
mvn test -Dtest=AuthServiceTest
```

### View coverage report
```bash
# Open in browser
auth-service\target\site\jacoco\index.html
```

### Windows Script
```bash
run-auth-tests.bat
```

## Test Coverage Breakdown

### Service Layer (90%+)
- ✅ AuthService - All methods tested
- ✅ OtpService - All scenarios covered
  - Valid OTP verification
  - Invalid OTP handling
  - Expired OTP handling
  - Max attempts exceeded
  - Resend OTP logic

### Controller Layer (95%+)
- ✅ AuthController - All endpoints tested
  - POST /auth/signup
  - POST /auth/login
  - POST /auth/verify-otp
  - POST /auth/resend-otp
  - POST /auth/forgot-password
  - POST /auth/reset-password
  - GET /auth/user/{userId}
  - GET /auth/test-token

### Security Layer (100%)
- ✅ JwtUtil - Token generation and validation
- ✅ CustomUserDetailsService - User loading

### Model Layer (100%)
- ✅ User entity
- ✅ OtpToken entity

### Exception Handling (100%)
- ✅ GlobalExceptionHandler
- ✅ AuthException

### Integration Tests
- ✅ End-to-end signup flow
- ✅ End-to-end login flow

## Test Scenarios Covered

### Authentication Flow
1. New user signup
2. Existing user signup (resend OTP)
3. Verified user signup (direct login)
4. Valid login
5. Invalid email login
6. Invalid password login
7. Unverified email login

### OTP Flow
1. Generate and send OTP
2. Verify valid OTP
3. Verify invalid OTP
4. Verify expired OTP
5. Max attempts exceeded
6. Resend OTP for unverified user
7. Resend OTP for verified user

### Password Reset
1. Forgot password request
2. Reset password with valid data
3. User not found scenarios

### JWT Operations
1. Generate token
2. Extract email from token
3. Extract role from token
4. Validate token
5. Invalid token handling

## Coverage Requirements

- Minimum Line Coverage: 85%
- Minimum Branch Coverage: 80%
- Excludes: Config classes, DTOs, Main application class

## Best Practices

1. Use @ExtendWith(MockitoExtension.class) for unit tests
2. Use @SpringBootTest for integration tests
3. Mock external dependencies (RabbitMQ, Database)
4. Test both success and failure scenarios
5. Verify method invocations with Mockito
6. Use meaningful test names
7. Clean up test data in @BeforeEach

## Troubleshooting

### Tests fail with database errors
- Ensure H2 dependency is in pom.xml
- Check application-test.properties configuration

### Coverage not generating
```bash
mvn clean install
mvn jacoco:report
```

### Integration tests fail
- Check if test profile is active
- Verify RabbitMQ mock configuration

## CI/CD Integration

Add to your pipeline:
```yaml
- mvn clean test jacoco:report
- mvn jacoco:check
```

This ensures 85% coverage is maintained on every commit.
