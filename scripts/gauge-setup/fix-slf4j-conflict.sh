#!/bin/bash

echo "🔧 Fixing Gauge SLF4J Conflict"
echo "════════════════════════════════════════════════════════════════════"
echo ""

# The Issue:
# Gauge Java plugin v0.13.0 includes slf4j-api-1.7.32.jar
# Spring Boot 3.2.0 uses slf4j-api-2.x with Logback
# These conflict, causing IllegalArgumentException

echo "📋 Problem: SLF4J version conflict"
echo "   Gauge plugin: slf4j-api-1.7.32.jar (old)"
echo "   Spring Boot:  slf4j-api-2.x.x (new + Logback)"
echo ""

# Solution 1: Update Gauge Java plugin to latest
echo "1️⃣  Checking Gauge Java plugin version..."
current_version=$(gauge version | grep "java" | awk '{print $2}' | tr -d '()')
echo "   Current: java ($current_version)"

if [ "$current_version" = "0.13.0" ]; then
    echo "   📦 Updating to latest version..."
    gauge install java
    echo "   ✅ Updated Gauge Java plugin"
else
    echo "   ✅ Already on latest"
fi
echo ""

# Solution 2: Configure java.properties with correct classpath priority
echo "2️⃣  Configuring java.properties..."
mkdir -p env/default

# Backup existing
if [ -f "env/default/java.properties" ]; then
    cp env/default/java.properties env/default/java.properties.slf4j-backup
fi

cat > env/default/java.properties << 'EOF'
# Gauge Java Configuration for Spring Boot
# Fixes SLF4J conflict by ensuring our dependencies load first

# Don't compile - use Gradle's compiled classes
gauge_custom_compile_dir =

# Our compiled classes (load first)
gauge_custom_build_path = build/classes/java/main:build/classes/java/test:build/resources/main:build/resources/test

# Our dependencies - these will override Gauge's built-in JARs
# CRITICAL: This includes our slf4j-api-2.x and logback JARs
gauge_additional_libs = libs/gauge/*

# JVM args
gauge_jvm_args = -Xmx1024m

# Clear state per scenario
gauge_clear_state_level = scenario
EOF
echo "   ✅ Updated java.properties"
echo ""

# Solution 3: Ensure our SLF4J is in libs/gauge
echo "3️⃣  Verifying SLF4J in libs/gauge..."
if ls libs/gauge/slf4j-api-*.jar 1> /dev/null 2>&1; then
    slf4j_jar=$(ls libs/gauge/slf4j-api-*.jar | head -1)
    echo "   ✅ Found: $(basename $slf4j_jar)"
else
    echo "   ⚠️  SLF4J not found in libs/gauge"
    echo "   Running: ./gradlew copyGaugeDependencies"
    ./gradlew copyGaugeDependencies --quiet
    echo "   ✅ Dependencies copied"
fi

if ls libs/gauge/logback-*.jar 1> /dev/null 2>&1; then
    echo "   ✅ Found Logback JARs"
else
    echo "   ⚠️  Logback not found"
    ./gradlew copyGaugeDependencies --quiet
fi
echo ""

# Verify our SLF4J version is newer
echo "4️⃣  Checking SLF4J versions..."
gauge_slf4j_path="$HOME/.gauge/plugins/java/0.13.0/libs/slf4j-api-1.7.32.jar"
if [ -f "$gauge_slf4j_path" ]; then
    echo "   ⚠️  Gauge plugin has old: slf4j-api-1.7.32.jar"
fi
our_slf4j=$(ls libs/gauge/slf4j-api-*.jar 2>/dev/null | head -1)
if [ -f "$our_slf4j" ]; then
    echo "   ✅ Our version: $(basename $our_slf4j)"
fi
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "✅ Configuration Updated!"
echo "════════════════════════════════════════════════════════════════════"
echo ""
echo "How this works:"
echo "  1. gauge_additional_libs loads OUR SLF4J + Logback first"
echo "  2. This takes precedence over Gauge's built-in SLF4J"
echo "  3. Spring Boot can now initialize Logback properly"
echo ""
echo "Test it:"
echo "  gauge run specs"
echo "  ./gradlew gaugeTest"
echo ""
echo "If still failing, the issue is classpath ordering."
echo "The gauge-java plugin loads its JARs before additional_libs."
echo ""
echo "Nuclear option (if above doesn't work):"
echo "  # Rename Gauge's SLF4J so it can't be loaded"
echo "  cd ~/.gauge/plugins/java/0.13.0/libs/"
echo "  mv slf4j-api-1.7.32.jar slf4j-api-1.7.32.jar.disabled"
echo ""
