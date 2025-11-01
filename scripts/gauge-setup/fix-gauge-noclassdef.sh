#!/bin/bash

echo "🔧 Fixing Gauge NoClassDefFoundError Issue"
echo "════════════════════════════════════════════════════════════════════"
echo ""

# Step 1: Update build.gradle
echo "1️⃣  Updating build.gradle..."
if [ -f "build.gradle.backup" ]; then
    echo "   ⚠️  Backup already exists, skipping"
else
    cp build.gradle build.gradle.backup
    echo "   📦 Backed up to build.gradle.backup"
fi
echo ""

# Step 2: Update java.properties
echo "2️⃣  Updating env/default/java.properties..."
mkdir -p env/default
if [ -f "env/default/java.properties.backup" ]; then
    echo "   ⚠️  Backup already exists, skipping"
else
    if [ -f "env/default/java.properties" ]; then
        cp env/default/java.properties env/default/java.properties.backup
        echo "   📦 Backed up existing config"
    fi
fi

cat > env/default/java.properties << 'EOF'
# Gauge Java Configuration
# Configured to work with Gradle-built projects

# Don't compile - use Gradle's compiled classes
gauge_custom_compile_dir =

# Use Gradle's build output
gauge_custom_build_path = build/classes/java/main:build/classes/java/test:build/resources/main:build/resources/test

# All dependencies are copied to libs/gauge by Gradle
gauge_additional_libs = libs/gauge/*

# JVM settings
gauge_jvm_args = -Xmx1024m

# Clear state per scenario
gauge_clear_state_level = scenario
EOF
echo "   ✅ Updated java.properties"
echo ""

# Step 3: Copy dependencies
echo "3️⃣  Copying dependencies to libs/gauge/..."
./gradlew copyGaugeDependencies --console=plain 2>&1 | grep -E "(BUILD|Copied|Task)" | tail -5
if [ ${PIPESTATUS[0]} -eq 0 ]; then
    jar_count=$(ls -1 libs/gauge/*.jar 2>/dev/null | wc -l | tr -d ' ')
    echo "   ✅ Copied $jar_count JAR files"
else
    echo "   ❌ Failed to copy dependencies"
    exit 1
fi
echo ""

# Step 4: Compile classes
echo "4️⃣  Compiling test classes..."
./gradlew classes testClasses --console=plain 2>&1 | grep -E "(BUILD|Task)" | tail -5
if [ ${PIPESTATUS[0]} -eq 0 ]; then
    echo "   ✅ Classes compiled"
else
    echo "   ❌ Compilation failed"
    exit 1
fi
echo ""

# Step 5: Verify setup
echo "5️⃣  Verifying setup..."
echo "   Checking libs/gauge/:"
ls libs/gauge/*.jar 2>/dev/null | head -5 | while read jar; do
    echo "      ✓ $(basename $jar)"
done
echo "      ... and $(ls -1 libs/gauge/*.jar 2>/dev/null | wc -l | tr -d ' ') more"
echo ""

echo "   Checking compiled classes:"
if [ -f "build/classes/java/test/com/deryncullen/resume/specs/ProfileApiSteps.class" ]; then
    echo "      ✓ ProfileApiSteps.class"
else
    echo "      ✗ ProfileApiSteps.class NOT FOUND"
    exit 1
fi
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "✅ Setup Complete!"
echo "════════════════════════════════════════════════════════════════════"
echo ""
echo "Now run Gauge tests:"
echo ""
echo "  Direct:  gauge run specs"
echo "  Gradle:  ./gradlew gaugeTest"
echo ""
echo "If you still get NoClassDefFoundError:"
echo "  1. Check: ls -la libs/gauge/ | head -20"
echo "  2. Check: cat env/default/java.properties"
echo "  3. Run: ./gradlew clean copyGaugeDependencies testClasses"
echo ""
