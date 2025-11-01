# Fixing Gauge SLF4J Conflict with Spring Boot

## The Error

```
java.lang.IllegalArgumentException: LoggerFactory is not a Logback LoggerContext 
but Logback is on the classpath. Either remove Logback or the competing implementation 
(class org.slf4j.helpers.NOPLoggerFactory loaded from 
file:/Users/deryncullen/.gauge/plugins/java/0.13.0/libs/slf4j-api-1.7.32.jar)
```

## What's Happening

**The Conflict:**
- Gauge Java plugin 0.13.0 includes `slf4j-api-1.7.32.jar` (old)
- Spring Boot 3.2.0 uses `slf4j-api-2.x` with Logback (new)
- Both are on the classpath, causing a conflict

**Why It Fails:**
1. Gauge loads its `slf4j-api-1.7.32.jar` FIRST (from plugin libs)
2. This initializes SLF4J with `NOPLoggerFactory` (no-op implementation)
3. Spring Boot tries to initialize Logback
4. Logback expects `slf4j-api-2.x` but finds old version already loaded
5. **BOOM**: IllegalArgumentException

## Solutions (Try in Order)

### Solution 1: Nuclear Option (Recommended - Fastest)

Simply disable Gauge's built-in SLF4J:

```bash
chmod +x disable-gauge-slf4j.sh
./disable-gauge-slf4j.sh
```

This renames Gauge's `slf4j-api-1.7.32.jar` to `.disabled`, forcing it to use YOUR version.

**Test:**
```bash
gauge run specs
```

**Pros:**
- ✅ Works immediately
- ✅ Simple and effective
- ✅ Easy to undo

**Cons:**
- ⚠️  Modifies Gauge plugin directory
- ⚠️  May need to redo after Gauge updates

---

### Solution 2: Update Gauge Plugin (May Work)

Update to the latest Gauge Java plugin:

```bash
# Update plugin
gauge install java

# Check version
gauge version
```

If the new version uses `slf4j-api-2.x`, the conflict is resolved!

**Test:**
```bash
gauge run specs
```

If this still fails, the latest plugin still has the old SLF4J. Proceed to Solution 1.

---

### Solution 3: Classpath Manipulation (Advanced)

Create a custom wrapper script that controls classpath order:

```bash
#!/bin/bash
# gauge-wrapper.sh

# Build classpath with OUR SLF4J first
CP="build/classes/java/main:build/classes/java/test"
CP="$CP:libs/gauge/*"  # Our dependencies (includes slf4j-api-2.x)

# Set Java options to prefer user classpath
export JAVA_OPTS="-cp $CP"

# Run gauge
gauge "$@"
```

**Use:**
```bash
chmod +x gauge-wrapper.sh
./gauge-wrapper.sh run specs
```

**Issue:** Gauge may still load its plugin JARs first. This is why Solution 1 is better.

---

## Understanding Classloading

```
┌─────────────────────────────────────────────────────┐
│ Current (Broken) Classpath Order:                   │
├─────────────────────────────────────────────────────┤
│ 1. Gauge Plugin JARs                                │
│    └── slf4j-api-1.7.32.jar  ❌ (old, loaded first) │
│                                                      │
│ 2. gauge_additional_libs (libs/gauge/*)             │
│    └── slf4j-api-2.x.jar  ⏭️ (new, but too late!)   │
│                                                      │
│ 3. Our classes                                      │
└─────────────────────────────────────────────────────┘
         ↓
    CONFLICT! SLF4J already initialized with old version
```

```
┌─────────────────────────────────────────────────────┐
│ After Fix (Working) Classpath:                      │
├─────────────────────────────────────────────────────┤
│ 1. Gauge Plugin JARs                                │
│    └── slf4j-api-1.7.32.jar.disabled  🚫 (ignored)  │
│                                                      │
│ 2. gauge_additional_libs (libs/gauge/*)             │
│    └── slf4j-api-2.x.jar  ✅ (loads first!)         │
│                                                      │
│ 3. Our classes                                      │
│    └── Uses SLF4J 2.x + Logback ✅                  │
└─────────────────────────────────────────────────────┘
         ↓
    SUCCESS! Spring Boot can initialize Logback
```

---

## Manual Fix (If Scripts Don't Work)

```bash
# 1. Find Gauge's SLF4J
find ~/.gauge/plugins -name "slf4j-api-*.jar"

# Example output:
# /Users/deryncullen/.gauge/plugins/java/0.13.0/libs/slf4j-api-1.7.32.jar

# 2. Disable it
cd ~/.gauge/plugins/java/0.13.0/libs/
mv slf4j-api-1.7.32.jar slf4j-api-1.7.32.jar.disabled

# 3. Verify our SLF4J exists
ls -la libs/gauge/slf4j-api-*.jar

# 4. Test
gauge run specs
```

---

## Verification

After applying the fix:

```bash
# 1. Check Gauge's SLF4J is disabled
ls -la ~/.gauge/plugins/java/0.13.0/libs/slf4j-api-*

# Should show:
# slf4j-api-1.7.32.jar.disabled  ✅
# slf4j-api-1.7.32.jar.backup     (backup)

# 2. Check our SLF4J exists
ls -la libs/gauge/slf4j-api-*.jar

# Should show:
# slf4j-api-2.0.9.jar  ✅ (or similar 2.x version)

# 3. Test Gauge
gauge run specs

# Should see:
# All 9 scenarios passing! 🎉
```

---

## Troubleshooting

### Still getting the error?

```bash
# 1. Ensure Gauge's SLF4J is really disabled
ls ~/.gauge/plugins/java/*/libs/slf4j-api-*.jar

# If you see any NON-disabled JARs, disable them:
mv <path-to-jar> <path-to-jar>.disabled

# 2. Ensure our SLF4J is present
ls libs/gauge/slf4j-api-*.jar
# If missing: ./gradlew copyGaugeDependencies

# 3. Check Logback is present
ls libs/gauge/logback-*.jar
# Should see: logback-classic and logback-core

# 4. Try with verbose logging
GAUGE_LOG_LEVEL=debug gauge run specs
```

### Different Gauge plugin version?

```bash
# Find your version
gauge version

# Find the libs directory
ls ~/.gauge/plugins/java/*/libs/

# Disable SLF4J in that directory
cd ~/.gauge/plugins/java/<VERSION>/libs/
mv slf4j-api-*.jar slf4j-api-*.jar.disabled
```

### Want to undo?

```bash
# Restore Gauge's SLF4J
cd ~/.gauge/plugins/java/0.13.0/libs/
mv slf4j-api-1.7.32.jar.disabled slf4j-api-1.7.32.jar

# Or from backup
cp slf4j-api-1.7.32.jar.backup slf4j-api-1.7.32.jar
```

---

## Why This Happens

This is a common issue when mixing:
- **Old tools** (Gauge Java plugin with SLF4J 1.7.x)
- **New frameworks** (Spring Boot 3.x with SLF4J 2.x)

The Java ClassLoader loads JARs in this order:
1. Plugin/System JARs (Gauge's)
2. Application JARs (yours)

Once SLF4J is loaded, it can't be replaced. So Gauge's old version "wins" and breaks Spring Boot.

---

## Permanent Solution

File a Gauge Java plugin issue asking them to:
1. Update to SLF4J 2.x
2. Or exclude SLF4J from the plugin (let users provide it)

Until then, disabling their SLF4J is the cleanest workaround.

---

## Summary

**Problem**: Gauge plugin has old SLF4J that conflicts with Spring Boot 3.x
**Solution**: Disable Gauge's SLF4J, use yours instead
**Command**: `./disable-gauge-slf4j.sh`
**Result**: All tests pass! 🎉

The nuclear option is clean, reversible, and works immediately!
