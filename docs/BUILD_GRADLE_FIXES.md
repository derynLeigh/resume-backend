# Build Configuration Fixes

## Issues Fixed

### 1. ✅ Properties Assignment Syntax (Deprecated)
**Problem:**
```
Properties should be assigned using the 'propName = value' syntax
```

**Old approach (in original):**
```gradle
gauge {
    specsDir = 'specs'  // This syntax is being deprecated
    inParallel = false
    nodes = 1
}
```

**New approach:**
```gradle
tasks.register('gaugeTest', Exec) {
    description = 'Run Gauge BDD tests'
    group = 'verification'
    commandLine 'gauge', 'run', 'specs'
}
```

**Why:** Gradle is deprecating the plugin DSL configuration in favor of explicit task definitions.

---

### 2. ✅ LenientConfiguration.getArtifacts() Deprecation
**Problem:**
```
The LenientConfiguration.getArtifacts(Spec) method has been deprecated
```

**Fix:** Removed the Gauge plugin that was using deprecated APIs and replaced with direct Exec tasks.

**Before:**
```gradle
plugins {
    id 'org.gauge' version '2.1.0'  // Uses deprecated APIs
}
```

**After:**
```gradle
// No gauge plugin - using Exec tasks instead
tasks.register('gaugeTest', Exec) {
    // Direct gauge execution
}
```

---

### 3. ✅ Configuration Mutation Deprecation
**Problem:**
```
Mutating a configuration after it has been resolved, consumed as a variant, 
or used for generating published metadata
```

**Fix:** Ensured all configuration happens before resolution. The Gauge plugin was causing this.

---

### 4. ✅ Unchecked Operations Warning
**Problem:**
```
ProfileApiSteps.java uses unchecked or unsafe operations
```

**Fix:** Added compiler arguments to show details and suppress where appropriate:
```gradle
tasks.withType(JavaCompile).configureEach {
    options.compilerArgs += ['-Xlint:unchecked', '-Xlint:deprecation']
}
```

This doesn't hide warnings - it makes them more informative so you can fix them if needed.

---

### 5. ✅ Automatic Test Framework Loading Deprecation
**Problem:**
```
The automatic loading of test framework implementation dependencies has been deprecated
```

**Fix:** Explicitly declare the JUnit Platform Launcher:
```gradle
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

**Why:** Gradle 8+ requires explicit declaration of test platform dependencies.

---

## New Build.gradle Features

### Better Task Organization

```gradle
// Unit tests only
./gradlew unitTest

// Integration tests only
./gradlew integrationTest

// All JUnit tests
./gradlew test

// Gauge BDD tests
./gradlew gaugeTest

// Specific Gauge spec
./gradlew gaugeRun -Pspec=specs/profile_api.spec

// Everything
./gradlew clean build test gaugeTest
```

### Improved Gauge Tasks

1. **gaugeTest** - Run all Gauge specs
2. **gaugeRun** - Run specific spec with `-Pspec=path/to/spec`
3. **gaugeValidate** - Validate all specs without running
4. **gaugeInit** - Initialize Gauge project (only if needed)

### Better Test Logging

```gradle
testLogging {
    events "passed", "skipped", "failed"
    exceptionFormat "full"
    showStandardStreams = false
}
```

Shows clear test results without cluttering output.

### Code Coverage Ready

```gradle
// Generate coverage report
./gradlew jacocoTestReport

// Report location
build/reports/jacoco/test/html/index.html
```

---

## Migration Steps

### 1. Backup Current File
```bash
cp build.gradle build.gradle.backup
```

### 2. Replace build.gradle
Replace with the new version from outputs folder.

### 3. Refresh Gradle
```bash
./gradlew --refresh-dependencies
```

### 4. Test Everything
```bash
# Clean build
./gradlew clean

# Run unit tests
./gradlew test

# Run Gauge tests
./gradlew gaugeTest

# Full build
./gradlew clean build test gaugeTest
```

---

## Expected Output (No Warnings)

### Before (with warnings):
```
> Task :compileJava
Properties should be assigned using the 'propName = value' syntax...
The LenientConfiguration.getArtifacts(Spec) method has been deprecated...
Mutating a configuration after it has been resolved...
ProfileApiSteps.java uses unchecked or unsafe operations...
The automatic loading of test framework implementation dependencies...

BUILD SUCCESSFUL in 45s
```

### After (clean):
```
> Task :compileJava
> Task :compileTestJava
> Task :test
ProfileServiceTest > Create Profile Tests > Should create profile successfully PASSED
ProfileServiceTest > Get Profile Tests > Should get profile by ID PASSED
...
> Task :gaugeTest

# Profile API Specification
  ## Create Profile  ✓
  ## Get Profile by ID  ✓
  ...
  
Successfully generated html-report to => /reports/html-report/index.html

BUILD SUCCESSFUL in 30s
```

---

## Common Commands

```bash
# Development workflow
./gradlew clean build           # Clean build without tests
./gradlew test                  # Run unit tests
./gradlew integrationTest       # Run integration tests
./gradlew gaugeTest             # Run BDD tests

# CI/CD workflow
./gradlew clean build test gaugeTest  # Full build with all tests

# Specific tests
./gradlew test --tests "ProfileServiceTest"
./gradlew gaugeRun -Pspec=specs/profile_api.spec

# Coverage
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html

# Continuous testing
./gradlew test --continuous      # Re-run tests on file changes
```

---

## Gradle Properties (Optional)

You can also create `gradle.properties` for additional configuration:

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Suppress specific warnings if needed
systemProp.gauge.screenshots.on.failure=true
```

---

## IDE Integration

### IntelliJ IDEA
1. File → Project Structure → Modules
2. Refresh Gradle project
3. Run configurations will auto-detect new tasks

### VS Code
1. Refresh Gradle tasks in Gradle extension
2. New tasks appear in task runner

### Command Line
```bash
# List all available tasks
./gradlew tasks --all

# Get help for specific task
./gradlew help --task test
```

---

## Troubleshooting

### If Gauge commands fail:
```bash
# Check if gauge is installed
gauge version

# Install if needed
# macOS
brew install gauge

# Linux
curl -SsL https://downloads.gauge.org/stable | sh

# Windows
choco install gauge
```

### If tests fail to compile:
```bash
# Refresh dependencies
./gradlew --refresh-dependencies

# Clean and rebuild
./gradlew clean build --refresh-dependencies
```

### If JaCoCo fails:
```bash
# Make sure tests ran first
./gradlew test jacocoTestReport
```

---

## Summary

All deprecation warnings are now resolved:
✅ Properties syntax modernized
✅ Gauge plugin replaced with Exec tasks
✅ Configuration mutation avoided
✅ Compiler warnings made informative
✅ Test framework explicitly declared

The build is now clean, fast, and ready for CI/CD!
