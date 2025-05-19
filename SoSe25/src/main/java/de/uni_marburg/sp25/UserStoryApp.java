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

public class UserStoryApp extends Application {

    private UserStoryManager manager = new UserStoryManager();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("User Story Manager");

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);

        Label languageLabel = new Label("Language:");
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("English", "Deutsch");
        languageSelector.setValue("English");
        Button changeLanguageButton = new Button("Change Language");
        Button convertButton = new Button("Convert to JSON");

        Label label = new Label("Load User Stories from a file:");
        Button loadButton = new Button("Load File");
        Button loadJsonButton = new Button("Load JSON File");
        Button clearButton = new Button("Clear Output");
        HBox buttonBox = new HBox(10, loadButton, loadJsonButton, clearButton);
        buttonBox.setSpacing(10);

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("User stories will appear here...");
        outputArea.setPrefWidth(800);
        outputArea.setPrefHeight(400);

        loadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open User Stories File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    List<UserStory> userStories = manager.readUserStories(file.getAbsolutePath());
                    StringBuilder currentContent = new StringBuilder(outputArea.getText());
                    currentContent.append("File: ").append(file.getName()).append("\n");
                    for (UserStory userStory : userStories) {
                        currentContent.append(userStory.toString()).append("\n");
                    }
                    currentContent.append("\n");
                    outputArea.setText(currentContent.toString());
                } catch (IOException e) {
                    showError("Error reading file: " + file.getName() + " - " + e.getMessage());
                }
            }
        });

        loadJsonButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open JSON File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    List<UserStory> userStories = manager.readUserStories(file.getAbsolutePath());
                    StringBuilder currentContent = new StringBuilder(outputArea.getText());
                    currentContent.append("File: ").append(file.getName()).append("\n");
                    for (UserStory userStory : userStories) {
                        currentContent.append(userStory.toString()).append("\n");
                    }
                    currentContent.append("\n");
                    outputArea.setText(currentContent.toString());
                } catch (IOException e) {
                    showError("Error reading file: " + file.getName() + " - " + e.getMessage());
                }
            }
        });

        clearButton.setOnAction(event -> outputArea.clear());

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

        HBox languageBox = new HBox(10, languageLabel, languageSelector, changeLanguageButton);

        VBox layout = new VBox(10, menuBar, languageBox, label, buttonBox, outputArea);
        layout.setSpacing(15);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}