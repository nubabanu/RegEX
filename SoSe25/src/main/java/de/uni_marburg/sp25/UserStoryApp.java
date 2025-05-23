package de.uni_marburg.sp25;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.uni_marburg.sp25.quality.QualityAnalysisManager;
import de.uni_marburg.sp25.quality.QualityAnalysisResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class UserStoryApp extends Application {

    private UserStoryManager manager = new UserStoryManager();
    private QualityAnalysisManager qualityManager;
    private TextArea txtOutputArea = new TextArea();
    private TextArea jsonOutputArea = new TextArea();
    private TextArea warningsArea = new TextArea();
    private TextArea qualityResultsArea = new TextArea();
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
    private Label warningsLabel;
    
    // Quality analysis components
    private Label qualityAnalysisLabel;
    private Button analyzeQualityButton;
    private Button exportReportButton;
    private ListView<UserStory> userStoriesListView;
    private VBox qualityCriteriaBox;
    private List<CheckBox> criteriaCheckBoxes = new ArrayList<>();
    private Label qualityResultsLabel;
    private Label userStoriesLabel;
    private Label criteriaLabel;
    
    private List<UserStory> currentUserStories = new ArrayList<>();
    private QualityAnalysisResult lastAnalysisResult;
    private String currentFileName = "";

    private ResourceBundle messages;

    @Override
    public void start(Stage primaryStage) {
        loadResourceBundle(Locale.ENGLISH);
        qualityManager = new QualityAnalysisManager(messages);

        primaryStage.setTitle(messages.getString("app.title"));

        MenuBar menuBar = new MenuBar();
        fileMenu = new Menu(messages.getString("menu.file"));
        exitItem = new MenuItem(messages.getString("menu.file.exit"));
        exitItem.setOnAction(_e -> primaryStage.close());
        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);

        languageLabelText = new Label(messages.getString("label.language"));
        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("English", "Deutsch");
        languageSelector.setValue("English");

        changeLanguageButton = new Button(messages.getString("button.changeLanguage"));
        changeLanguageButton.setOnAction(_event -> {
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

        // Create tabs
        TabPane tabPane = new TabPane();
        
        // Tab 1: File Operations
        Tab fileOperationsTab = new Tab("File Operations");
        fileOperationsTab.setClosable(false);
        
        VBox fileOperationsContent = createFileOperationsTab();
        fileOperationsTab.setContent(new ScrollPane(fileOperationsContent));
        
        // Tab 2: Quality Analysis
        Tab qualityAnalysisTab = new Tab("Quality Analysis");
        qualityAnalysisTab.setClosable(false);
        
        VBox qualityAnalysisContent = createQualityAnalysisTab();
        qualityAnalysisTab.setContent(new ScrollPane(qualityAnalysisContent));
        
        tabPane.getTabs().addAll(fileOperationsTab, qualityAnalysisTab);

        // Event handlers
        setupEventHandlers(primaryStage);

        VBox mainLayout = new VBox(10, languageBox, tabPane);
        mainLayout.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainLayout);

        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createFileOperationsTab() {
        // File operations
        loadFileLabel = new Label(messages.getString("label.loadUserStories"));
        loadTxtButton = new Button(messages.getString("button.loadTxt"));
        loadJsonButton = new Button(messages.getString("button.loadJson"));
        clearButton = new Button(messages.getString("button.clearOutput"));
        convertToJsonButton = new Button(messages.getString("button.convertToJson"));
        saveJsonButton = new Button(messages.getString("button.saveJson"));

        HBox fileOperationsBox = new HBox(10, loadTxtButton, loadJsonButton, convertToJsonButton, saveJsonButton, clearButton);
        fileOperationsBox.setPadding(new Insets(10,0,10,0));

        // Text areas
        txtOutputArea.setEditable(false);
        txtOutputArea.setPromptText(messages.getString("prompt.txtOutputArea"));
        txtOutputArea.setPrefHeight(200);

        jsonOutputArea.setEditable(false);
        jsonOutputArea.setPromptText(messages.getString("prompt.jsonOutputArea"));
        jsonOutputArea.setPrefHeight(200);

        warningsLabel = new Label(messages.getString("label.warnings"));
        warningsArea = new TextArea();
        warningsArea.setEditable(false);
        warningsArea.setPromptText(messages.getString("prompt.warningsArea"));
        warningsArea.setPrefHeight(80);

        VBox outputDisplayBox = new VBox(10, 
            new Label(messages.getString("label.txtContent")), txtOutputArea, 
            new Label(messages.getString("label.jsonContent")), jsonOutputArea,
            warningsLabel, warningsArea);

        return new VBox(10, loadFileLabel, fileOperationsBox, outputDisplayBox);
    }

    private VBox createQualityAnalysisTab() {
        qualityAnalysisLabel = new Label(messages.getString("label.qualityAnalysis"));
        qualityAnalysisLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        analyzeQualityButton = new Button(messages.getString("button.analyzeQuality"));
        exportReportButton = new Button(messages.getString("button.exportReport"));

        HBox qualityOperationsBox = new HBox(10, analyzeQualityButton, exportReportButton);
        qualityOperationsBox.setPadding(new Insets(10,0,10,0));

        // User stories selection
        userStoriesLabel = new Label(messages.getString("label.selectedStories"));
        userStoriesListView = new ListView<>();
        userStoriesListView.setPrefHeight(150);
        userStoriesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userStoriesListView.setCellFactory(listView -> new ListCell<UserStory>() {
            @Override
            protected void updateItem(UserStory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getPid() + ": " + item.getText());
                }
            }
        });

        // Quality criteria selection with checkboxes
        criteriaLabel = new Label(messages.getString("label.qualityCriteria"));
        qualityCriteriaBox = new VBox(5);
        qualityCriteriaBox.setPrefHeight(150);
        qualityCriteriaBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-padding: 5;");
        
        // Initialize quality criteria checkboxes
        updateQualityCriteriaCheckBoxes();

        // Add "Select All" and "Clear All" buttons for criteria
        Button selectAllCriteriaButton = new Button("Select All");
        Button clearAllCriteriaButton = new Button("Clear All");
        
        selectAllCriteriaButton.setOnAction(e -> {
            for (CheckBox checkBox : criteriaCheckBoxes) {
                checkBox.setSelected(true);
            }
        });
        
        clearAllCriteriaButton.setOnAction(e -> {
            for (CheckBox checkBox : criteriaCheckBoxes) {
                checkBox.setSelected(false);
            }
        });
        
        HBox criteriaButtonsBox = new HBox(5, selectAllCriteriaButton, clearAllCriteriaButton);

        // Quality results
        qualityResultsLabel = new Label(messages.getString("label.qualityResults"));
        qualityResultsArea = new TextArea();
        qualityResultsArea.setEditable(false);
        qualityResultsArea.setPromptText(messages.getString("prompt.qualityResultsArea"));
        qualityResultsArea.setPrefHeight(200);

        // Layout for quality analysis
        HBox selectionBox = new HBox(20);
        
        VBox userStoriesBox = new VBox(5, userStoriesLabel, userStoriesListView);
        userStoriesBox.setPrefWidth(400);
        
        VBox criteriaBox = new VBox(5, criteriaLabel, qualityCriteriaBox, criteriaButtonsBox);
        criteriaBox.setPrefWidth(300);
        
        selectionBox.getChildren().addAll(userStoriesBox, criteriaBox);

        return new VBox(10, 
            qualityAnalysisLabel,
            qualityOperationsBox,
            selectionBox,
            qualityResultsLabel, 
            qualityResultsArea
        );
    }

    private void setupEventHandlers(Stage primaryStage) {
        loadTxtButton.setOnAction(_event -> loadFile(primaryStage, "txt"));
        loadJsonButton.setOnAction(_event -> loadFile(primaryStage, "json"));
        clearButton.setOnAction(_event -> {
            txtOutputArea.clear();
            jsonOutputArea.clear();
            warningsArea.clear();
            qualityResultsArea.clear();
            userStoriesListView.getItems().clear();
            currentUserStories.clear();
        });
        convertToJsonButton.setOnAction(_event -> convertTxtToJson(primaryStage));
        saveJsonButton.setOnAction(_event -> saveJsonToFile(primaryStage));
        
        analyzeQualityButton.setOnAction(_event -> performQualityAnalysis());
        exportReportButton.setOnAction(_event -> exportQualityReport(primaryStage));
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
                manager.loadResourceBundle(messages.getLocale());
                warningsArea.clear();
                
                List<UserStory> userStories = manager.readUserStories(selectedFile.getAbsolutePath());
                currentUserStories = userStories;
                currentFileName = selectedFile.getName();
                
                // Update quality analysis tab with loaded stories
                updateUserStoriesListView();
                
                TextArea targetArea = type.equals("txt") ? txtOutputArea : jsonOutputArea;
                targetArea.clear();
                targetArea.appendText(messages.getString("label.file") + ": " + selectedFile.getName() + "\n\n");
                for (UserStory story : userStories) {
                    targetArea.appendText(story.toString() + "\n\n");
                }
                if (type.equals("txt")) {
                    displayUserStoriesAsJson(userStories, selectedFile.getName());
                }
                
                List<String> warnings = manager.getParsingWarnings();
                if (!warnings.isEmpty()) {
                    for (String warning : warnings) {
                        warningsArea.appendText(warning + "\n");
                    }
                    warningsLabel.setVisible(true);
                    warningsArea.setVisible(true);
                } else {
                    warningsLabel.setVisible(false);
                    warningsArea.setVisible(false);
                }
            } catch (IOException e) {
                showError(messages.getString("error.loadingFile") + ": " + e.getMessage());
            }
        }
    }

    private void updateUserStoriesListView() {
        userStoriesListView.getItems().clear();
        userStoriesListView.getItems().addAll(currentUserStories);
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

    private void performQualityAnalysis() {
        List<UserStory> selectedUserStories = new ArrayList<>(userStoriesListView.getSelectionModel().getSelectedItems());
        
        if (selectedUserStories.isEmpty()) {
            showError("No user stories selected. Please select user stories first.");
            return;
        }
        
        List<String> selectedCriteria = new ArrayList<>();
        for (CheckBox checkBox : criteriaCheckBoxes) {
            if (checkBox.isSelected()) {
                // Extract criterion key from checkbox userData
                String criterionKey = (String) checkBox.getUserData();
                selectedCriteria.add(criterionKey);
            }
        }
        
        if (selectedCriteria.isEmpty()) {
            showError("No quality criteria selected. Please select at least one criterion.");
            return;
        }
        
        try {
            lastAnalysisResult = qualityManager.analyzeQuality(selectedUserStories, selectedCriteria);
            String report = qualityManager.generateReport(lastAnalysisResult, currentFileName);
            
            qualityResultsArea.clear();
            qualityResultsArea.appendText(report);
            
            showInfo("Quality analysis completed. Found " + lastAnalysisResult.getTotalProblems() + " problems.");
        } catch (Exception e) {
            showError("Error during quality analysis: " + e.getMessage());
        }
    }
    
    private void exportQualityReport(Stage ownerStage) {
        if (lastAnalysisResult == null) {
            showInfo(messages.getString("info.noQualityResults"));
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(messages.getString("fileChooser.saveQualityReport"));
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        fileChooser.setInitialFileName("quality_report");
        File file = fileChooser.showSaveDialog(ownerStage);
        
        if (file != null) {
            try {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".json")) {
                    qualityManager.saveResultsToJson(lastAnalysisResult, file.getAbsolutePath());
                } else {
                    String report = qualityManager.generateReport(lastAnalysisResult, currentFileName);
                    java.nio.file.Files.write(file.toPath(), report.getBytes());
                }
                showInfo(messages.getString("info.qualityReportSaved"));
            } catch (IOException e) {
                showError(messages.getString("error.savingQualityReport") + ": " + e.getMessage());
            }
        }
    }
    
    private void updateQualityCriteriaCheckBoxes() {
        qualityCriteriaBox.getChildren().clear();
        criteriaCheckBoxes.clear();
        
        for (String criterionKey : qualityManager.getAvailableCriteriaNames()) {
            CheckBox checkBox = new CheckBox(qualityManager.getCriterionDisplayName(criterionKey));
            checkBox.setUserData(criterionKey); // Store the criterion key for later retrieval
            criteriaCheckBoxes.add(checkBox);
            qualityCriteriaBox.getChildren().add(checkBox);
        }
    }

    private void loadResourceBundle(Locale locale) {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", locale);
        qualityManager = new QualityAnalysisManager(messages);
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
        warningsArea.setPromptText(messages.getString("prompt.warningsArea"));
        warningsLabel.setText(messages.getString("label.warnings"));
        
        // Quality analysis components
        qualityAnalysisLabel.setText(messages.getString("label.qualityAnalysis"));
        analyzeQualityButton.setText(messages.getString("button.analyzeQuality"));
        exportReportButton.setText(messages.getString("button.exportReport"));
        qualityResultsLabel.setText(messages.getString("label.qualityResults"));
        qualityResultsArea.setPromptText(messages.getString("prompt.qualityResultsArea"));
        userStoriesLabel.setText(messages.getString("label.selectedStories"));
        criteriaLabel.setText(messages.getString("label.qualityCriteria"));
        
        updateQualityCriteriaCheckBoxes();
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