#!/bin/bash

# Nuclear Option: Disable Gauge's built-in SLF4J
# This forces Gauge to use our Spring Boot-compatible SLF4J

echo "☢️  Nuclear Option: Disabling Gauge's Built-in SLF4J"
echo "════════════════════════════════════════════════════════════════════"
echo ""

GAUGE_JAVA_LIBS="$HOME/.gauge/plugins/java/0.13.0/libs"
SLF4J_JAR="$GAUGE_JAVA_LIBS/slf4j-api-1.7.32.jar"

if [ ! -f "$SLF4J_JAR" ]; then
    echo "❓ Gauge's SLF4J not found at expected location"
    echo "   Looking for: $SLF4J_JAR"
    echo ""
    
    # Try to find it
    echo "   Searching for slf4j-api in Gauge plugins..."
    find ~/.gauge/plugins/java -name "slf4j-api-*.jar" 2>/dev/null | while read jar; do
        echo "   Found: $jar"
    done
    echo ""
    exit 1
fi

echo "📦 Found Gauge's SLF4J:"
echo "   $SLF4J_JAR"
echo ""

# Check if already disabled
if [ -f "$SLF4J_JAR.disabled" ]; then
    echo "✅ Already disabled (found .disabled file)"
    echo ""
    echo "To re-enable:"
    echo "  mv $SLF4J_JAR.disabled $SLF4J_JAR"
    exit 0
fi

# Backup and disable
echo "📦 Creating backup..."
cp "$SLF4J_JAR" "$SLF4J_JAR.backup"
echo "   ✅ Backed up to: $SLF4J_JAR.backup"
echo ""

echo "🚫 Disabling by renaming..."
mv "$SLF4J_JAR" "$SLF4J_JAR.disabled"
echo "   ✅ Renamed to: $SLF4J_JAR.disabled"
echo ""

# Verify our SLF4J is present
echo "✅ Verifying our SLF4J is available..."
if ls libs/gauge/slf4j-api-*.jar 1> /dev/null 2>&1; then
    our_slf4j=$(ls libs/gauge/slf4j-api-*.jar | head -1)
    echo "   ✅ Found: $(basename $our_slf4j)"
else
    echo "   ⚠️  WARNING: libs/gauge/slf4j-api-*.jar not found!"
    echo "   Run: ./gradlew copyGaugeDependencies"
fi
echo ""

echo "════════════════════════════════════════════════════════════════════"
echo "✅ Gauge's SLF4J Disabled!"
echo "════════════════════════════════════════════════════════════════════"
echo ""
echo "Gauge will now use YOUR slf4j-api (from libs/gauge/)"
echo "This is compatible with Spring Boot 3.2.0 + Logback"
echo ""
echo "Test it:"
echo "  gauge run specs"
echo "  ./gradlew gaugeTest"
echo ""
echo "To undo this change:"
echo "  mv $SLF4J_JAR.disabled $SLF4J_JAR"
echo ""
echo "To restore from backup:"
echo "  cp $SLF4J_JAR.backup $SLF4J_JAR"
echo ""
