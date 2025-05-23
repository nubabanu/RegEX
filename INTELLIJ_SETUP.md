## IntelliJ IDEA Setup Instructions

This project can be easily imported and run in IntelliJ IDEA. Follow these steps to get started:

### Importing the Project

1. Open IntelliJ IDEA
2. Select **File > Open**
3. Navigate to the project directory and select the root folder `sp25_gruppe6_fshaye_geilmann_manafov`
4. Choose to open as a **Project** (not a directory or a new window)
5. IntelliJ should detect that this is a Maven project and will start importing the dependencies

### Running the Application

There are multiple ways to run the application in IntelliJ:

#### Method 1: Using Maven Run Configuration (Recommended)

1. In the top menu, select **Run > Edit Configurations**
2. Click the **+** button and select **Maven**
3. Set the following:
   - Name: `JavaFX Run`
   - Working directory: `/Users/zuhalsicim/sp25_gruppe6_fshaye_geilmann_manafov/SoSe25`
   - Command line: `javafx:run`
4. Click **OK**
5. Now you can run the application by selecting **Run > Run 'JavaFX Run'**

#### Method 2: Using the Main Class

1. Open the `UserStoryApp.java` file
2. Right-click in the editor and select **Run 'UserStoryApp.main()'**
3. The first time, it may fail because of missing VM options
4. Go to **Run > Edit Configurations**
5. Edit the automatically created configuration and add the following VM options:
   ```
   --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml --enable-native-access=ALL-UNNAMED
   ```
6. You'll need to define the `PATH_TO_FX` variable in IntelliJ, pointing to your JavaFX SDK location

#### Method 3: Using the Terminal Tool Within IntelliJ

1. Open the Terminal tool in IntelliJ (usually at the bottom of the window)
2. Navigate to the project directory: `cd SoSe25`
3. Run the script: `./launch.sh`

### Running Tests

1. In the Project view, right-click on the `src/test/java` directory
2. Select **Run 'All Tests'**
3. IntelliJ will automatically configure and run all the JUnit tests

### Troubleshooting

- If you encounter any issues with JavaFX dependencies, make sure you have the PATH_TO_FX environment variable set correctly
- If the Maven import fails, try refreshing the Maven project by right-clicking on the project and selecting **Maven > Reload Project**

### Notes

- The project uses Java 24, ensure you have JDK 24 configured in IntelliJ
- The provided .idea configuration files should set up most things automatically
