# Quick Start Checklist - 30 Minute Setup

## ⚡ Super Fast Setup (Get Running in 30 Minutes)

### Step 1: Download Files (2 minutes)
```bash
# Download all files from outputs/ directory
# You should have:
# - complete_implementation/ folder
# - START_HERE.md
# - COMPLETE_BACKEND_IMPLEMENTATION_GUIDE.md
# - All other documentation
```

### Step 2: Copy Files to Your Project (5 minutes)
```bash
cd /Users/deryncullen/dev/PoCs/resume-backend

# Copy model files
cp ~/Downloads/complete_implementation/model/*.java \
   src/main/java/com/deryncullen/resume/model/

# Copy repository files  
cp ~/Downloads/complete_implementation/repository/*.java \
   src/main/java/com/deryncullen/resume/repository/

# Copy service files
cp ~/Downloads/complete_implementation/service/*.java \
   src/main/java/com/deryncullen/resume/service/

# Copy controller files
cp ~/Downloads/complete_implementation/controller/*.java \
   src/main/java/com/deryncullen/resume/controller/

# Copy security files
cp ~/Downloads/complete_implementation/security/*.java \
   src/main/java/com/deryncullen/resume/security/

# Copy DTO files (you'll need to split the combined file)
# Or just reference when creating individual DTOs
```

### Step 3: Update build.gradle (2 minutes)
Add to dependencies section:
```gradle
// PDF Generation
implementation 'com.itextpdf:itext7-core:7.2.5'
implementation 'com.itextpdf:html2pdf:4.0.5'

// Method Security (if not already there)
implementation 'org.springframework.boot:spring-boot-starter-security'
```

### Step 4: Update application.properties (2 minutes)
Add at the end:
```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# File Upload
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload.dir=./uploads

# PDF Export
pdf.template.path=classpath:templates/resume-template.html
```

### Step 5: Create Database Migration (3 minutes)
Create: `src/main/resources/db/changelog/002-add-user-and-file-tables.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

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
    </changeSet>

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
</databaseChangeLog>
```

Update `db.changelog-master.xml`:
```xml
<include file="db/changelog/002-add-user-and-file-tables.xml"/>
```

### Step 6: Create Missing Files (10 minutes)

You still need to create these files. Use the templates in FILE_MANIFEST.md:

**Critical:**
1. `UserRepository.java` - Simple interface
2. `AuthenticationService.java` - Login/register logic
3. `AuthenticationController.java` - Auth endpoints
4. `SecurityConfig.java` - Update with JWT
5. `UserDetailsServiceImpl.java` - Load user by email

**Nice to have:**
6. `EducationController.java` - Copy from ExperienceController pattern
7. `SkillService.java` + `SkillController.java` - Follow pattern
8. `CertificationService.java` + `CertificationController.java` - Follow pattern

### Step 7: Compile and Run Migrations (2 minutes)
```bash
# Stop any running instances
# Ctrl+C if bootRun is running

# Clean and compile
./gradlew clean compileJava

# Run migrations
./gradlew update

# Should see: "Successfully released change log lock"
```

### Step 8: Start Application (1 minute)
```bash
./gradlew bootRun

# Wait for: "Started ResumeBackendApplication"
```

### Step 9: Test Endpoints (3 minutes)
```bash
# Test health check
curl http://localhost:8080/api/actuator/health

# Should return: {"status":"UP"}

# Once you implement AuthenticationController:
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!"
  }'

# Test protected endpoint (with token from login)
curl -X POST http://localhost:8080/api/profiles/1/experiences \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "companyName": "Test Company",
    "jobTitle": "Developer",
    "startDate": "2025-01-01",
    "current": true
  }'
```

## ✅ Checklist

### Before You Start
- [ ] PostgreSQL running (`docker-compose up -d postgres`)
- [ ] All files downloaded from outputs/
- [ ] Backup created (`git commit -m "Backup"`)

### Installation Steps
- [ ] Files copied to project
- [ ] build.gradle updated
- [ ] application.properties updated
- [ ] Database migration created
- [ ] Master changelog updated
- [ ] Missing files created (at minimum: UserRepository, AuthenticationService, AuthenticationController, SecurityConfig)

### Testing
- [ ] Application compiles (`./gradlew compileJava`)
- [ ] Migrations run (`./gradlew update`)
- [ ] Application starts (`./gradlew bootRun`)
- [ ] Health check works
- [ ] Can register user
- [ ] Can login and get JWT
- [ ] Can access protected endpoint with JWT
- [ ] All tests pass (`./gradlew test`)

### Next Steps
- [ ] Implement remaining controllers (Education, Skills, Certifications)
- [ ] Add file upload (optional)
- [ ] Add PDF export (optional)
- [ ] Update Gauge specs for new endpoints
- [ ] Deploy to Railway/Render
- [ ] Connect Next.js frontend

## 🆘 Troubleshooting

### Compilation Errors
```bash
# If you see import errors:
# - Check all files are in correct packages
# - Run: ./gradlew --refresh-dependencies
# - Run: ./gradlew clean compileJava
```

### Migration Errors
```bash
# If Liquibase fails:
# - Check PostgreSQL is running
# - Check db.changelog-master.xml includes new file
# - Run: ./gradlew clean update
```

### Application Won't Start
```bash
# Check for:
# - Port 8080 already in use: lsof -i :8080
# - PostgreSQL not accessible: psql -h localhost -U resume_user -d resume_db
# - Configuration errors: Check application.properties
```

### JWT Not Working
```bash
# Verify:
# - JWT secret is set in application.properties
# - JwtAuthenticationFilter is registered
# - SecurityConfig has JWT filter chain
# - UserDetailsService is implemented
```

## 🎯 Minimum Viable Setup

To get a working backend with auth, you MUST create these 5 files:

1. **UserRepository.java**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

2. **UserDetailsServiceImpl.java**
```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;
    
    @Override
    public UserDetails loadUserByUsername(String email) 
            throws UsernameNotFoundException {
        return repository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email: " + email));
    }
}
```

3. **AuthenticationService.java** (see IMPLEMENTATION_GUIDE.md for full code)

4. **AuthenticationController.java** (see IMPLEMENTATION_GUIDE.md for full code)

5. **SecurityConfig.java** (update existing with JWT filter chain)

## 🚀 After 30 Minutes

You should have:
- ✅ Backend compiling
- ✅ Database migrated
- ✅ Application running
- ✅ JWT authentication working
- ✅ Experience API functional
- ✅ Ready for frontend integration

## 📚 Full Documentation

- **START_HERE.md** - Overview and paths
- **COMPLETE_BACKEND_IMPLEMENTATION_GUIDE.md** - Detailed setup
- **FILE_MANIFEST.md** - All files with priorities
- **complete_backend_package.md** - Package info

## 💡 Pro Tips

1. **Start with auth**: Get JWT working first, everything else depends on it
2. **Test as you go**: Run curl commands after each file
3. **Use Swagger**: Visit http://localhost:8080/api/swagger-ui.html
4. **Commit often**: Git commit after each working feature
5. **Deploy early**: Get it on Railway as soon as auth works

## 🎉 You Got This!

Follow this checklist and you'll have a working backend in 30 minutes. Focus on getting the MVP working first, then add features incrementally.

The hard work is done - the architecture is solid, patterns are clear, and most files are created. You're in the final stretch!

Good luck! 🚀
