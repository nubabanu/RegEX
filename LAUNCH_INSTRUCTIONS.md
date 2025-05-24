# User Story Quality Analysis Application - Launch Instructions

## Overview
This document provides multiple ways to launch the User Story Quality Analysis JavaFX application.

## Prerequisites
- Java 24 or higher
- Maven 3.6+
- JavaFX dependencies (managed automatically by Maven)

## Launch Methods

### Method 1: VS Code Task (Recommended for VS Code users)
If you're using VS Code, use the built-in task:
1. Open Command Palette (Cmd+Shift+P)
2. Type "Tasks: Run Task"
3. Select "Run JavaFX Application"

### Method 2: Maven JavaFX Plugin (Recommended)
From the project root directory:
```bash
mvn javafx:run
```

### Method 3: Shell Scripts
From the SoSe25 directory:

**Using run.sh:**
```bash
cd SoSe25
./run.sh
```

**Using launch.sh:**
```bash
cd SoSe25
./launch.sh
```

Both scripts will:
1. Navigate to the project root
2. Compile the project
3. Launch the application using Maven JavaFX plugin

## Recent Fixes Applied

### JSON Prefix Issue ✅ RESOLVED
- **Problem**: Saved JSON files contained unwanted prefix "JSON Representation of user_stories_testset.txt:\n\n"
- **Solution**: Modified the `saveJsonToFile` method in `UserStoryApp.java` to use clean JSON output
- **Testing**: Added comprehensive test `saveToJson_producesCleanJsonWithoutPrefix()` 
- **Status**: All 86 tests passing, clean JSON output verified

### Run Script Issues ✅ RESOLVED
- **Problem**: Manual classpath management and JavaFX module configuration issues
- **Solution**: Simplified scripts to use Maven JavaFX plugin instead of manual Java execution
- **Status**: Scripts now properly launch the application

## Application Features
- Load user stories from text files
- Analyze user story quality across multiple dimensions
- Export results to clean JSON format (without prefixes)
- Interactive JavaFX GUI with modern MaterialFX components

## Troubleshooting
If you encounter issues:
1. Ensure Java 24 is installed and set as JAVA_HOME
2. Run `mvn clean compile` to ensure project is built
3. Check that JavaFX dependencies are properly downloaded
4. Use the Maven approach (`mvn javafx:run`) as it handles all module configurations automatically

## Testing
Run the full test suite to verify functionality:
```bash
mvn test
```

Expected result: 86 tests passing, including the new JSON prefix fix test.
