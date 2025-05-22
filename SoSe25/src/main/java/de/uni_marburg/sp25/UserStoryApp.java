package de.uni_marburg.sp25;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.util.Locale;
import java.util.ResourceBundle;

public class UserStoryApp extends Application {

    private UserStoryManager manager = new UserStoryManager();
    private TextArea txtOutputArea = new TextArea();
    private TextArea jsonOutputArea = new TextArea();
    // private Label appTitleLabel; // Removed as it was unused, stage title is set directly
    private Menu fileMenu;
    private MenuItem exitItem;
    private Label loadFileLabel;
    private Button loadTxtButton;
    private Button loadJsonButton;
    private Button clearButton;
    private Button convertToJsonButton;
    private Button saveJsonButton;
    private ComboBox<String> languageSelector;
    private Label languageLabelText;
    private Button changeLanguageButton;

    private ResourceBundle messages;

    @Override
    public void start(Stage primaryStage) {
        loadResourceBundle(Locale.ENGLISH);

        primaryStage.setTitle(messages.getString("app.title"));

        MenuBar menuBar = new MenuBar();
        fileMenu = new Menu(messages.getString("menu.file"));
        exitItem = new MenuItem(messages.getString("menu.file.exit"));
        exitItem.setOnAction(_e -> primaryStage.close()); // Use _e for unused lambda parameter
        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);

        languageLabelText = new Label(messages.getString("label.language"));
        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("English", "Deutsch");
        languageSelector.setValue("English");

        changeLanguageButton = new Button(messages.getString("button.changeLanguage"));
        changeLanguageButton.setOnAction(_event -> { // Use _event for unused lambda parameter
            String selectedLanguage = languageSelector.getValue();
            if ("Deutsch".equals(selectedLanguage)) {
                loadResourceBundle(Locale.GERMAN);
            } else {
                loadResourceBundle(Locale.ENGLISH);
            }
            updateUIText(primaryStage);
        });

        HBox languageBox = new HBox(10, languageLabelText, languageSelector, changeLanguageButton);
        languageBox.setAlignment(Pos.CENTER_RIGHT);
        languageBox.setPadding(new Insets(5));

        loadFileLabel = new Label(messages.getString("label.loadUserStories"));
        loadTxtButton = new Button(messages.getString("button.loadTxt"));
        loadJsonButton = new Button(messages.getString("button.loadJson"));
        clearButton = new Button(messages.getString("button.clearOutput"));
        convertToJsonButton = new Button(messages.getString("button.convertToJson"));
        saveJsonButton = new Button(messages.getString("button.saveJson"));

        HBox fileOperationsBox = new HBox(10, loadTxtButton, loadJsonButton, convertToJsonButton, saveJsonButton, clearButton);
        fileOperationsBox.setPadding(new Insets(10,0,10,0));

        txtOutputArea.setEditable(false);
        txtOutputArea.setPromptText(messages.getString("prompt.txtOutputArea"));
        txtOutputArea.setPrefHeight(300);

        jsonOutputArea.setEditable(false);
        jsonOutputArea.setPromptText(messages.getString("prompt.jsonOutputArea"));
        jsonOutputArea.setPrefHeight(300);

        VBox outputDisplayBox = new VBox(10, new Label(messages.getString("label.txtContent")), txtOutputArea, new Label(messages.getString("label.jsonContent")), jsonOutputArea);

        loadTxtButton.setOnAction(_event -> loadFile(primaryStage, "txt")); // Use _event
        loadJsonButton.setOnAction(_event -> loadFile(primaryStage, "json")); // Use _event
        clearButton.setOnAction(_event -> { // Use _event
            txtOutputArea.clear();
            jsonOutputArea.clear();
        });
        convertToJsonButton.setOnAction(_event -> convertTxtToJson(primaryStage)); // Use _event
        saveJsonButton.setOnAction(_event -> saveJsonToFile(primaryStage)); // Use _event

        VBox mainLayout = new VBox(10, languageBox, loadFileLabel, fileOperationsBox, outputDisplayBox);
        mainLayout.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainLayout);

        Scene scene = new Scene(root, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadFile(Stage ownerStage, String type) {
        FileChooser fileChooser = new FileChooser();
        String title = type.equals("txt") ? messages.getString("fileChooser.selectTxt") : messages.getString("fileChooser.selectJson");
        String extensionDescription = type.equals("txt") ? messages.getString("fileChooser.txtFiles") : messages.getString("fileChooser.jsonFiles");
        String extension = "*." + type;

        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, extension));
        File selectedFile = fileChooser.showOpenDialog(ownerStage);

        if (selectedFile != null) {
            try {
                List<UserStory> userStories = manager.readUserStories(selectedFile.getAbsolutePath());
                TextArea targetArea = type.equals("txt") ? txtOutputArea : jsonOutputArea;
                targetArea.clear();
                targetArea.appendText(messages.getString("label.file") + ": " + selectedFile.getName() + "\n\n");
                for (UserStory story : userStories) {
                    targetArea.appendText(story.toString() + "\n\n");
                }
                if (type.equals("txt")) {
                    displayUserStoriesAsJson(userStories, selectedFile.getName());
                }
            } catch (IOException e) {
                showError(messages.getString("error.loadingFile") + ": " + e.getMessage());
            }
        }
    }

    private void displayUserStoriesAsJson(List<UserStory> userStories, String originalFileName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String jsonString = mapper.writeValueAsString(userStories);
            jsonOutputArea.clear();
            jsonOutputArea.appendText(messages.getString("label.jsonRepresentationOf") + " " + originalFileName + ":\n\n");
            jsonOutputArea.appendText(jsonString);
        } catch (IOException e) {
            showError(messages.getString("error.convertingToJsonPreview") + ": " + e.getMessage());
        }
    }

    private void convertTxtToJson(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(messages.getString("fileChooser.selectTxtConvert"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(messages.getString("fileChooser.txtFiles"), "*.txt"));
        File txtFile = fileChooser.showOpenDialog(ownerStage);

        if (txtFile != null) {
            FileChooser saveFileChooser = new FileChooser();
            saveFileChooser.setTitle(messages.getString("fileChooser.saveJson"));
            saveFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(messages.getString("fileChooser.jsonFiles"), "*.json"));
            String initialOutputName = txtFile.getName().replaceFirst("[.][^.]+$", "") + ".json";
            saveFileChooser.setInitialFileName(initialOutputName);
            File jsonFile = saveFileChooser.showSaveDialog(ownerStage);

            if (jsonFile != null) {
                try {
                    List<UserStory> userStories = manager.readUserStories(txtFile.getAbsolutePath());
                    manager.saveToJson(userStories, jsonFile.getAbsolutePath());
                    showInfo(messages.getString("info.fileConvertedAndSaved"));
                    displayUserStoriesAsJson(userStories, jsonFile.getName());
                } catch (IOException e) {
                    showError(messages.getString("error.convertingTxtToJson") + ": " + e.getMessage());
                }
            }
        }
    }

    private void saveJsonToFile(Stage ownerStage) {
        String jsonContent = jsonOutputArea.getText();
        if (jsonContent.isEmpty() || jsonContent.equals(messages.getString("prompt.jsonOutputArea"))) {
            showInfo(messages.getString("info.noJsonToSave"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(messages.getString("fileChooser.saveJsonContent"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(messages.getString("fileChooser.jsonFiles"), "*.json"));
        fileChooser.setInitialFileName("user_stories.json");
        File file = fileChooser.showSaveDialog(ownerStage);

        if (file != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<UserStory> storiesToSave = mapper.readValue(jsonContent, new TypeReference<List<UserStory>>() {});
                manager.saveToJson(storiesToSave, file.getAbsolutePath());
                showInfo(messages.getString("info.jsonSavedSuccessfully"));
            } catch (IOException e) {
                showError(messages.getString("error.savingJsonFile") + ": " + e.getMessage());
            }
        }
    }

    private void loadResourceBundle(Locale locale) {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", locale);
    }

    private void updateUIText(Stage stage) {
        stage.setTitle(messages.getString("app.title"));
        fileMenu.setText(messages.getString("menu.file"));
        exitItem.setText(messages.getString("menu.file.exit"));
        languageLabelText.setText(messages.getString("label.language"));
        changeLanguageButton.setText(messages.getString("button.changeLanguage"));
        loadFileLabel.setText(messages.getString("label.loadUserStories"));
        loadTxtButton.setText(messages.getString("button.loadTxt"));
        loadJsonButton.setText(messages.getString("button.loadJson"));
        clearButton.setText(messages.getString("button.clearOutput"));
        convertToJsonButton.setText(messages.getString("button.convertToJson"));
        saveJsonButton.setText(messages.getString("button.saveJson"));
        txtOutputArea.setPromptText(messages.getString("prompt.txtOutputArea"));
        jsonOutputArea.setPromptText(messages.getString("prompt.jsonOutputArea"));

        // Safely update labels for output areas by checking parent and children types
        if (txtOutputArea.getParent() instanceof VBox) {
            VBox parentVBox = (VBox) txtOutputArea.getParent();
            if (!parentVBox.getChildren().isEmpty() && parentVBox.getChildren().get(0) instanceof Label) {
                ((Label) parentVBox.getChildren().get(0)).setText(messages.getString("label.txtContent"));
            }
            if (parentVBox.getChildren().size() > 2 && parentVBox.getChildren().get(2) instanceof Label) {
                 ((Label) parentVBox.getChildren().get(2)).setText(messages.getString("label.jsonContent"));
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(messages.getString("dialog.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(messages.getString("dialog.info.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}