package de.uni_marburg.sp25;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A JavaFX application for managing user stories.
 * Provides functionality to load user stories from a file, display them, and clear the output.
 * Also supports language switching between English and German.
 */
public class UserStoryApp extends Application {

    private UserStoryManager manager = new UserStoryManager();

    /**
     * Starts the JavaFX application.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("User Story Manager");

        // Menu Bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);

        // Language Selection
        Label languageLabel = new Label("Language:");
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("English", "Deutsch");
        languageSelector.setValue("English");
        Button changeLanguageButton = new Button("Change Language");

        // File Loading Section
        Label label = new Label("Load User Stories from a file:");
        Button loadButton = new Button("Load File");
        Button clearButton = new Button("Clear Output");
        HBox buttonBox = new HBox(10, loadButton, clearButton);
        buttonBox.setSpacing(10);

        // File Conversion Section
        Button convertButton = new Button("Convert to JSON");

        // Output Area
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("User stories will appear here...");
        outputArea.setPrefWidth(800); // Set the preferred width
        outputArea.setPrefHeight(400); // Set the preferred height

        // Language Change Action
        changeLanguageButton.setOnAction(event -> {
            String selectedLanguage = languageSelector.getValue();
            if ("Deutsch".equals(selectedLanguage)) {
                primaryStage.setTitle("User Story Manager");
                fileMenu.setText("Datei");
                exitItem.setText("Beenden");
                label.setText("Lade User Stories aus einer Datei:");
                loadButton.setText("Datei laden");
                clearButton.setText("Ausgabe löschen");
                outputArea.setPromptText("User Stories werden hier angezeigt...");
                convertButton.setText("In JSON umwandeln");
                changeLanguageButton.setText("Sprache ändern");
            } else {
                primaryStage.setTitle("User Story Manager");
                fileMenu.setText("File");
                exitItem.setText("Exit");
                label.setText("Load User Stories from a file:");
                loadButton.setText("Load File");
                clearButton.setText("Clear Output");
                outputArea.setPromptText("User stories will appear here...");
                convertButton.setText("Convert to JSON");
                changeLanguageButton.setText("Change Language");
            }
        });

        // Change to JSON

        // Button Actions
        convertButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select User Stories File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    // Read user stories from the file
                    List<UserStory> userStories = manager.readUserStories(file.getAbsolutePath());

                    // Save user stories to a JSON file
                    FileChooser saveChooser = new FileChooser();
                    saveChooser.setTitle("Save as JSON");
                    saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                    File saveFile = saveChooser.showSaveDialog(primaryStage);

                    if (saveFile != null) {
                        manager.saveToJson(userStories, saveFile.getAbsolutePath());
                        showInfo("Conversion successful! JSON saved to: " + saveFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    showError("Error during conversion: " + e.getMessage());
                }
            }
        });

        // Layout for Language Selection
        HBox languageBox = new HBox(10, languageLabel, languageSelector, changeLanguageButton);

        // Button Actions
        loadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open User Stories File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            // Set an initial directory (optional)
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            // Open the file chooser
            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    List<UserStory> userStories = manager.readUserStories(file.getAbsolutePath());
                    StringBuilder currentContent = new StringBuilder(outputArea.getText());
                    currentContent.append("File: ").append(file.getName()).append("\n");
                    currentContent.append(userStories.toString()).append("\n\n");
                    outputArea.setText(currentContent.toString());
                } catch (IOException e) {
                    showError("Error reading file: " + file.getName() + " - " + e.getMessage());
                }
            }
        });

        clearButton.setOnAction(event -> outputArea.clear());

        // Layout
        VBox layout = new VBox(10, menuBar, languageBox, label, buttonBox,convertButton,outputArea);
        layout.setSpacing(15);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Displays an error message in an alert dialog.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an informational message in an alert dialog.
     *
     * @param message the informational message to display
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * The main entry point for the application.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}