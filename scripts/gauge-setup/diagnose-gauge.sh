#!/bin/bash

# Gauge Diagnostic Script
# Run this to diagnose Gauge test issues

echo "╔════════════════════════════════════════════════════════════════════"
echo "║ Gauge Test Diagnostics"
echo "╚════════════════════════════════════════════════════════════════════"
echo ""

# 1. Check Gauge installation
echo "1. Checking Gauge installation..."
if command -v gauge &> /dev/null; then
    echo "   ✅ Gauge found"
    gauge version
else
    echo "   ❌ Gauge not found!"
    echo "   Install: brew install gauge"
    exit 1
fi
echo ""

# 2. Check Gauge Java plugin (fixed grep)
echo "2. Checking Gauge Java plugin..."
if gauge version | grep -i "java" &> /dev/null; then
    echo "   ✅ Java plugin installed"
    gauge version | grep "java"
else
    echo "   ❌ Java plugin missing"
    echo "   Run: gauge install java"
    exit 1
fi
echo ""

# 3. Check PostgreSQL
echo "3. Checking PostgreSQL..."
if lsof -i :5432 &> /dev/null; then
    echo "   ✅ PostgreSQL running on port 5432"
    lsof -i :5432 | head -2
else
    echo "   ⚠️  PostgreSQL not running on port 5432"
    echo "   Start: docker run --name resume-postgres -p 5432:5432 \\"
    echo "          -e POSTGRES_DB=resume_db -e POSTGRES_USER=resume_user \\"
    echo "          -e POSTGRES_PASSWORD=resume_password -d postgres:14-alpine"
fi
echo ""

# 4. Check port 8081 (test server)
echo "4. Checking port 8081 (test server)..."
if lsof -i :8081 &> /dev/null; then
    echo "   ⚠️  Port 8081 is in use"
    echo "   Process:"
    lsof -i :8081 | grep LISTEN
    echo ""
    echo "   Kill with: kill -9 \$(lsof -t -i :8081)"
else
    echo "   ✅ Port 8081 available"
fi
echo ""

# 5. Check Gauge specs
echo "5. Checking Gauge specs..."
if [ -d "specs" ]; then
    echo "   ✅ specs/ directory exists"
    spec_count=$(find specs -name "*.spec" | wc -l | tr -d ' ')
    echo "   Found $spec_count spec file(s)"
    find specs -name "*.spec" | while read spec; do
        echo "      - $spec"
    done
else
    echo "   ❌ specs/ directory not found!"
    exit 1
fi
echo ""

# 6. Validate specs
echo "6. Validating Gauge specs..."
gauge validate specs 2>&1 | head -20
validation_result=${PIPESTATUS[0]}
if [ $validation_result -eq 0 ]; then
    echo "   ✅ All specs are valid"
else
    echo "   ⚠️  Spec validation has warnings/errors (see above)"
fi
echo ""

# 7. Check test classes
echo "7. Checking test classes..."
if [ -d "build/classes/java/test" ]; then
    echo "   ✅ Test classes compiled"
    if [ -f "build/classes/java/test/com/deryncullen/resume/specs/ProfileApiSteps.class" ]; then
        echo "   ✅ ProfileApiSteps.class found"
    else
        echo "   ⚠️  ProfileApiSteps.class not found"
        echo "   Run: ./gradlew testClasses"
    fi
else
    echo "   ⚠️  Test classes not compiled"
    echo "   Run: ./gradlew testClasses"
fi
echo ""

# 8. Check classpath
echo "8. Checking Gauge Java classpath..."
if [ -f "env/default/java.properties" ]; then
    echo "   ✅ env/default/java.properties exists"
    echo "   Content:"
    cat env/default/java.properties | sed 's/^/      /'
else
    echo "   ⚠️  env/default/java.properties not found"
    echo "   Creating default configuration..."
    mkdir -p env/default
    cat > env/default/java.properties << 'EOF'
# Gauge Java properties
gauge_custom_classpath = build/classes/java/test:build/resources/test:build/classes/java/main:build/resources/main

# Add all test dependencies  
gauge_additional_libs = true
EOF
    echo "   ✅ Created env/default/java.properties"
fi
echo ""

# 9. Try running a test
echo "9. Attempting to run Gauge tests..."
echo "   Command: gauge run --verbose specs"
echo "   ──────────────────────────────────────────────────────────────────"
echo ""

gauge run --verbose specs
exit_code=$?

echo ""
echo "   ──────────────────────────────────────────────────────────────────"

if [ $exit_code -eq 0 ]; then
    echo ""
    echo "╔════════════════════════════════════════════════════════════════════"
    echo "║ ✅ All Gauge tests passed!"
    echo "╠════════════════════════════════════════════════════════════════════"
    echo "║"
    echo "║ Now try via Gradle:"
    echo "║   ./gradlew gaugeTest"
    echo "║"
    echo "╚════════════════════════════════════════════════════════════════════"
else
    echo ""
    echo "╔════════════════════════════════════════════════════════════════════"
    echo "║ ❌ Gauge tests failed with exit code: $exit_code"
    echo "╠════════════════════════════════════════════════════════════════════"
    echo "║"
    echo "║ Common solutions:"
    echo "║"
    echo "║ 1. Ensure PostgreSQL is running (see step 3 above)"
    echo "║    docker start resume-postgres"
    echo "║"
    echo "║ 2. Kill any process using port 8081 (see step 4 above)"
    echo "║    kill -9 \$(lsof -t -i :8081)"
    echo "║"
    echo "║ 3. Recompile test classes:"
    echo "║    ./gradlew cleanTest testClasses"
    echo "║"
    echo "║ 4. Check detailed logs:"
    echo "║    open reports/html-report/index.html"
    echo "║    cat logs/gauge.log"
    echo "║"
    echo "╚════════════════════════════════════════════════════════════════════"
fi

exit $exit_code
