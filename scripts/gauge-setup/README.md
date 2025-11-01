# Gauge Setup Scripts

Helper scripts for setting up and troubleshooting Gauge BDD tests with Spring Boot.

## Quick Start

If you encounter issues with Gauge tests, run these in order:

```bash
# 1. Diagnose issues
./diagnose-gauge.sh

# 2. Apply fixes based on diagnosis
./setup-gauge.sh              # General setup
./fix-gauge-noclassdef.sh     # Fix NoClassDefFoundError
./quick-fix-slf4j.sh          # Fix SLF4J conflict (most common)
```

## Scripts Overview

### Diagnostic
- **diagnose-gauge.sh** - Comprehensive diagnostic tool
  - Checks Gauge installation
  - Verifies dependencies
  - Tests configuration
  - Runs sample tests

### Setup
- **setup-gauge.sh** - Initial environment setup
  - Frees ports
  - Starts PostgreSQL
  - Compiles classes
  - Configures Gauge

### Fixes
- **fix-gauge-noclassdef.sh** - Fix missing dependencies
  - Copies Gradle dependencies to libs/gauge
  - Updates java.properties

- **fix-slf4j-conflict.sh** - Fix SLF4J version conflicts
  - Updates Gauge plugin
  - Configures classpath priority

- **disable-gauge-slf4j.sh** - Nuclear option for SLF4J
  - Disables Gauge's built-in SLF4J
  - Forces use of Spring Boot's version

- **quick-fix-slf4j.sh** - One-command SLF4J fix
  - Fastest way to fix SLF4J conflict
  - Recommended first approach

## Common Issues

### SLF4J Conflict (IllegalArgumentException)
```bash
./quick-fix-slf4j.sh
gauge run specs
```

### NoClassDefFoundError
```bash
./fix-gauge-noclassdef.sh
gauge run specs
```

### General Setup Issues
```bash
./diagnose-gauge.sh
# Follow recommendations from output
```

## Team Onboarding

New team members should run:
```bash
cd scripts/gauge-setup
./setup-gauge.sh
```

This ensures:
- PostgreSQL is running
- Dependencies are copied
- Gauge is configured
- Test classes are compiled

## CI/CD Integration

In your CI/CD pipeline:
```bash
# Setup
./scripts/gauge-setup/setup-gauge.sh

# Run tests
./gradlew gaugeTest
```

## Maintenance

These scripts should be kept in version control because:
1. They document solutions to real problems
2. They help new developers get started
3. They're useful for CI/CD setup
4. They provide troubleshooting guidance

## Need Help?

See detailed documentation in `/docs`:
- FIX_SLF4J_CONFLICT.md
- FIX_NOCLASSDEF.md
- GAUGE_TROUBLESHOOTING.md
