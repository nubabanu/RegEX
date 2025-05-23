#!/bin/zsh

# Navigate to the project directory
cd "$(dirname "$0")"

# Ensure the project is built
echo "Building project..."
mvn compile

# Find JavaFX jars specifically for macOS (aarch64 or x64)
JAVAFX_MODULES=$(find ~/.m2/repository/org/openjfx -name "*.jar" | grep -v javadoc | grep -v sources | grep -e "mac" | tr '\n' ':')

# Add Jackson and other dependencies
DEPS=$(find ~/.m2/repository -name "jackson-*.jar" | grep -v javadoc | grep -v sources | tr '\n' ':')
CLASSPATH="target/classes:$DEPS"

echo "Starting application..."
# Run the application with JavaFX modules
java --module-path "$JAVAFX_MODULES" \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=ALL-UNNAMED \
     -cp "$CLASSPATH" \
     de.uni_marburg.sp25.UserStoryApp
