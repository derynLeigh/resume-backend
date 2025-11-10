# Resume Backend - Complete Implementation Guide

## 🎯 What You're Getting

A complete, production-ready implementation of:
1. ✅ Service & Controller layers for all entities (Experience, Education, Skills, Certifications)
2. ✅ JWT Authentication system
3. ✅ File Upload capability  
4. ✅ PDF Export functionality
5. ✅ Deployment configurations
6. ✅ Comprehensive testing
7. ✅ API documentation

## 📦 Package Structure

I've created all the files you need organized by layer. Here's what to do:

### Step 1: Copy Files to Your Project

```bash
# Navigate to your project
cd /Users/deryncullen/dev/PoCs/resume-backend

# Create backup
git add .
git commit -m "Backup before adding new features"

# Download the implementation files
# (Files are in /mnt/user-data/outputs/)
```

### Step 2: Update build.gradle

Add these new dependencies:

```gradle
dependencies {
    // ... existing dependencies ...
    
    // PDF Generation
    implementation 'com.itextpdf:itext7-core:7.2.5'
    implementation 'com.itextpdf:html2pdf:4.0.5'
    
    // Method Security
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT already included in your dependencies ✓
}
```

### Step 3: Update application.properties

Add these configurations:

```properties
# Existing configurations remain...

# JWT Configuration
jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload.dir=./uploads

# PDF Export Configuration
pdf.template.path=classpath:templates/resume-template.html
```

### Step 4: Create Database Migrations

Create new Liquibase changeset: `src/main/resources/db/changelog/002-add-user-and-file-tables.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <!-- Users table -->
    <changeSet id="002-create-users-table" author="deryn.cullen">
        <createTable tableName="users">
            <column name="id" type="BIGSERIAL">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="email" type="VARCHAR(255)">
                <constraints nullable="false" unique="true"/>
            </column>
            <column name="password" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="first_name" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="last_name" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="enabled" type="BOOLEAN" defaultValueBoolean="true">
                <constraints nullable="false"/>
            </column>
            <column name="account_non_expired" type="BOOLEAN" defaultValueBoolean="true">
                <constraints nullable="false"/>
            </column>
            <column name="account_non_locked" type="BOOLEAN" defaultValueBoolean="true">
                <constraints nullable="false"/>
            </column>
            <column name="credentials_non_expired" type="BOOLEAN" defaultValueBoolean="true">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="last_login" type="TIMESTAMP"/>
            <column name="version" type="BIGINT" defaultValueNumeric="0"/>
        </createTable>
        
        <createIndex tableName="users" indexName="idx_users_email">
            <column name="email"/>
        </createIndex>
    </changeSet>

    <!-- User roles table -->
    <changeSet id="003-create-user-roles-table" author="deryn.cullen">
        <createTable tableName="user_roles">
            <column name="user_id" type="BIGINT">
                <constraints nullable="false" foreignKeyName="fk_user_roles_user" 
                           references="users(id)" deleteCascade="true"/>
            </column>
            <column name="role" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
        </createTable>
        
        <addPrimaryKey tableName="user_roles" columnNames="user_id,role"/>
    </changeSet>

    <!-- File metadata table -->
    <changeSet id="004-create-file-metadata-table" author="deryn.cullen">
        <createTable tableName="file_metadata">
            <column name="id" type="BIGSERIAL">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="profile_id" type="BIGINT">
                <constraints nullable="false" foreignKeyName="fk_file_profile" 
                           references="profiles(id)" deleteCascade="true"/>
            </column>
            <column name="file_name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="original_name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="file_type" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="file_size" type="BIGINT">
                <constraints nullable="false"/>
            </column>
            <column name="content_type" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="storage_path" type="VARCHAR(500)">
                <constraints nullable="false"/>
            </column>
            <column name="uploaded_by" type="BIGINT">
                <constraints nullable="false" foreignKeyName="fk_file_uploader" 
                           references="users(id)"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="version" type="BIGINT" defaultValueNumeric="0"/>
        </createTable>
        
        <createIndex tableName="file_metadata" indexName="idx_file_profile">
            <column name="profile_id"/>
        </createIndex>
        
        <createIndex tableName="file_metadata" indexName="idx_file_type">
            <column name="file_type"/>
        </createIndex>
    </changeSet>

</databaseChangeLog>
```

Update `db.changelog-master.xml`:
```xml
<include file="db/changelog/002-add-user-and-file-tables.xml"/>
```

## 🚀 Quick Start Testing

### 1. Start PostgreSQL
```bash
docker-compose up -d postgres
```

### 2. Run migrations
```bash
./gradlew update
```

### 3. Start application
```bash
./gradlew bootRun
```

### 4. Register first user (ADMIN)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@resume.com",
    "password": "SecurePass123!",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

### 5. Login and get JWT
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@resume.com",
    "password": "SecurePass123!"
  }'

# Save the "accessToken" from the response
```

### 6. Test protected endpoint
```bash
# Replace YOUR_JWT_TOKEN with the actual token
curl -X POST http://localhost:8080/api/profiles/1/experiences \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "companyName": "Sky",
    "jobTitle": "Technical Product Owner",
    "startDate": "2025-01-01",
    "current": true,
    "description": "Leading digital identity platform",
    "achievements": [
      "Led team of 5 engineers",
      "Improved velocity by 30%"
    ],
    "technologies": ["Java", "Spring Boot", "React", "PostgreSQL"]
  }'
```

### 7. Test PDF export
```bash
curl -X GET "http://localhost:8080/api/profiles/1/export/pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output resume.pdf

# Open the PDF
open resume.pdf  # macOS
# or
xdg-open resume.pdf  # Linux
```

### 8. Test file upload
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/resume.pdf" \
  -F "profileId=1" \
  -F "fileType=RESUME"
```

## 📝 API Endpoints

### Authentication (Public)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT
- `POST /api/auth/refresh` - Refresh JWT token

### Profiles (existing + updated security)
- `GET /api/profiles` - List all profiles (public)
- `POST /api/profiles` - Create profile (requires ADMIN)
- `GET /api/profiles/{id}` - Get profile (public)
- `PUT /api/profiles/{id}` - Update profile (requires ADMIN)
- `DELETE /api/profiles/{id}` - Delete profile (requires ADMIN)
- `GET /api/profiles/{id}/full` - Get profile with all relations (public)

### Experiences (NEW - requires AUTH for write operations)
- `GET /api/profiles/{id}/experiences` - List experiences
- `POST /api/profiles/{id}/experiences` - Create experience (ADMIN)
- `GET /api/profiles/{id}/experiences/{expId}` - Get experience
- `PUT /api/profiles/{id}/experiences/{expId}` - Update experience (ADMIN)
- `DELETE /api/profiles/{id}/experiences/{expId}` - Delete experience (ADMIN)
- `PUT /api/profiles/{id}/experiences/reorder` - Reorder experiences (ADMIN)
- `GET /api/profiles/{id}/experiences/current` - Get current experiences

### Education (NEW - requires AUTH for write operations)
- `GET /api/profiles/{id}/educations` - List education
- `POST /api/profiles/{id}/educations` - Create education (ADMIN)
- `GET /api/profiles/{id}/educations/{eduId}` - Get education
- `PUT /api/profiles/{id}/educations/{eduId}` - Update education (ADMIN)
- `DELETE /api/profiles/{id}/educations/{eduId}` - Delete education (ADMIN)
- `PUT /api/profiles/{id}/educations/reorder` - Reorder education (ADMIN)

### Skills (NEW - requires AUTH for write operations)
- `GET /api/profiles/{id}/skills` - List skills
- `POST /api/profiles/{id}/skills` - Create skill (ADMIN)
- `GET /api/profiles/{id}/skills/{skillId}` - Get skill
- `PUT /api/profiles/{id}/skills/{skillId}` - Update skill (ADMIN)
- `DELETE /api/profiles/{id}/skills/{skillId}` - Delete skill (ADMIN)
- `GET /api/profiles/{id}/skills/category/{category}` - Filter by category
- `PUT /api/profiles/{id}/skills/{skillId}/primary` - Toggle primary skill (ADMIN)

### Certifications (NEW - requires AUTH for write operations)
- `GET /api/profiles/{id}/certifications` - List certifications
- `POST /api/profiles/{id}/certifications` - Create certification (ADMIN)
- `GET /api/profiles/{id}/certifications/{certId}` - Get certification
- `PUT /api/profiles/{id}/certifications/{certId}` - Update certification (ADMIN)
- `DELETE /api/profiles/{id}/certifications/{certId}` - Delete certification (ADMIN)
- `GET /api/profiles/{id}/certifications/expiring` - Get expiring certs

### Files (NEW - requires AUTH)
- `POST /api/files/upload` - Upload file (AUTH)
- `GET /api/files/{id}` - Download file (public)
- `DELETE /api/files/{id}` - Delete file (ADMIN)
- `GET /api/profiles/{id}/files` - List profile files (public)

### PDF Export (NEW)
- `GET /api/profiles/{id}/export/pdf` - Export resume as PDF (public)

## 🧪 Running Tests

```bash
# All tests
./gradlew clean test gaugeTest

# Unit tests only
./gradlew test

# Integration tests
./gradlew integrationTest

# BDD tests
./gradlew gaugeTest

# Specific test class
./gradlew test --tests "ExperienceServiceTest"

# With coverage
./gradlew test jacocoTestReport
```

## 🔒 Security Features

1. **JWT Authentication**: Stateless authentication with refresh tokens
2. **Role-Based Access Control**: USER and ADMIN roles
3. **Password Encryption**: BCrypt hashing
4. **CORS Configuration**: Configured for Next.js frontend
5. **Method Security**: `@PreAuthorize` annotations on sensitive endpoints
6. **Input Validation**: Bean validation on all DTOs
7. **SQL Injection Prevention**: JPA with parameterized queries
8. **XSS Protection**: Spring Security defaults

## 📊 File Structure

```
src/main/java/com/deryncullen/resume/
├── config/
│   ├── SecurityConfig.java ← UPDATED with JWT
│   ├── JwtAuthenticationFilter.java ← NEW
│   ├── OpenApiConfig.java ← UPDATED
│   └── CorsConfig.java ← NEW
├── controller/
│   ├── ProfileController.java (existing, updated security)
│   ├── ExperienceController.java ← NEW
│   ├── EducationController.java ← NEW
│   ├── SkillController.java ← NEW
│   ├── CertificationController.java ← NEW
│   ├── AuthenticationController.java ← NEW
│   ├── FileController.java ← NEW
│   └── PdfExportController.java ← NEW
├── dto/
│   ├── ... (existing DTOs)
│   ├── AuthenticationRequest.java ← NEW
│   ├── AuthenticationResponse.java ← NEW
│   └── RegisterRequest.java ← NEW
├── model/
│   ├── ... (existing models)
│   ├── User.java ← NEW
│   ├── Role.java ← NEW
│   └── FileMetadata.java ← NEW
├── repository/
│   ├── ProfileRepository.java (existing)
│   ├── ExperienceRepository.java ← NEW
│   ├── EducationRepository.java ← NEW
│   ├── SkillRepository.java ← NEW
│   ├── CertificationRepository.java ← NEW
│   ├── UserRepository.java ← NEW
│   └── FileMetadataRepository.java ← NEW
├── security/
│   ├── JwtService.java ← NEW
│   ├── JwtAuthenticationFilter.java ← NEW
│   └── AuthenticationService.java ← NEW
├── service/
│   ├── ProfileService.java (existing)
│   ├── ExperienceService.java ← NEW
│   ├── EducationService.java ← NEW
│   ├── SkillService.java ← NEW
│   ├── CertificationService.java ← NEW
│   ├── FileStorageService.java ← NEW
│   └── PdfExportService.java ← NEW
└── exception/
    └── ... (existing exceptions)
```

## 🚀 Deployment

### Railway (Recommended)
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Initialize project
railway init

# Add PostgreSQL
railway add

# Deploy
railway up

# Set environment variables
railway variables set JWT_SECRET="your-production-secret-here"
railway variables set SPRING_PROFILES_ACTIVE="production"
```

### Render
```bash
# Push to GitHub
git push origin main

# In Render dashboard:
# 1. New > Web Service
# 2. Connect your GitHub repo
# 3. Use these settings:
#    - Build Command: ./gradlew build -x test
#    - Start Command: java -jar build/libs/resume-backend-0.0.1-SNAPSHOT.jar
# 4. Add environment variables
```

### Docker
```bash
# Build
docker build -f Dockerfile.production -t resume-backend:latest .

# Run
docker-compose -f docker-compose.production.yml up -d
```

## 📈 Performance Tips

1. **Database Indexing**: All foreign keys indexed
2. **Connection Pooling**: HikariCP configured
3. **Lazy Loading**: Relationships loaded on-demand
4. **Caching**: Add Redis for high traffic
5. **Compression**: Enable Gzip in production
6. **CDN**: Use CloudFront/CloudFlare for static files

## 🔍 Monitoring

Add these actuator endpoints to `application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

Access:
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

## 🐛 Troubleshooting

### JWT Secret Error
```
Error: JWT secret must be at least 256 bits
Solution: Update jwt.secret in application.properties
```

### File Upload Error
```
Error: Maximum upload size exceeded
Solution: Increase spring.servlet.multipart.max-file-size
```

### Database Migration Error
```
Error: Liquibase changeset failed
Solution: Check PostgreSQL is running and accessible
./gradlew clean update
```

### Port Already in Use
```
Error: Port 8080 is already in use
Solution: Change server.port in application.properties
```

## ✅ Success Checklist

Before going to production:

- [ ] All tests passing
- [ ] JWT secret changed from default
- [ ] Database credentials secured
- [ ] CORS configured for your domain
- [ ] File upload directory configured
- [ ] PDF templates customized
- [ ] Environment variables set
- [ ] Database backed up
- [ ] Monitoring configured
- [ ] Logs aggregation setup
- [ ] SSL/TLS certificate installed
- [ ] Rate limiting configured
- [ ] Error tracking setup (Sentry)

## 📚 Additional Resources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/) - JWT debugger
- [iText PDF Documentation](https://itextpdf.com/en/resources/documentation)
- [Railway Documentation](https://docs.railway.app/)
- [Render Documentation](https://render.com/docs)

## 🎓 What's Next?

1. **Frontend Integration**: Connect your Next.js frontend
2. **Email Notifications**: Add email service for alerts
3. **Analytics**: Add usage tracking
4. **Rate Limiting**: Add Redis-based rate limiting
5. **Search**: Add Elasticsearch for resume search
6. **Webhooks**: Add webhook support for events
7. **Audit Logs**: Track all changes
8. **Multi-tenancy**: Support multiple users

## 💪 You're Ready!

You now have a production-ready resume backend with:
- Complete CRUD for all entities
- JWT authentication
- File upload
- PDF export
- Comprehensive testing
- Deployment configurations

Time to build that amazing Next.js frontend! 🚀
