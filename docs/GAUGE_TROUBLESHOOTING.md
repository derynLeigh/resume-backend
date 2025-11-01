# Gauge Test Troubleshooting Guide

## Quick Fix Steps

### Step 1: Update build.gradle
Replace your `build.gradle` with the new version from outputs (it shows better error messages now).

### Step 2: Run Diagnostics
```bash
# Make the script executable
chmod +x diagnose-gauge.sh

# Run diagnostics
./diagnose-gauge.sh
```

This will check:
- ✅ Gauge installation
- ✅ Java plugin
- ✅ PostgreSQL status
- ✅ Port availability
- ✅ Spec files
- ✅ Test classes
- ✅ And more...

### Step 3: Try Manual Gauge Run
```bash
# See what Gauge actually outputs
cd /Users/deryncullen/dev/PoCs/resume-backend
gauge run --verbose specs
```

## Most Common Issues & Fixes

### Issue 1: PostgreSQL Not Running
```bash
# Check if running
lsof -i :5432

# Start PostgreSQL
docker run --name resume-postgres \
  -e POSTGRES_DB=resume_db \
  -e POSTGRES_USER=resume_user \
  -e POSTGRES_PASSWORD=resume_password \
  -p 5432:5432 \
  -d postgres:14-alpine

# Verify it's running
docker ps | grep resume-postgres
```

### Issue 2: Port 8081 In Use
```bash
# Find what's using port 8081
lsof -i :8081

# Kill the process
kill -9 $(lsof -t -i:8081)

# Or kill all Java processes (nuclear option)
pkill -9 java
```

### Issue 3: Test Classes Not Compiled
```bash
# Clean and recompile
./gradlew cleanTest testClasses

# Verify classes exist
ls -la build/classes/java/test/com/deryncullen/resume/specs/
```

### Issue 4: Gauge Java Plugin Missing
```bash
# Check installed plugins
gauge list

# Install Java plugin
gauge install java

# Verify installation
gauge version
```

### Issue 5: Classpath Issues
```bash
# Check if env/default/java.properties exists
cat env/default/java.properties

# If missing, create it
mkdir -p env/default
cat > env/default/java.properties << 'EOF'
# Gauge Java properties
gauge_custom_classpath = build/classes/java/test:build/resources/test:build/classes/java/main:build/resources/main

# Add all test dependencies
gauge_additional_libs = true
EOF
```

### Issue 6: H2 Database Conflicts
```bash
# Stop any running H2 instances
pkill -f h2

# Clear H2 database files (if they exist)
rm -rf ~/testdb*
rm -rf testdb*
```

## Debug Commands

### Verbose Gauge Output
```bash
# Most verbose
gauge run --verbose --log-level=debug specs

# With all details
GAUGE_LOG_LEVEL=debug gauge run specs
```

### Check Gauge Environment
```bash
# List all specs
gauge list specs

# Validate all specs
gauge validate specs

# Check gauge configuration
gauge config
```

### Test Individual Components

#### 1. Test Spring Boot Starts
```bash
# Start the app manually
./gradlew bootRun --args='--spring.profiles.active=test --server.port=8081'

# In another terminal, test it
curl http://localhost:8081/api/actuator/health

# Stop it (Ctrl+C)
```

#### 2. Test Database Connection
```bash
# Try connecting to PostgreSQL
psql -h localhost -p 5432 -U resume_user -d resume_db

# Password: resume_password
```

#### 3. Test Gauge Step Classes
```bash
# Compile and check
./gradlew testClasses --info

# Verify ProfileApiSteps exists
find build -name "ProfileApiSteps.class"
```

## Common Error Messages & Solutions

### Error: "gauge: command not found"
```bash
# Install Gauge
brew install gauge

# Verify
gauge version
```

### Error: "Failed to start bean 'liquibase'"
```bash
# PostgreSQL not running - start it (see Issue 1 above)
```

### Error: "Port 8081 is already in use"
```bash
# Kill the process (see Issue 2 above)
```

### Error: "Cannot find test implementation"
```bash
# Java plugin not installed
gauge install java

# Or reinstall
gauge uninstall java
gauge install java
```

### Error: "Step implementation not found"
```bash
# Test classes not compiled or not in classpath
./gradlew cleanTest testClasses

# Check env/default/java.properties
cat env/default/java.properties
```

## Full Reset (Nuclear Option)

If all else fails, do a complete reset:

```bash
# 1. Stop everything
docker stop resume-postgres
pkill -9 java

# 2. Clean Gradle
./gradlew clean
rm -rf .gradle build

# 3. Clean Gauge
rm -rf reports logs

# 4. Start fresh
docker start resume-postgres

# 5. Rebuild
./gradlew clean build

# 6. Run Gauge directly
gauge run specs

# 7. If works, try via Gradle
./gradlew gaugeTest
```

## Still Not Working?

### Get Detailed Logs

```bash
# Run with maximum verbosity
GAUGE_LOG_LEVEL=debug ./gradlew gaugeTest --stacktrace --info --debug
```

### Check Specific Files

```bash
# 1. Specs are valid
gauge validate specs

# 2. Steps file exists and compiles
ls -la src/test/java/com/deryncullen/resume/specs/ProfileApiSteps.java
./gradlew :compileTestJava

# 3. Gauge can find steps
gauge list specs
```

### Manual Test Run

Try running tests without Gradle:

```bash
# 1. Compile everything
./gradlew testClasses

# 2. Run Gauge directly
cd /Users/deryncullen/dev/PoCs/resume-backend
gauge run specs

# If this works but ./gradlew gaugeTest doesn't, there's a Gradle configuration issue
```

## Success Checklist

Before running `./gradlew gaugeTest`, verify:

- [ ] Gauge installed: `gauge version`
- [ ] Java plugin: `gauge list | grep java`
- [ ] PostgreSQL running: `lsof -i :5432`
- [ ] Port 8081 free: `! lsof -i :8081`
- [ ] Specs valid: `gauge validate specs`
- [ ] Tests compiled: `./gradlew testClasses`
- [ ] Can run manually: `gauge run specs`

If all checkboxes are ticked and manual run works, but Gradle still fails, the issue is in the build.gradle Exec task configuration.

## Get Help

1. Run diagnostics: `./diagnose-gauge.sh`
2. Check the actual error in the output (not just exit code)
3. Look at: `reports/html-report/index.html` for test details
4. Check: `logs/gauge.log` for Gauge-specific logs

The new build.gradle will show much better error messages when tests fail!
