#!/bin/bash

echo "📁 Organizing Gauge setup scripts..."
echo ""

# Create scripts directory
mkdir -p scripts/gauge-setup

# Move scripts
echo "Moving scripts to scripts/gauge-setup/..."
mv diagnose-gauge.sh scripts/gauge-setup/ 2>/dev/null && echo "  ✓ diagnose-gauge.sh"
mv setup-gauge.sh scripts/gauge-setup/ 2>/dev/null && echo "  ✓ setup-gauge.sh"
mv fix-gauge-noclassdef.sh scripts/gauge-setup/ 2>/dev/null && echo "  ✓ fix-gauge-noclassdef.sh"
mv fix-slf4j-conflict.sh scripts/gauge-setup/ 2>/dev/null && echo "  ✓ fix-slf4j-conflict.sh"
mv disable-gauge-slf4j.sh scripts/gauge-setup/ 2>/dev/null && echo "  ✓ disable-gauge-slf4j.sh"
mv quick-fix-slf4j.sh scripts/gauge-setup/ 2>/dev/null && echo "  ✓ quick-fix-slf4j.sh"

# Move documentation
echo ""
echo "Moving documentation to docs/..."
mkdir -p docs
mv GAUGE_TROUBLESHOOTING.md docs/ 2>/dev/null && echo "  ✓ GAUGE_TROUBLESHOOTING.md"
mv GAUGE_CONFIG_EXPLAINED.md docs/ 2>/dev/null && echo "  ✓ GAUGE_CONFIG_EXPLAINED.md"
mv FIX_NOCLASSDEF.md docs/ 2>/dev/null && echo "  ✓ FIX_NOCLASSDEF.md"
mv FIX_SLF4J_CONFLICT.md docs/ 2>/dev/null && echo "  ✓ FIX_SLF4J_CONFLICT.md"
mv BUILD_GRADLE_FIXES.md docs/ 2>/dev/null && echo "  ✓ BUILD_GRADLE_FIXES.md"
mv GRADLE_QUICK_REFERENCE.md docs/ 2>/dev/null && echo "  ✓ GRADLE_QUICK_REFERENCE.md"
mv TEST_FIXES_SUMMARY.md docs/ 2>/dev/null && echo "  ✓ TEST_FIXES_SUMMARY.md"
mv STATUS_CODE_FIX.md docs/ 2>/dev/null && echo "  ✓ STATUS_CODE_FIX.md"
mv FINAL_HIBERNATE_FIX.md docs/ 2>/dev/null && echo "  ✓ FINAL_HIBERNATE_FIX.md"
mv UNIT_TEST_UPDATES.md docs/ 2>/dev/null && echo "  ✓ UNIT_TEST_UPDATES.md"
mv COMPLETE_SUMMARY.md docs/ 2>/dev/null && echo "  ✓ COMPLETE_SUMMARY.md"
mv HIBERNATE_FIX_EXPLANATION.md docs/ 2>/dev/null && echo "  ✓ HIBERNATE_FIX_EXPLANATION.md"

# Create README in scripts directory
cat > scripts/gauge-setup/README.md << 'EOF'
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
EOF

# Make all scripts executable
chmod +x scripts/gauge-setup/*.sh

echo ""
echo "✅ Organization complete!"
echo ""
echo "Structure:"
echo "  scripts/gauge-setup/    - All setup and diagnostic scripts"
echo "  docs/                   - All documentation"
echo ""
echo "Update your .gitignore to include:"
echo "  libs/gauge/             - Generated dependencies (should not be committed)"
echo ""
echo "These scripts are now ready for:"
echo "  - Team onboarding"
echo "  - CI/CD setup"
echo "  - Future troubleshooting"
echo ""
