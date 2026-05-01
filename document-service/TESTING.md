# Document Service Testing Guide

## Test Coverage: 85%+

### Test Structure

```
src/test/java/com/finflow/document/
├── controller/
│   └── DocumentControllerTest.java      # REST API tests
├── service/
│   └── DocumentServiceTest.java         # Business logic tests
├── security/
│   └── JwtUtilTest.java                 # JWT utility tests
└── model/
    └── DocumentTest.java                # Model tests
```

## Running Tests

### Run all tests
```bash
mvn test
```

### Run with coverage
```bash
mvn test jacoco:report
```

### View coverage report
```bash
# Open in browser
document-service\target\site\jacoco\index.html
```

### Windows Script
```bash
run-document-tests.bat
```

## Test Coverage Breakdown

### Service Layer (90%+)
- ✅ Document upload
- ✅ Document replacement
- ✅ Document verification
- ✅ Document rejection
- ✅ Document validation
- ✅ Authorization checks

### Controller Layer (95%+)
- ✅ All upload endpoints (6 loan types)
- ✅ Replace document
- ✅ View/Download document
- ✅ Validate documents
- ✅ Admin verify/reject

### Security Layer (100%)
- ✅ JWT token operations

### Model Layer (100%)
- ✅ Document entity

## Test Scenarios Covered

### Document Upload
1. Valid document upload
2. Unauthorized user
3. Empty file
4. Duplicate document
5. Admin blocked from upload

### Document Management
1. Replace document
2. Document not found
3. Ownership validation
4. Get documents by application
5. Get document by ID

### Document Verification
1. Verify valid document
2. Reject document with remarks
3. Draft application validation
4. Document completeness check

### Security
1. JWT token generation
2. Extract email/role
3. Token validation

## Coverage Requirements

- Minimum Line Coverage: 85%
- Minimum Branch Coverage: 80%

## Best Practices

1. Use @TempDir for file operations
2. Mock external dependencies (Feign clients)
3. Test both success and failure scenarios
4. Verify method invocations
5. Clean up test data

## Troubleshooting

### Tests fail with file errors
- @TempDir automatically creates temp directories
- Files are cleaned up after tests

### Feign client errors
- All external clients are mocked
- Check mock configurations
