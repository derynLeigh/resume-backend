#!/bin/bash
# One-line fix for Gauge SLF4J conflict
# Just run this and you're done!

echo "🔧 Quick Fix: Disabling Gauge's conflicting SLF4J..."

# Disable Gauge's old SLF4J
GAUGE_SLF4J="$HOME/.gauge/plugins/java/0.13.0/libs/slf4j-api-1.7.32.jar"
if [ -f "$GAUGE_SLF4J" ]; then
    mv "$GAUGE_SLF4J" "$GAUGE_SLF4J.disabled"
    echo "✅ Disabled: $(basename $GAUGE_SLF4J)"
else
    echo "⚠️  Not found at: $GAUGE_SLF4J"
    echo "   Searching..."
    find ~/.gauge/plugins/java -name "slf4j-api-*.jar" 2>/dev/null | while read jar; do
        mv "$jar" "$jar.disabled"
        echo "✅ Disabled: $jar"
    done
fi

# Verify our SLF4J is available
if [ ! -d "libs/gauge" ] || [ -z "$(ls -A libs/gauge 2>/dev/null)" ]; then
    echo "📦 Copying dependencies..."
    ./gradlew copyGaugeDependencies --quiet
fi

echo ""
echo "✅ Fixed! Now run: gauge run specs"
