# Resume Backend API

A Spring Boot backend application for managing resume/CV data with a REST API. Built with TDD/BDD practices using JUnit, Mockito, and Gauge.

## 🚀 Features

- **RESTful API** for managing resume profiles
- **Domain Models**: Profile, Experience, Education, Skills, Certifications
- **PostgreSQL** database with JPA/Hibernate
- **Liquibase** for database migrations
- **Test-Driven Development** with comprehensive test coverage
- **BDD with Gauge** for functional testing
- **OpenAPI/Swagger** documentation
- **JWT Authentication** ready
- **Docker** support for PostgreSQL

## 📋 Prerequisites

- Java 17+
- Gradle 8+
- PostgreSQL 14+ (or use Docker)
- Node.js (for Gauge installation)

## 🛠️ Technology Stack

- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Liquibase**
- **Lombok**
- **MapStruct**
- **JUnit 5**
- **Mockito**
- **Gauge** for BDD
- **TestContainers**
- **RestAssured**

## 🏗️ Project Structure

```
resume-backend/
├── src/
│   ├── main/
│   │   ├── java/com/deryncullen/resume/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── model/          # JPA entities
│   │   │   ├── dto/            # Data transfer objects
│   │   │   ├── config/         # Configuration classes
│   │   │   └── exception/      # Custom exceptions
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/   # Liquibase migrations
│   └── test/
│       ├── java/               # Unit and integration tests
│       └── resources/          # Test configurations
├── specs/                      # Gauge BDD specifications
├── build.gradle               # Gradle build configuration
└── README.md
```

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd resume-backend
```

### 2. Set up PostgreSQL

#### Option A: Using Docker (Recommended)

```bash
# Start PostgreSQL in Docker
docker run --name resume-postgres \
  -e POSTGRES_DB=resume_db \
  -e POSTGRES_USER=resume_user \
  -e POSTGRES_PASSWORD=resume_password \
  -p 5432:5432 \
  -d postgres:14-alpine

# Verify it's running
docker ps
```

#### Option B: Local PostgreSQL

Create database and user:

```sql
CREATE DATABASE resume_db;
CREATE USER resume_user WITH PASSWORD 'resume_password';
GRANT ALL PRIVILEGES ON DATABASE resume_db TO resume_user;
```

### 3. Configure Application

Update `src/main/resources/application.properties` if needed:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/resume_db
spring.datasource.username=resume_user
spring.datasource.password=resume_password
```

### 4. Build the Project

```bash
# Build without tests
./gradlew build -x test

# Or with tests
./gradlew build
```

### 5. Run Tests

```bash
# Run all tests
./gradlew test

# Run unit tests only
./gradlew unitTest

# Run integration tests only
./gradlew integrationTest

# Run Gauge BDD tests
./gradlew gauge
```

### 6. Run the Application

```bash
# Using Gradle
./gradlew bootRun

# Or using the built JAR
java -jar build/libs/resume-backend-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080/api`

## 📚 API Documentation

Once the application is running, access:
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/api/api-docs`

## 🧪 Testing Strategy

### Unit Tests
- Model validation tests
- Service layer business logic tests
- Repository custom query tests
- Uses Mockito for mocking dependencies

### Integration Tests
- Full API endpoint testing
- Database integration with H2 in-memory database
- Uses TestContainers for PostgreSQL testing

### BDD Tests (Gauge)
- End-to-end API scenarios
- Business-readable specifications
- Located in `/specs` directory

Run specific test suites:

```bash
# Unit tests only
./gradlew test --tests "*Test"

# Integration tests
./gradlew test --tests "*IT"

# Specific test class
./gradlew test --tests "ProfileServiceTest"
```

## 📊 Database Schema

### Profile Table
- Personal information (name, email, phone, location)
- Professional title and summary
- Social links (LinkedIn, GitHub, website)

### Experience Table
- Company details
- Job title and duration
- Achievements and technologies used

### Education Table
- Institution and degree information
- Dates and grades

### Skills Table
- Skill categories (Programming, Framework, etc.)
- Proficiency levels
- Years of experience

### Certifications Table
- Certification details
- Issuing organization
- Expiration tracking

## 🔧 Development Workflow

1. **Write Tests First (TDD)**
   - Create unit tests for new features
   - Write Gauge specifications for API endpoints

2. **Implement Features**
   - Create/modify entities
   - Implement service logic
   - Add controllers

3. **Run Tests**
   ```bash
   ./gradlew test
   ```

4. **Check Code Coverage**
   ```bash
   ./gradlew jacocoTestReport
   # Report available at: build/reports/jacoco/test/html/index.html
   ```

## 🐳 Docker Deployment

### Build Docker Image

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:

```bash
# Build the application
./gradlew build

# Build Docker image
docker build -t resume-backend:latest .

# Run with Docker Compose (create docker-compose.yml first)
docker-compose up
```

## 🔒 Security

- Basic authentication configured (update for production)
- JWT token support included in dependencies
- CORS configuration needed for frontend integration

## 📝 Environment Variables

For production, use environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/resume_db
export DB_USERNAME=resume_user
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret
```

## 🚧 Next Steps

To complete the backend:

1. **Implement remaining services and controllers**
2. **Add DTOs and mappers (MapStruct)**
3. **Create Liquibase migrations**
4. **Implement JWT authentication**
5. **Add validation and error handling**
6. **Implement file upload for resume PDFs**
7. **Add caching with Redis**
8. **Set up CI/CD pipeline**

## 📄 License

[Your License]

## 👥 Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests first (TDD)
4. Implement features
5. Ensure all tests pass
6. Submit a pull request

## 📧 Contact

Deryn Cullen - derynleigh.cullen@icloud.com
