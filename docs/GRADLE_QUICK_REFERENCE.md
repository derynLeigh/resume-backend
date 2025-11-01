# Quick Reference - Gradle Commands

## 🚀 Most Common Commands

```bash
# Development - Fast feedback loop
./gradlew test                          # Run unit tests only
./gradlew gaugeTest                     # Run BDD tests only
./gradlew build                         # Build without tests

# Full test suite
./gradlew clean build test gaugeTest    # Everything (use in CI/CD)

# Watch mode
./gradlew test --continuous             # Re-run on file changes
```

## 🧪 Testing Commands

### Unit Tests
```bash
./gradlew test                          # All unit tests
./gradlew unitTest                      # Unit tests only (no integration)
./gradlew test --tests "ProfileServiceTest"  # Specific test class
./gradlew test --tests "*Profile*"     # Pattern matching
```

### Integration Tests
```bash
./gradlew integrationTest               # Integration tests only
```

### BDD Tests (Gauge)
```bash
./gradlew gaugeTest                     # All specs
./gradlew gaugeRun -Pspec=specs/profile_api.spec  # Specific spec
./gradlew gaugeValidate                 # Validate specs without running
```

### Coverage
```bash
./gradlew test jacocoTestReport         # Generate coverage report
open build/reports/jacoco/test/html/index.html  # View report
```

## 🏗️ Build Commands

```bash
./gradlew clean                         # Clean build directory
./gradlew build                         # Compile and package (no tests)
./gradlew build -x test                 # Build, skip tests
./gradlew assemble                      # Build without running tests
./gradlew bootJar                       # Create executable JAR
```

## 🔍 Inspection Commands

```bash
./gradlew tasks                         # List available tasks
./gradlew tasks --all                   # List all tasks (including internal)
./gradlew dependencies                  # Show dependency tree
./gradlew properties                    # Show project properties
./gradlew help --task test              # Get help for specific task
```

## 🐛 Debugging Commands

```bash
./gradlew test --info                   # Verbose logging
./gradlew test --debug                  # Debug logging
./gradlew test --stacktrace             # Show stack traces
./gradlew build --scan                  # Generate build scan
./gradlew --refresh-dependencies        # Force refresh dependencies
```

## 🚢 Running the Application

```bash
./gradlew bootRun                       # Run Spring Boot app
./gradlew bootRun --args='--spring.profiles.active=dev'  # With profile
```

## 📦 Dependency Management

```bash
./gradlew dependencies                  # Show all dependencies
./gradlew dependencyInsight --dependency spring-boot  # Specific dependency
./gradlew --refresh-dependencies        # Refresh cached dependencies
```

## 🧹 Cleanup Commands

```bash
./gradlew clean                         # Remove build directory
./gradlew cleanTest                     # Clean test results only
rm -rf ~/.gradle/caches                 # Clear Gradle cache (nuclear option)
```

## ⚡ Performance Tips

```bash
# Parallel execution
./gradlew test --parallel

# Gradle daemon status
./gradlew --status

# Stop daemon (if stuck)
./gradlew --stop

# Use build cache
./gradlew build --build-cache
```

## 🎯 Common Workflows

### TDD Workflow
```bash
# Watch tests continuously
./gradlew test --continuous

# In another terminal, edit code and save
# Tests auto-run on file changes
```

### BDD Workflow
```bash
# 1. Write spec in specs/*.spec
# 2. Run to see failures
./gradlew gaugeTest

# 3. Implement step definitions
# 4. Re-run until green
./gradlew gaugeTest
```

### Pre-commit Workflow
```bash
# Before committing
./gradlew clean test gaugeTest
```

### CI/CD Workflow
```bash
# Full clean build with all tests
./gradlew clean build test gaugeTest

# With coverage
./gradlew clean test jacocoTestReport gaugeTest
```

## 📊 Report Locations

```
build/reports/
├── tests/test/                     # JUnit test results
│   └── index.html
├── jacoco/test/                    # Code coverage
│   └── html/index.html
└── html-report/                    # Gauge BDD results
    └── index.html
```

## 🔧 Gradle Wrapper Commands

```bash
# Update wrapper version
./gradlew wrapper --gradle-version=8.5

# Verify wrapper
./gradlew --version

# Regenerate wrapper files
gradle wrapper
```

## 💡 Tips

1. **Use the daemon**: Speeds up subsequent builds
   ```bash
   # Daemon starts automatically on first run
   # Check status: ./gradlew --status
   ```

2. **Build scan**: Share build info with team
   ```bash
   ./gradlew build --scan
   # Generates URL to view build details
   ```

3. **Continuous testing**: Get instant feedback
   ```bash
   ./gradlew test --continuous
   ```

4. **Parallel tests**: Speed up test execution
   ```bash
   ./gradlew test --parallel --max-workers=4
   ```

5. **Profile tests**: See what's slow
   ```bash
   ./gradlew test --profile
   # Creates build/reports/profile/
   ```

## ⚠️ Troubleshooting

### Tests not found
```bash
./gradlew cleanTest test --rerun-tasks
```

### Dependency issues
```bash
./gradlew --refresh-dependencies clean build
```

### Build cache issues
```bash
./gradlew clean --no-build-cache
```

### Port already in use (tests)
```bash
# Find process on port 8081
lsof -i :8081
# Kill it
kill -9 <PID>
```

### Gauge not found
```bash
# Check installation
gauge version

# Install if needed (macOS)
brew install gauge

# Verify in PATH
which gauge
```

## 📝 Environment Variables

```bash
# Set Java version
export JAVA_HOME=/path/to/jdk-21

# Gradle options
export GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Skip tests temporarily
export SKIP_TESTS=true
./gradlew build $([ "$SKIP_TESTS" = "true" ] && echo "-x test")
```

## 🎓 Learning More

```bash
# Official docs
open https://docs.gradle.org

# Project tasks
./gradlew tasks

# Task details
./gradlew help --task <taskName>

# Build scan for insights
./gradlew build --scan
```
