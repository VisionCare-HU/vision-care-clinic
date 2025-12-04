# Testing Guide - VisionCare Clinic

This guide explains how to run tests and generate test reports for the backend.

## Backend Testing (Java/Spring Boot)

### Prerequisites
- Java 17
- Maven 3.6+

### Running Tests

Navigate to the backend directory:
```bash
cd vision-care-clinic/vision_care_clinic/backend
```

Run all tests:
```bash
mvn test
```

### Test Reports

After running tests, reports are automatically generated in:
- **XML Reports**: `target/surefire-reports/` - Contains individual test class XML reports
- **HTML Reports**: `target/site/surefire-report.html` - Generated after running `mvn surefire-report:report`

To view the reports:
1. **XML Reports**: Navigate to `target/surefire-reports/` and open any `TEST-*.xml` file for detailed test results
2. **HTML Report**: 
   - Run `mvn surefire-report:report` after running tests
   - Open `target/site/surefire-report.html` in your browser for a formatted HTML report

### Test Coverage

The following test files have been created:

**Controller Tests:**
- `AuthControllerTest.java` - Tests for registration and login endpoints
- `AppointmentControllerTest.java` - Tests for appointment booking and retrieval
- `UserControllerTest.java` - Tests for user profile endpoints

**Service Tests:**
- `AuthServiceTest.java` - Tests for authentication service logic
- `AppointmentServiceTest.java` - Tests for appointment service logic
- `JwtServiceTest.java` - Tests for JWT token generation and validation

## Test Report Locations

### Backend
- **XML Reports**: `vision-care-clinic/vision_care_clinic/backend/target/surefire-reports/`
  - Individual test class reports: `TEST-*.xml`
  - Summary report: `surefire-reports.html` (if generated)
- **HTML Report**: `vision-care-clinic/vision_care_clinic/backend/target/site/surefire-report.html`
  - Generate with: `mvn surefire-report:report`
  - Open in browser for formatted view

## Continuous Integration

For CI/CD pipelines:
```bash
cd vision-care-clinic/vision_care_clinic/backend
mvn clean test surefire-report:report
```

## Notes

- Backend tests use JUnit 5 and Mockito for mocking
- All tests are configured to run automatically when you execute the test commands
- Test reports are generated automatically after test execution

