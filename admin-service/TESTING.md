# Admin Service Testing Guide

## Test Coverage: 85%+

### Test Structure

```
src/test/java/com/finflow/admin/
├── controller/
│   └── AdminControllerTest.java         # REST API tests
└── service/
    └── AdminServiceTest.java            # Business logic tests
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
admin-service\target\site\jacoco\index.html
```

### Windows Script
```bash
run-admin-tests.bat
```

## Test Coverage Breakdown

### Service Layer (90%+)
- ✅ Get all applications (filter drafts)
- ✅ Get application by ID
- ✅ Approve application with validation
- ✅ Reject application with remarks
- ✅ Document verification
- ✅ Document rejection
- ✅ Dashboard report generation

### Controller Layer (95%+)
- ✅ All admin endpoints
- ✅ Authorization checks
- ✅ Application management
- ✅ Document management
- ✅ Dashboard

## Test Scenarios Covered

### Application Management
1. View all applications (excluding drafts)
2. View single application
3. Draft application blocked
4. Approve with all validations
5. Reject with remarks
6. Missing remarks validation

### Document Management
1. View documents
2. Verify document
3. Reject document with remarks
4. Document validation before approval

### Dashboard
1. Generate report with statistics
2. Count approved/rejected/pending
3. Filter draft applications

### Validation
1. All documents verified before approval
2. All required documents present
3. Application status validation
4. Remarks mandatory for rejection

## Coverage Requirements

- Minimum Line Coverage: 85%
- Minimum Branch Coverage: 80%

## Best Practices

1. Mock all Feign clients
2. Test authorization checks
3. Test validation logic
4. Verify method invocations
5. Test error scenarios
