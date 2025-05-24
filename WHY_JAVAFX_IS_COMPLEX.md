# Why JavaFX Can't Be Launched Simply with `java -cp`

## The Core Problem

JavaFX applications can't be launched with a simple `java -cp MyApp.jar MainClass` command due to several architectural changes that happened when JavaFX was separated from the JDK.

## Historical Context

### Pre-Java 11: JavaFX was bundled with JDK
- Simple launch: `java -cp myapp.jar MyMainClass`
- JavaFX was part of the core JDK modules

### Post-Java 11: JavaFX became a separate project
- JavaFX modules must be explicitly managed
- Platform-specific native libraries required
- Module system (JPMS) enforcement

## Technical Challenges

### 1. **Module System Requirements**
JavaFX uses the Java Platform Module System (JPMS). Your application requires these modules:
- `javafx.controls`
- `javafx.fxml` 
- `javafx.graphics`
- `javafx.base`

These must be explicitly added to the module path, not just the classpath.

### 2. **Platform-Specific Native Libraries**
Looking at your project's dependencies, JavaFX requires platform-specific JARs:

```
javafx-controls-24.0.1.jar              (pure Java)
javafx-controls-24.0.1-mac-aarch64.jar  (macOS ARM64 native)
javafx-graphics-24.0.1.jar              (pure Java)  
javafx-graphics-24.0.1-mac-aarch64.jar  (macOS ARM64 native)
```

The native JARs contain:
- Platform-specific rendering libraries
- Font handling systems
- Window management code
- Hardware acceleration drivers

### 3. **Internal API Access**
Your application (via MaterialFX) needs access to JavaFX internal APIs:
```bash
--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED
--add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED
# ... and many more
```

### 4. **Memory Management**
JavaFX uses unsafe memory operations that require special permissions:
```bash
--add-opens java.base/sun.misc=ALL-UNNAMED
--enable-native-access=javafx.graphics
```

## What Makes the Maven Plugin Special

The JavaFX Maven plugin (`mvn javafx:run`) automatically handles:

1. **Classpath Construction**: Builds the complete classpath with all dependencies
2. **Module Path Setup**: Configures `--module-path` with JavaFX modules
3. **Module Addition**: Adds `--add-modules` for required JavaFX modules  
4. **Export Configuration**: Sets up all required `--add-exports` flags
5. **Native Access**: Configures `--enable-native-access` permissions
6. **Platform Detection**: Automatically includes the correct platform-specific JARs

## Comparison: Simple vs Complex Launch

### ❌ **What Doesn't Work** (Simple approach):
```bash
java -cp "target/classes:~/.m2/repository/org/openjfx/javafx-controls/24.0.1/javafx-controls-24.0.1.jar" de.uni_marburg.sp25.UserStoryApp
```
**Error**: "JavaFX runtime components are missing"

### ✅ **What Works** (Complex approach):
```bash
java \
    --module-path "javafx-controls.jar:javafx-graphics.jar:javafx-fxml.jar:javafx-base.jar" \
    --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
    --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
    --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
    --add-exports javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED \
    --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED \
    --add-exports javafx.base/com.sun.javafx.collections=ALL-UNNAMED \
    --add-exports javafx.base/com.sun.javafx.runtime=ALL-UNNAMED \
    --add-exports javafx.graphics/com.sun.javafx.util=ALL-UNNAMED \
    --add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
    --add-exports javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED \
    --add-opens java.base/sun.misc=ALL-UNNAMED \
    --enable-native-access=javafx.graphics \
    -cp "target/classes:all_other_dependencies.jar" \
    de.uni_marburg.sp25.UserStoryApp
```

## Available Launch Methods for Your Project

### 1. **Maven Plugin** (Recommended - Easiest)
```bash
mvn javafx:run
```

### 2. **Direct Java Command** (Complex but Educational)
```bash
./run_direct.sh  # Uses the complex command above
```

### 3. **VS Code Task** (Recommended for Development)
- Command Palette → "Run Task" → "Run JavaFX Application"

### 4. **Simple Scripts** (Delegates to Maven)
```bash
./run.sh        # Calls mvn javafx:run
./launch.sh     # Calls mvn javafx:run
```

## Why We Recommend Maven Plugin

1. **Maintenance**: Maven automatically handles dependency updates
2. **Platform Independence**: Works on Windows, macOS, Linux
3. **Simplicity**: One command handles all complexity
4. **Reliability**: Tested and maintained by the JavaFX team
5. **IDE Integration**: Works seamlessly with IDEs

## Summary

JavaFX applications require complex launch configurations due to:
- Module system requirements
- Platform-specific native libraries  
- Internal API access needs
- Memory management permissions

The Maven JavaFX plugin exists specifically to handle this complexity, which is why it's the recommended approach for running JavaFX applications.
