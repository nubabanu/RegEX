package de.uni_marburg.sp25;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.uni_marburg.sp25.quality.QualityAnalysisManager;
import de.uni_marburg.sp25.quality.QualityAnalysisResult;
import de.uni_marburg.sp25.quality.QualityCriterion;
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
import java.util.ResourceBundle; // Ensure this import is present
import java.util.Map; // For availableCriteria field

public class UserStoryApp extends Application {
    private Stage primaryStage; // Add this field

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
    private TabPane tabPane; // Added field
    private VBox qualityCriteriaBox; // Moved declaration here
    
    // Quality analysis components
    private Label qualityAnalysisLabel;
    private Button analyzeQualityButton;
    private Button exportReportButton;
    private ListView<UserStory> userStoriesListView;
    private List<CheckBox> criteriaCheckBoxes = new ArrayList<>();
    private Label qualityResultsLabel;
    private Label userStoriesLabel;
    private Label criteriaLabel;
    
    private List<UserStory> currentUserStories = new ArrayList<>();
    private QualityAnalysisResult lastAnalysisResult;
    private String currentFileName = "";

    private ResourceBundle messages;
    private Map<String, QualityCriterion> availableCriteria; // Added field for criteria re-initialization

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Assign to the field
        
        // Initialize components that need IDs set before other UI setup
        languageSelector = new ComboBox<>();
        changeLanguageButton = new Button(); // Initialize, text will be set later
        qualityCriteriaBox = new VBox(5); // Initialize here
        qualityCriteriaBox.setId("qualityCriteriaBox"); // And set ID here
        
        // Initialize qualityManager before loading resources that depend on it
        // Ensure messages is loaded first if QualityAnalysisManager constructor needs it immediately.
        // However, typical practice is to load messages, then pass to manager.
        // For now, let's ensure messages is available for the first loadResourceBundle call.
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH); // Load default messages
        qualityManager = new QualityAnalysisManager(messages); 

        loadResourceBundle(primaryStage, Locale.ENGLISH); // Pass primaryStage here
        // qualityManager = new QualityAnalysisManager(messages); // Moved up

        // Set IDs for components accessed by tests
        txtOutputArea.setId("txtOutputArea");
        jsonOutputArea.setId("jsonOutputArea");
        warningsArea.setId("warningsArea"); // Though not directly in test, good practice
        qualityResultsArea.setId("qualityResultsArea");
        languageSelector.setId("languageSelector");
        changeLanguageButton.setId("changeLanguageButton");
        // Note: Labels like loadFileLabel are created within methods, ID needs to be set there.
        // Buttons like analyzeQualityButton are also created in methods.

        primaryStage.setTitle(messages.getString("app.title"));

        MenuBar menuBar = new MenuBar();
        fileMenu = new Menu(messages.getString("menu.file"));
        exitItem = new MenuItem(messages.getString("menu.file.exit"));
        exitItem.setOnAction(_e -> primaryStage.close());
        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);

        languageLabelText = new Label(messages.getString("label.language"));
        // languageSelector = new ComboBox<>(); // Moved up
        languageSelector.getItems().addAll("English", "Deutsch");
        languageSelector.setValue("English");

        // changeLanguageButton = new Button(messages.getString("button.changeLanguage")); // Moved up
        changeLanguageButton.setText(messages.getString("button.changeLanguage")); // Set text now
        changeLanguageButton.setOnAction(_event -> {
            updateUIText(this.primaryStage, languageSelector.getValue()); // Pass this.primaryStage
        });

        HBox languageBox = new HBox(10, languageLabelText, languageSelector, changeLanguageButton);
        languageBox.setAlignment(Pos.CENTER_RIGHT);
        languageBox.setPadding(new Insets(5));

        // Create tabs
        this.tabPane = new TabPane(); // Initialize the field directly
        tabPane.setId("mainTabPane"); // Add ID to TabPane

        // Tab 1: File Operations
        Tab fileOperationsTab = new Tab(messages.getString("tab.fileOperations"));
        fileOperationsTab.setId("fileOperationsTab");
        fileOperationsTab.setClosable(false);
        
        VBox fileOperationsContent = createFileOperationsTab();
        fileOperationsTab.setContent(new ScrollPane(fileOperationsContent));
        
        // Tab 2: Quality Analysis
        Tab qualityAnalysisTab = new Tab(messages.getString("tab.qualityAnalysis"));
        qualityAnalysisTab.setId("qualityAnalysisTab");
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
        loadFileLabel.setId("loadFileLabel"); // Set ID
        loadTxtButton = new Button(messages.getString("button.loadTxt"));
        loadTxtButton.setId("loadTxtButton");
        loadJsonButton = new Button(messages.getString("button.loadJson"));
        loadJsonButton.setId("loadJsonButton");
        clearButton = new Button(messages.getString("button.clearOutput"));
        clearButton.setId("clearButton");
        convertToJsonButton = new Button(messages.getString("button.convertToJson"));
        convertToJsonButton.setId("convertToJsonButton");
        saveJsonButton = new Button(messages.getString("button.saveJson"));
        saveJsonButton.setId("saveJsonButton");

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
        analyzeQualityButton.setId("analyzeQualityButton"); // Set ID
        exportReportButton = new Button(messages.getString("button.exportReport"));
        exportReportButton.setId("exportReportButton");

        HBox qualityOperationsBox = new HBox(10, analyzeQualityButton, exportReportButton);
        qualityOperationsBox.setPadding(new Insets(10,0,10,0));

        // User stories selection
        userStoriesLabel = new Label(messages.getString("label.selectedStories"));
        userStoriesListView = new ListView<>();
        userStoriesListView.setId("userStoriesListView"); // Set ID
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
        // qualityCriteriaBox = new VBox(5); // Moved to start()
        // qualityCriteriaBox.setId("qualityCriteriaBox"); // Moved to start()
        qualityCriteriaBox.setPrefHeight(150);
        qualityCriteriaBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-padding: 5;");
        
        // Initialize quality criteria checkboxes
        updateQualityCriteriaCheckBoxes();

        // Add "Select All" and "Clear All" buttons for criteria
        Button selectAllCriteriaButton = new Button("Select All");
        selectAllCriteriaButton.setId("selectAllCriteriaButton"); // Set ID
        Button clearAllCriteriaButton = new Button("Clear All");
        clearAllCriteriaButton.setId("clearAllCriteriaButton"); // Set ID
        
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
            processLoadedFile(selectedFile, type);
        }
    }

    // New overloaded method for testing
    public void loadFile(File selectedFile, String type) {
        processLoadedFile(selectedFile, type);
    }

    private void processLoadedFile(File selectedFile, String type) {
        try {
            manager.loadResourceBundle(messages.getLocale());
            
            warningsArea.clear(); // UI update

            List<UserStory> userStories = manager.readUserStories(selectedFile.getAbsolutePath());
            currentUserStories = userStories;
            currentFileName = selectedFile.getName();

            // Update quality analysis tab with loaded stories
            updateUserStoriesListView(); // UI update

            TextArea targetArea = type.equals("txt") ? txtOutputArea : jsonOutputArea;
            targetArea.clear();
            targetArea.appendText(messages.getString("label.file") + ": " + selectedFile.getName() + "\n\n");
            for (int i = 0; i < userStories.size(); i++) {
                UserStory story = userStories.get(i);
                if (type.equals("txt")) {
                    // Format for text display with each field on its own line
                    targetArea.appendText("PID: " + story.getPid() + "\n");
                    targetArea.appendText("Text: " + story.getText() + "\n");
                    targetArea.appendText("Persona: " + (story.getPersona() != null ? story.getPersona().toString() : "[]") + "\n");
                    targetArea.appendText("Action.Goal: " + (story.getActionGoal() != null ? story.getActionGoal().toString() : "[]") + "\n");
                    targetArea.appendText("Action.Benefit: " + (story.getActionBenefit() != null ? story.getActionBenefit().toString() : "[]") + "\n");
                    targetArea.appendText("Entity.Goal: " + (story.getEntityGoal() != null ? story.getEntityGoal().toString() : "[]") + "\n");
                    targetArea.appendText("Entity.Benefit: " + (story.getEntityBenefit() != null ? story.getEntityBenefit().toString() : "[]") + "\n");
                    targetArea.appendText("Benefit: " + story.getBenefit() + "\n");
                    targetArea.appendText("Triggers: " + (story.getTriggers() != null ? story.getTriggers().toString() : "[]") + "\n");
                    targetArea.appendText("Targets: " + (story.getTargets() != null ? story.getTargets().toString() : "[]") + "\n");
                    targetArea.appendText("Contains: " + (story.getContains() != null ? story.getContains().toString() : "[]") + "\n");
                    
                    // Add extra newlines between stories to clearly separate them, and after the last story
                    targetArea.appendText("\n\n");
                    
                    // Add a visual separator between stories for better readability
                    if (i < userStories.size() - 1) {
                        targetArea.appendText("----------------------------------------\n\n");
                    }
                } else {
                    targetArea.appendText(story.toString() + "\n\n");
                    
                    // Add a visual separator between JSON stories
                    if (i < userStories.size() - 1) {
                        targetArea.appendText("----------------------------------------\n\n");
                    }
                }
            }

            if (type.equals("txt")) {
                // displayUserStoriesAsJson is already safe or updates UI on FX thread
                displayUserStoriesAsJson(userStories, selectedFile.getName());
            }

            List<String> warnings = manager.getParsingWarnings();
            // Platform.runLater(() -> { // Remove Platform.runLater wrapper
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
            // });
        } catch (IOException e) {
            showError(messages.getString("error.loadingFile") + ": " + e.getMessage());
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
            jsonOutputArea.appendText(messages.getString("label.jsonRepresentationOf") + " " + originalFileName + ":\\n\\n");
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
                processConvertTxtToJson(txtFile, jsonFile);
            }
        }
    }

    // New overloaded method for testing
    public void convertTxtToJson(File txtFile, File jsonFile) {
        processConvertTxtToJson(txtFile, jsonFile);
    }

    private void processConvertTxtToJson(File txtFile, File jsonFile) {
        try {
            List<UserStory> userStories = manager.readUserStories(txtFile.getAbsolutePath());
            manager.saveToJson(userStories, jsonFile.getAbsolutePath());
            showInfo(messages.getString("info.fileConvertedAndSaved"));
            // displayUserStoriesAsJson is already safe or updates UI on FX thread
            displayUserStoriesAsJson(userStories, jsonFile.getName());
        } catch (IOException e) {
            showError(messages.getString("error.convertingTxtToJson") + ": " + e.getMessage());
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
            showError("No user stories selected. Please select user stories first."); // showError is wrapped
            return;
        }

        List<String> selectedCriteria = new ArrayList<>();
        for (CheckBox checkBox : criteriaCheckBoxes) {
            if (checkBox.isSelected()) {
                String criterionKey = (String) checkBox.getUserData();
                selectedCriteria.add(criterionKey);
            }
        }

        if (selectedCriteria.isEmpty()) {
            showError("No quality criteria selected. Please select at least one criterion."); // showError is wrapped
            return;
        }

        try {
            lastAnalysisResult = qualityManager.analyzeQuality(selectedUserStories, selectedCriteria);
            String report = qualityManager.generateReport(lastAnalysisResult, currentFileName);

            qualityResultsArea.clear();
            qualityResultsArea.appendText(report);
            
            showInfo("Quality analysis completed. Found " + lastAnalysisResult.getTotalProblems() + " problems."); // showInfo is wrapped
        } catch (Exception e) {
            showError("Error during quality analysis: " + e.getMessage()); // showError is wrapped
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
            processExportQualityReport(file);
        }
    }

    // New overloaded method for testing
    public void exportQualityReport(File file) {
        if (lastAnalysisResult == null) {
            showInfo(messages.getString("info.noQualityResults")); // Consider if this should throw an error for tests
            return;
        }
        processExportQualityReport(file);
    }

    private void processExportQualityReport(File file) {
        try {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".json")) {
                qualityManager.saveResultsToJson(lastAnalysisResult, file.getAbsolutePath());
            } else { // Default to text if not json
                String report = qualityManager.generateReport(lastAnalysisResult, currentFileName);
                java.nio.file.Files.write(file.toPath(), report.getBytes());
            }
            // Using a hardcoded string to avoid resource bundle issue
            showInfo("Quality report saved successfully.");
        } catch (IOException e) {
            showError(messages.getString("error.savingQualityReport") + ": " + e.getMessage());
        }
    }
    
    // Modified to accept Stage
    private void loadResourceBundle(Stage stage, Locale locale) {
        javafx.application.Platform.runLater(() -> {
            messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", locale);
            if (qualityManager != null) {
                qualityManager.setMessages(messages);
            }
            if (stage != null) { 
                stage.setTitle(messages.getString("app.title"));
            }
            // Update UI elements that depend on the resource bundle
            if (fileMenu != null) {
                fileMenu.setText(messages.getString("menu.file"));
            }
            if (exitItem != null) {
                exitItem.setText(messages.getString("menu.file.exit"));
            }
            if (languageLabelText != null) {
                languageLabelText.setText(messages.getString("label.language"));
            }
            if (changeLanguageButton != null) {
                changeLanguageButton.setText(messages.getString("button.changeLanguage"));
            }
            if (tabPane != null && tabPane.getTabs().size() >= 2) {
                tabPane.getTabs().get(0).setText(messages.getString("tab.fileOperations"));
                tabPane.getTabs().get(1).setText(messages.getString("tab.qualityAnalysis"));
            }
            if (loadFileLabel != null) {
                loadFileLabel.setText(messages.getString("label.loadUserStories"));
            }
            if (loadTxtButton != null) {
                loadTxtButton.setText(messages.getString("button.loadTxt"));
            }
            if (loadJsonButton != null) {
                loadJsonButton.setText(messages.getString("button.loadJson"));
            }
            if (clearButton != null) {
                clearButton.setText(messages.getString("button.clearOutput"));
            }
            if (convertToJsonButton != null) {
                convertToJsonButton.setText(messages.getString("button.convertToJson"));
            }
            if (saveJsonButton != null) {
                saveJsonButton.setText(messages.getString("button.saveJson"));
            }
            if (txtOutputArea != null) {
                txtOutputArea.setPromptText(messages.getString("prompt.txtOutputArea"));
            }
            if (jsonOutputArea != null) {
                jsonOutputArea.setPromptText(messages.getString("prompt.jsonOutputArea"));
            }
            if (warningsLabel != null) {
                warningsLabel.setText(messages.getString("label.warnings"));
            }
            if (warningsArea != null) {
                warningsArea.setPromptText(messages.getString("prompt.warningsArea"));
            }
            if (qualityAnalysisLabel != null) {
                qualityAnalysisLabel.setText(messages.getString("label.qualityAnalysis"));
            }
            if (analyzeQualityButton != null) {
                analyzeQualityButton.setText(messages.getString("button.analyzeQuality"));
            }
            if (exportReportButton != null) {
                exportReportButton.setText(messages.getString("button.exportReport"));
            }
            if (userStoriesLabel != null) {
                userStoriesLabel.setText(messages.getString("label.selectedStories"));
            }
            if (criteriaLabel != null) {
                criteriaLabel.setText(messages.getString("label.qualityCriteria"));
            }
            if (qualityResultsLabel != null) {
                qualityResultsLabel.setText(messages.getString("label.qualityResults"));
            }
            if (qualityResultsArea != null) {
                qualityResultsArea.setPromptText(messages.getString("prompt.qualityResultsArea"));
            }
            updateQualityCriteriaCheckBoxes(); 
        });
    }

    // Overloaded method to handle language change
    private void updateUIText(Stage stage, String selectedLanguage) {
        Locale localeToLoad = "Deutsch".equals(selectedLanguage) ? Locale.GERMAN : Locale.ENGLISH;
        
        // First load the new resource bundle
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", localeToLoad);
        if (qualityManager != null) {
            qualityManager.setMessages(messages);
        }
        
        // Then update the UI on the FX application thread
        javafx.application.Platform.runLater(() -> {
            // Update stage title
            if (stage != null) { 
                stage.setTitle(messages.getString("app.title"));
            }
            
            // Update all UI elements
            if (fileMenu != null) {
                fileMenu.setText(messages.getString("menu.file"));
            }
            if (exitItem != null) {
                exitItem.setText(messages.getString("menu.file.exit"));
            }
            if (languageLabelText != null) {
                languageLabelText.setText(messages.getString("label.language"));
            }
            if (changeLanguageButton != null) {
                changeLanguageButton.setText(messages.getString("button.changeLanguage"));
            }
            if (tabPane != null && tabPane.getTabs().size() >= 2) {
                tabPane.getTabs().get(0).setText(messages.getString("tab.fileOperations"));
                tabPane.getTabs().get(1).setText(messages.getString("tab.qualityAnalysis"));
            }
            if (loadFileLabel != null) {
                loadFileLabel.setText(messages.getString("label.loadUserStories"));
            }
            if (loadTxtButton != null) {
                loadTxtButton.setText(messages.getString("button.loadTxt"));
            }
            if (loadJsonButton != null) {
                loadJsonButton.setText(messages.getString("button.loadJson"));
            }
            if (clearButton != null) {
                clearButton.setText(messages.getString("button.clearOutput"));
            }
            if (convertToJsonButton != null) {
                convertToJsonButton.setText(messages.getString("button.convertToJson"));
            }
            if (saveJsonButton != null) {
                saveJsonButton.setText(messages.getString("button.saveJson"));
            }
            if (txtOutputArea != null) {
                txtOutputArea.setPromptText(messages.getString("prompt.txtOutputArea"));
            }
            if (jsonOutputArea != null) {
                jsonOutputArea.setPromptText(messages.getString("prompt.jsonOutputArea"));
            }
            if (warningsLabel != null) {
                warningsLabel.setText(messages.getString("label.warnings"));
            }
            if (warningsArea != null) {
                warningsArea.setPromptText(messages.getString("prompt.warningsArea"));
            }
            if (qualityAnalysisLabel != null) {
                qualityAnalysisLabel.setText(messages.getString("label.qualityAnalysis"));
            }
            if (analyzeQualityButton != null) {
                analyzeQualityButton.setText(messages.getString("button.analyzeQuality"));
            }
            if (exportReportButton != null) {
                exportReportButton.setText(messages.getString("button.exportReport"));
            }
            if (userStoriesLabel != null) {
                userStoriesLabel.setText(messages.getString("label.selectedStories"));
            }
            if (criteriaLabel != null) {
                criteriaLabel.setText(messages.getString("label.qualityCriteria"));
            }
            if (qualityResultsLabel != null) {
                qualityResultsLabel.setText(messages.getString("label.qualityResults"));
            }
            if (qualityResultsArea != null) {
                qualityResultsArea.setPromptText(messages.getString("prompt.qualityResultsArea"));
            }
            
            // Update quality criteria checkboxes
            updateQualityCriteriaCheckBoxes();
        });
    }

    // Public method for testing
    public void loadResourceBundle(Locale locale) {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", locale);
        if (qualityManager != null) {
            qualityManager.setMessages(messages);
        }
        updateUIText(primaryStage, locale == Locale.GERMAN ? "Deutsch" : "English");
    }
    private void updateQualityCriteriaCheckBoxes() {
        // Ensure this runs on the FX application thread
        // and that modifications to the qualityCriteriaBox are atomic from TestFX's perspective.
        javafx.application.Platform.runLater(() -> {
            qualityCriteriaBox.getChildren().clear();
            criteriaCheckBoxes.clear();

            if (qualityManager == null) {
                // This case should ideally not happen if initialization order is correct.
                // Consider logging an error or throwing an IllegalStateException.
                System.err.println("QualityManager is null when trying to update criteria checkboxes.");
                return;
            }
            
            // Ensure availableCriteria is initialized and fetched correctly
            availableCriteria = qualityManager.getAvailableCriteria();
            if (availableCriteria == null || availableCriteria.isEmpty()) {
                 // Log or handle the case where no criteria are available
                 System.err.println("No available quality criteria to display.");
                 return;
            }

            for (Map.Entry<String, QualityCriterion> entry : availableCriteria.entrySet()) {
                String key = entry.getKey();
                QualityCriterion criterion = entry.getValue();
                // Use criterion.getCriterionName() which should be localized by QualityAnalysisManager
                CheckBox checkBox = new CheckBox(criterion.getCriterionName()); 
                checkBox.setUserData(key); // Store the key for later use
                checkBox.setId("criterionCheckBox_" + key); // Unique ID for testing
                criteriaCheckBoxes.add(checkBox);
                qualityCriteriaBox.getChildren().add(checkBox);
            }
        });
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