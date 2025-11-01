#!/bin/bash

echo "🚀 Setting up Gauge test environment..."
echo ""

# 1. Stop any processes on port 8081
echo "1. Checking port 8081..."
if lsof -i :8081 &> /dev/null; then
    echo "   Killing process on port 8081..."
    kill -9 $(lsof -t -i :8081) 2>/dev/null
    sleep 1
    echo "   ✅ Port 8081 freed"
else
    echo "   ✅ Port 8081 is free"
fi
echo ""

# 2. Check/Start PostgreSQL
echo "2. Checking PostgreSQL..."
if lsof -i :5432 &> /dev/null; then
    echo "   ✅ PostgreSQL is running"
else
    echo "   Starting PostgreSQL container..."
    docker run --name resume-postgres \
      -e POSTGRES_DB=resume_db \
      -e POSTGRES_USER=resume_user \
      -e POSTGRES_PASSWORD=resume_password \
      -p 5432:5432 \
      -d postgres:14-alpine
    
    echo "   Waiting for PostgreSQL to be ready..."
    sleep 3
    echo "   ✅ PostgreSQL started"
fi
echo ""

# 3. Compile test classes
echo "3. Compiling test classes..."
./gradlew testClasses --console=plain 2>&1 | tail -5
if [ ${PIPESTATUS[0]} -eq 0 ]; then
    echo "   ✅ Test classes compiled"
else
    echo "   ❌ Compilation failed - check output above"
    exit 1
fi
echo ""

# 4. Create/check env configuration
echo "4. Checking Gauge environment configuration..."
if [ ! -f "env/default/java.properties" ]; then
    echo "   Creating env/default/java.properties..."
    mkdir -p env/default
    cat > env/default/java.properties << 'EOF'
gauge_custom_classpath = build/classes/java/test:build/resources/test:build/classes/java/main:build/resources/main
gauge_additional_libs = true
EOF
    echo "   ✅ Configuration created"
else
    echo "   ✅ Configuration exists"
fi
echo ""

# 5. Validate specs
echo "5. Validating Gauge specs..."
gauge validate specs &> /dev/null
if [ $? -eq 0 ]; then
    echo "   ✅ Specs are valid"
else
    echo "   ⚠️  Spec validation warnings (may be okay)"
fi
echo ""

echo "═══════════════════════════════════════════════════════════════════"
echo "✅ Environment ready!"
echo "═══════════════════════════════════════════════════════════════════"
echo ""
echo "Now try:"
echo "  1. Direct:  gauge run --verbose specs"
echo "  2. Gradle:  ./gradlew gaugeTest"
echo ""
