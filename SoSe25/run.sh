#!/bin/zsh

# Navigate to the root project directory (parent of SoSe25)
cd "$(dirname "$0")/.."

echo "Building project..."
mvn compile

echo "Starting User Story Quality Analysis Application..."
echo "Using Java version:"
java --version

# Use Maven to run the JavaFX application with proper module configuration
echo "Launching application using Maven JavaFX plugin..."
mvn javafx:run
