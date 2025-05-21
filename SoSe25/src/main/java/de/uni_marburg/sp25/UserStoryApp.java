package de.uni_marburg.sp25;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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

        Label label = new Label("Load User Stories from a file:");
        Button loadButton = new Button("Load TXT File");
        Button loadJsonButton = new Button("Load JSON File");
        Button clearButton = new Button("Clear Output");

        HBox buttonBox = new HBox(10, loadButton, loadJsonButton, clearButton);
        buttonBox.setSpacing(10);

        TextArea txtOutputArea = new TextArea();
        txtOutputArea.setEditable(false);
        txtOutputArea.setPromptText("TXT User stories will appear here...");
        txtOutputArea.setPrefWidth(1000);
        txtOutputArea.setPrefHeight(500);

        TextArea jsonOutputArea = new TextArea();
        jsonOutputArea.setEditable(false);
        jsonOutputArea.setPromptText("JSON User stories will appear here...");
        jsonOutputArea.setPrefWidth(1000);
        jsonOutputArea.setPrefHeight(500);

        VBox outputBox = new VBox(10, txtOutputArea, jsonOutputArea);
        outputBox.setSpacing(15);
        outputBox.setPadding(new Insets(10));

        loadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select TXT File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    List<UserStory> userStories = manager.readUserStories(selectedFile.getAbsolutePath());
                    txtOutputArea.appendText("File: " + selectedFile.getName() + "\n");
                    for (UserStory story : userStories) {
                        String[] lines = story.toString().split("\\r?\\n");
                        for (String line : lines) {
                            txtOutputArea.appendText(line.trim() + "\n");
                        }
                        txtOutputArea.appendText("\n");
                    }
                } catch (IOException e) {
                    showError("Error loading TXT file: " + e.getMessage());
                }
            }
        });

        loadJsonButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select JSON File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    List<UserStory> userStories = manager.readUserStories(selectedFile.getAbsolutePath());
                    jsonOutputArea.appendText("File: " + selectedFile.getName() + "\n");
                    for (UserStory story : userStories) {
                        String[] lines = story.toString().split("\\r?\\n");
                        for (String line : lines) {
                            jsonOutputArea.appendText(line.trim() + "\n");
                        }
                        jsonOutputArea.appendText("\n");
                    }
                } catch (IOException e) {
                    showError("Error loading JSON file: " + e.getMessage());
                }
            }
        });

        clearButton.setOnAction(event -> {
            txtOutputArea.clear();
            jsonOutputArea.clear();
        });

        changeLanguageButton.setOnAction(event -> {
            String selectedLanguage = languageSelector.getValue();
            if ("Deutsch".equals(selectedLanguage)) {
                primaryStage.setTitle("Benutzerstory-Manager");
                fileMenu.setText("Datei");
                exitItem.setText("Beenden");
                label.setText("Lade Benutzerstories aus einer Datei:");
                loadButton.setText("TXT-Datei laden");
                loadJsonButton.setText("JSON-Datei laden");
                clearButton.setText("Ausgabe löschen");
                txtOutputArea.setPromptText("TXT-Benutzerstories werden hier angezeigt...");
                jsonOutputArea.setPromptText("JSON-Benutzerstories werden hier angezeigt...");
                changeLanguageButton.setText("Sprache ändern");
            } else {
                primaryStage.setTitle("User Story Manager");
                fileMenu.setText("File");
                exitItem.setText("Exit");
                label.setText("Load User Stories from a file:");
                loadButton.setText("Load TXT File");
                loadJsonButton.setText("Load JSON File");
                clearButton.setText("Clear Output");
                txtOutputArea.setPromptText("TXT User stories will appear here...");
                jsonOutputArea.setPromptText("JSON User stories will appear here...");
                changeLanguageButton.setText("Change Language");
            }
        });

        Button convertToJsonButton = new Button("Convert to JSON");
        convertToJsonButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select TXT File to Convert");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    List<UserStory> userStories = manager.readUserStories(selectedFile.getAbsolutePath());
                    FileChooser saveFileChooser = new FileChooser();
                    saveFileChooser.setTitle("Save JSON File");
                    saveFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                    File saveFile = saveFileChooser.showSaveDialog(primaryStage);

                    if (saveFile != null) {
                        manager.saveToJson(userStories, saveFile.getAbsolutePath());
                        showInfo("File successfully converted to JSON and saved.");
                    }
                } catch (IOException e) {
                    showError("Error converting TXT to JSON: " + e.getMessage());
                }
            }
        });

        HBox bottomBox = new HBox(convertToJsonButton);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setSpacing(10);
        bottomBox.setAlignment(Pos.BOTTOM_LEFT);

        HBox languageBox = new HBox(10, languageLabel, languageSelector, changeLanguageButton);

        VBox centerLayout = new VBox(10, languageBox, label, buttonBox, outputBox);
        centerLayout.setSpacing(15);
        centerLayout.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(centerLayout);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1000, 800);
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}