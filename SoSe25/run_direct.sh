#!/bin/zsh

# Navigate to the root project directory (parent of SoSe25)
cd "$(dirname "$0")/.."

echo "Building project..."
mvn compile

echo "Getting Maven classpath..."
MAVEN_CLASSPATH=$(mvn -q exec:exec -Dexec.executable=echo -Dexec.args='%classpath')

echo "Starting User Story Quality Analysis Application (Direct Launch)..."
echo "Using Java version:"
java --version

# Build module path for JavaFX (just the core JavaFX JARs)
MODULE_PATH=""
for path in \
    ~/.m2/repository/org/openjfx/javafx-controls/24.0.1/javafx-controls-24.0.1.jar \
    ~/.m2/repository/org/openjfx/javafx-graphics/24.0.1/javafx-graphics-24.0.1.jar \
    ~/.m2/repository/org/openjfx/javafx-fxml/24.0.1/javafx-fxml-24.0.1.jar \
    ~/.m2/repository/org/openjfx/javafx-base/24.0.1/javafx-base-24.0.1.jar; do
    if [ -f "$path" ]; then
        if [ -z "$MODULE_PATH" ]; then
            MODULE_PATH="$path"
        else
            MODULE_PATH="$MODULE_PATH:$path"
        fi
    fi
done

echo "Launching with direct Java command..."
echo "Module path: $MODULE_PATH"
echo "Classpath length: ${#MAVEN_CLASSPATH} characters"

# Launch with proper JavaFX module configuration
java \
    --module-path "$MODULE_PATH" \
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
    -cp "$MAVEN_CLASSPATH" \
    de.uni_marburg.sp25.UserStoryApp
