package de.uni_marburg.sp25;

// Core library imports
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

// Application-specific imports
import de.uni_marburg.sp25.quality.QualityAnalysisManager;
import de.uni_marburg.sp25.quality.QualityAnalysisResult;
import de.uni_marburg.sp25.quality.QualityCriterion;

// JavaFX UI framework imports
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane; 
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Standard Java imports
import java.io.File;
import java.io.FileWriter; 
import java.io.IOException;
import java.nio.file.Files; 
import java.util.ArrayList;
import java.util.List; 
import java.util.Locale; 
import java.util.ResourceBundle; 
import java.util.Map;

/**
 * Main JavaFX application for User Story Management and Quality Analysis.
 * 
 * This application provides a comprehensive platform for:
 * - Loading user stories from text or JSON files
 * - Converting between text and JSON formats using Jackson library
 * - Performing quality analysis using multiple criteria (atomicity, minimality, etc.)
 * - Visualizing user story relationships as graphs using GraphStream
 * - Generating quality reports with detailed problem descriptions
 * - Supporting internationalization (English/German) via ResourceBundle
 * 
 * Key Dependencies:
 * - JavaFX: Provides the desktop GUI framework with scenes, controls, and layouts
 * - Jackson: Handles JSON serialization/deserialization for user story data
 * - GraphStream: Renders interactive graph visualizations of story relationships
 * - ResourceBundle: Enables multi-language support for UI text
 * 
 * Architecture:
 * - UserStoryApp: Main UI controller and application entry point
 * - UserStoryManager: Handles file I/O and data conversion operations
 * - QualityAnalysisManager: Coordinates quality analysis across multiple criteria
 * - AnnotationGraphManager: Manages graph visualization and relationship extraction
 */
public class UserStoryApp extends Application {
    
    // Core application components
    private Stage primaryStage;
    private UserStoryManager manager = new UserStoryManager();
    private QualityAnalysisManager qualityManager;
    
    // UI text areas for displaying different types of content
    private TextArea txtOutputArea = new TextArea();           // Original text format user stories
    private TextArea jsonOutputArea = new TextArea();          // JSON formatted user stories  
    private TextArea warningsArea = new TextArea();            // Parsing warnings and errors
    private TextArea qualityResultsArea = new TextArea();      // Quality analysis results
    
    // Specialized UI components
    private Pane graphVisualizationPane;                       // Container for GraphStream visualizations
    
    // Menu components for application navigation
    private Menu fileMenu;
    private MenuItem exitItem;
    
    // File operation controls
    private Label loadFileLabel;
    private Button loadTxtButton;
    private Button loadJsonButton; 
    private Button clearButton;
    private Button convertToJsonButton;
    private Button saveJsonButton;
    
    // Language selection and internationalization controls
    private ComboBox<String> languageSelector;
    private Label languageLabelText;
    private Button changeLanguageButton;
    
    // Warning display controls
    private Label warningsLabel;
    
    // Main layout containers
    private TabPane tabPane;                                    // Organizes functionality into tabs
    private VBox qualityCriteriaBox;                           // Container for quality criteria selection
    private HBox graphButtonBox;
    
    // Graph visualization controls
    private Label graphVisualizationLabel;
    private Button showGraphButton;
    private ListView<UserStory> graphUserStoriesListView;      // Selectable list for graph generation
    private Label graphUserStoriesLabel;
    
    // Quality analysis controls  
    private Label qualityAnalysisLabel;
    private Button analyzeQualityButton;
    private Button exportReportButton;
    private ListView<UserStory> userStoriesListView;           // Selectable list for quality analysis
    private List<CheckBox> criteriaCheckBoxes = new ArrayList<>(); // Dynamic criteria selection
    private Label qualityResultsLabel;
    private Label userStoriesLabel;
    private Label criteriaLabel;
    
    // Application state management
    private List<UserStory> currentUserStories = new ArrayList<>();
    private QualityAnalysisResult lastAnalysisResult;
    private String currentFileName = "";
    
    // Internationalization and configuration
    private ResourceBundle messages;                            // Localized text resources
    private Map<String, QualityCriterion> availableCriteria;   // Quality analysis criteria registry

    /**
     * Provides access to the current ResourceBundle for internationalization.
     * Used by other components that need localized text resources.
     * @return Current ResourceBundle instance containing localized messages
     */
    public ResourceBundle getMessages() {
        return messages;
    }

    /**
     * Main application entry point and UI initialization.
     * 
     * Sets up the complete JavaFX interface including:
     * - Language selection controls with ResourceBundle support
     * - Tabbed interface for file operations and quality analysis  
     * - Event handlers for all user interactions
     * - Initial window sizing and scene configuration
     * 
     * @param primaryStage The primary stage provided by JavaFX framework
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize UI components that require early setup for testing and event handling
        languageSelector = new ComboBox<>();
        changeLanguageButton = new Button();
        qualityCriteriaBox = new VBox(5);
        qualityCriteriaBox.setId("qualityCriteriaBox");
        
        // Load default English resources and initialize quality analysis system
        // QualityAnalysisManager requires ResourceBundle for localized criterion names
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH);
        qualityManager = new QualityAnalysisManager(messages);

        loadResourceBundle(primaryStage, Locale.ENGLISH);

        // Configure component IDs for automated testing and accessibility
        txtOutputArea.setId("txtOutputArea");
        jsonOutputArea.setId("jsonOutputArea");
        warningsArea.setId("warningsArea");
        qualityResultsArea.setId("qualityResultsArea");
        languageSelector.setId("languageSelector");
        changeLanguageButton.setId("changeLanguageButton");

        // Configure main window properties
        primaryStage.setTitle(messages.getString("app.title"));

        // Create application menu bar
        MenuBar menuBar = new MenuBar();
        fileMenu = new Menu(messages.getString("menu.file"));
        exitItem = new MenuItem(messages.getString("menu.file.exit"));
        exitItem.setOnAction(_ -> primaryStage.close());
        fileMenu.getItems().add(exitItem);
        menuBar.getMenus().add(fileMenu);

        // Setup language selection controls for internationalization
        languageLabelText = new Label(messages.getString("label.language"));
        languageSelector.getItems().addAll("English", "Deutsch");
        languageSelector.setValue("English");

        changeLanguageButton.setText(messages.getString("button.changeLanguage"));
        changeLanguageButton.setOnAction(_ -> {
            updateUIText(this.primaryStage, languageSelector.getValue());
        });

        HBox languageBox = new HBox(10, languageLabelText, languageSelector, changeLanguageButton);
        languageBox.setAlignment(Pos.CENTER_RIGHT);
        languageBox.setPadding(new Insets(5));

        // Create main tabbed interface to organize application functionality
        this.tabPane = new TabPane();
        tabPane.setId("mainTabPane");

        // Tab 1: File Operations - handles loading, converting, and saving user stories
        Tab fileOperationsTab = new Tab(messages.getString("tab.fileOperations"));
        fileOperationsTab.setId("fileOperationsTab");
        fileOperationsTab.setClosable(false);
        
        VBox fileOperationsContent = createFileOperationsTab();
        fileOperationsTab.setContent(new ScrollPane(fileOperationsContent));
        
        // Tab 2: Quality Analysis - performs quality assessment using multiple criteria
        Tab qualityAnalysisTab = new Tab(messages.getString("tab.qualityAnalysis"));
        qualityAnalysisTab.setId("qualityAnalysisTab");
        qualityAnalysisTab.setClosable(false);
        
        VBox qualityAnalysisContent = createQualityAnalysisTab();
        qualityAnalysisTab.setContent(new ScrollPane(qualityAnalysisContent));
        
        tabPane.getTabs().addAll(fileOperationsTab, qualityAnalysisTab);

        // Configure event handlers for user interactions
        setupEventHandlers(primaryStage);

        // Assemble main application layout
        VBox mainLayout = new VBox(10, languageBox, tabPane);
        mainLayout.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainLayout);

        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the file operations tab containing controls for loading, converting, and saving user stories.
     * 
     * This tab provides:
     * - File loading from TXT or JSON formats
     * - Text-to-JSON conversion using Jackson ObjectMapper
     * - JSON export functionality with pretty printing
     * - Real-time display of parsing warnings and errors
     * - Output areas for both original text and converted JSON
     * 
     * @return VBox containing all file operation controls and display areas
     */
    private VBox createFileOperationsTab() {
        // Create file operation controls with internationalized labels
        loadFileLabel = new Label(messages.getString("label.loadUserStories"));
        loadFileLabel.setId("loadFileLabel");
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

        // Configure text display areas for user story content
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
        
        // Graph visualization components using GraphStream library
        graphVisualizationLabel = new Label(messages.getString("label.graphVisualization"));
        graphVisualizationLabel.setId("graphVisualizationLabel");
        
        // User story selection for graph generation
        graphUserStoriesLabel = new Label(messages.getString("label.selectGraphStory"));
        graphUserStoriesLabel.setId("graphUserStoriesLabel");
        graphUserStoriesListView = new ListView<>();
        graphUserStoriesListView.setId("graphUserStoriesListView");
        graphUserStoriesListView.setPrefHeight(150);
        graphUserStoriesListView.setPrefWidth(400);
        
        // Custom cell factory to display user stories with PID and text
        graphUserStoriesListView.setCellFactory(_ -> new ListCell<UserStory>() {
            @Override
            protected void updateItem(UserStory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String pidText = item.getPid() != null ? item.getPid() + ": " : "";
                    setText(pidText + item.getText());
                }
            }
        });

        // GraphStream visualization container (replaces traditional text area)
        graphVisualizationPane = new Pane();
        graphVisualizationPane.setPrefHeight(400);
        graphVisualizationPane.setId("graphVisualizationPane");
        graphVisualizationPane.setStyle("-fx-border-color: lightgrey;");

        showGraphButton = new Button(messages.getString("button.showGraph"));
        showGraphButton.setId("showGraphButton");
        
        HBox graphButtonBox = new HBox(10, showGraphButton);
        graphButtonBox.setPadding(new Insets(5,0,5,0));
        this.graphButtonBox = graphButtonBox;
        
        VBox graphVisualizationBox = new VBox(5, 
            graphUserStoriesLabel,
            graphUserStoriesListView,
            graphButtonBox,
            graphVisualizationPane
        );

        VBox outputDisplayBox = new VBox(10, 
            new Label(messages.getString("label.txtContent")), txtOutputArea, 
            new Label(messages.getString("label.jsonContent")), jsonOutputArea,
            warningsLabel, warningsArea,
            graphVisualizationLabel, graphVisualizationBox);

        // Initially hide graph visualization controls until user stories are loaded
        graphVisualizationLabel.setVisible(false);
        graphUserStoriesLabel.setVisible(false);
        graphUserStoriesListView.setVisible(false);
        graphButtonBox.setVisible(false);
        graphVisualizationPane.setVisible(false);

        return new VBox(10, loadFileLabel, fileOperationsBox, outputDisplayBox);
    }

    /**
     * Creates the quality analysis tab containing controls for analyzing user story quality.
     * 
     * This tab provides:
     * - Multi-criteria quality analysis (atomicity, minimality, uniformity, etc.)
     * - User story selection for targeted analysis
     * - Dynamic criteria selection via checkboxes
     * - Quality results display with detailed problem descriptions
     * - Report export functionality for analysis results
     * 
     * Quality criteria are managed by QualityAnalysisManager and implemented as separate
     * analyzer classes, each focusing on specific quality aspects of user stories.
     * 
     * @return VBox containing all quality analysis controls and display areas
     */
    private VBox createQualityAnalysisTab() {
        qualityAnalysisLabel = new Label(messages.getString("label.qualityAnalysis"));
        qualityAnalysisLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        qualityAnalysisLabel.setId("qualityAnalysisLabel");
        
        analyzeQualityButton = new Button(messages.getString("button.analyzeQuality"));
        analyzeQualityButton.setId("analyzeQualityButton");
        exportReportButton = new Button(messages.getString("button.exportReport"));
        exportReportButton.setId("exportReportButton");

        HBox qualityOperationsBox = new HBox(10, analyzeQualityButton, exportReportButton);
        qualityOperationsBox.setPadding(new Insets(10,0,10,0));

        // User story selection with multi-select capability for targeted analysis
        userStoriesLabel = new Label(messages.getString("label.selectedStories"));
        userStoriesListView = new ListView<>();
        userStoriesListView.setId("userStoriesListView");
        userStoriesListView.setPrefHeight(150);
        userStoriesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userStoriesListView.setCellFactory(_ -> new ListCell<UserStory>() {
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

        // Quality criteria selection - dynamically populated from QualityAnalysisManager
        criteriaLabel = new Label(messages.getString("label.qualityCriteria"));
        qualityCriteriaBox.setPrefHeight(150);
        qualityCriteriaBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-padding: 5;");
        
        // Initialize quality criteria checkboxes
        updateQualityCriteriaCheckBoxes();

        // Add "Select All" and "Clear All" buttons for criteria
        Button selectAllCriteriaButton = new Button("Select All");
        selectAllCriteriaButton.setId("selectAllCriteriaButton"); // Set ID
        Button clearAllCriteriaButton = new Button("Clear All");
        clearAllCriteriaButton.setId("clearAllCriteriaButton"); // Set ID
        
        selectAllCriteriaButton.setOnAction(_ -> {
            for (CheckBox checkBox : criteriaCheckBoxes) {
                checkBox.setSelected(true);
            }
        });
        
        clearAllCriteriaButton.setOnAction(_ -> {
            for (CheckBox checkBox : criteriaCheckBoxes) {
                checkBox.setSelected(false);
            }
        });
        
        HBox criteriaButtonsBox = new HBox(5, selectAllCriteriaButton, clearAllCriteriaButton);

        // Quality results
        qualityResultsLabel = new Label(messages.getString("label.qualityResults"));
        // Don't re-initialize qualityResultsArea - it's already initialized in start() method
        // qualityResultsArea = new TextArea(); // Re-initialized here
        qualityResultsArea.setId("qualityResultsArea"); // Set ID to ensure it's always available for tests
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
        loadTxtButton.setOnAction(_ -> loadFile(primaryStage, "txt"));
        loadJsonButton.setOnAction(_ -> loadFile(primaryStage, "json"));
        clearButton.setOnAction(_ -> {
            txtOutputArea.clear();
            jsonOutputArea.clear();
            warningsArea.clear();
            // graphVisualizationArea.clear(); // Old TextArea
            if (graphVisualizationPane.getChildren().size() > 0 && graphVisualizationPane.getChildren().get(0) instanceof org.graphstream.ui.fx_viewer.FxViewPanel) {
                // If a GraphStream view is present, properly close it to release resources
                org.graphstream.ui.fx_viewer.FxViewPanel viewPanel = (org.graphstream.ui.fx_viewer.FxViewPanel) graphVisualizationPane.getChildren().get(0);
                if (viewPanel.getViewer() != null) {
                    viewPanel.getViewer().close();
                }
            }
            graphVisualizationPane.getChildren().clear(); // Clear the pane
            qualityResultsArea.clear();
            userStoriesListView.getItems().clear();
            graphUserStoriesListView.getItems().clear();
            currentUserStories.clear();
        });
        convertToJsonButton.setOnAction(_ -> convertTxtToJson(primaryStage));
        saveJsonButton.setOnAction(_ -> saveJsonToFile(primaryStage));
        
        showGraphButton.setOnAction(_ -> displayGraphVisualization());
        
        analyzeQualityButton.setOnAction(_ -> performQualityAnalysis());
        exportReportButton.setOnAction(_ -> exportQualityReport(primaryStage));
    }
    
    /**
     * Displays the graph visualization for the selected user story
     */
    private void displayGraphVisualization() {
        if (graphUserStoriesListView.getSelectionModel().getSelectedItem() == null) {
            showError(messages.getString("error.noUserStorySelected"));
            return;
        }
        
        UserStory selectedStory = graphUserStoriesListView.getSelectionModel().getSelectedItem();
        String pid = selectedStory.getPid();
        
        AnnotationGraph annotationGraph = manager.getAnnotationGraph(pid);
        
        if (annotationGraph == null) {
            annotationGraph = manager.generateGraphForUserStory(selectedStory);
        }
        
        if (annotationGraph == null) {
            showError("Could not generate or find graph for the selected story.");
            return;
        }

        // Create GraphStream graph with unique identifier to prevent naming conflicts
        org.graphstream.graph.Graph gsGraph = new org.graphstream.graph.implementations.SingleGraph("storyGraph-" + pid + System.currentTimeMillis(), false, true);
        System.setProperty("org.graphstream.ui", "javafx");

        String stylesheet = 
            "node { " +
            "   size: 20px, 20px; " +
            "   fill-color: #ADD8E6; " + 
            "   text-size: 12px; " +
            "   text-alignment: at-right; " +
            "   text-offset: 5px, 0px; " +
            "   stroke-mode: plain; " +
            "   stroke-color: #00008B; " + 
            "} " +
            "edge { " +
            "   fill-color: #808080; " + 
            "   text-size: 10px; " +
            "   arrow-size: 8px, 5px; " +
            "} " +
            "node.ROLE { fill-color: #FFD700; } " + 
            "node.GOAL_ACTION { fill-color: #90EE90; } " + 
            "node.GOAL_ENTITY { fill-color: #90EE90; } " +
            "node.BENEFIT_ACTION { fill-color: #FFA07A; } " + 
            "node.BENEFIT_ENTITY { fill-color: #FFA07A; } ";
        gsGraph.setAttribute("ui.stylesheet", stylesheet);
        gsGraph.setAttribute("ui.quality");
        gsGraph.setAttribute("ui.antialias");

        if (annotationGraph.getNodes() != null) {
            for (de.uni_marburg.sp25.Node appNode : annotationGraph.getNodes()) {
                if (appNode != null && appNode.getLabel() != null) { // Use getLabel() for ID
                    String nodeId = appNode.getLabel();
                    if (gsGraph.getNode(nodeId) == null) { // Avoid duplicate nodes if labels aren't unique (though they should be)
                        org.graphstream.graph.Node gsNode = gsGraph.addNode(nodeId);
                        // Use getLabel() for name and getType() for class
                        gsNode.setAttribute("ui.label", appNode.getLabel() + " [" + appNode.getType().toString() + "]");
                        gsNode.setAttribute("ui.class", appNode.getType().toString()); 
                    }
                } else {
                    System.err.println("Skipping null node or node with null label.");
                }
            }
        }

        if (annotationGraph.getEdges() != null) {
            int edgeCounter = 0; // Counter for unique edge IDs
            for (de.uni_marburg.sp25.Edge appEdge : annotationGraph.getEdges()) {
                if (appEdge != null && appEdge.getSource() != null && appEdge.getTarget() != null &&
                    appEdge.getSource().getLabel() != null && appEdge.getTarget().getLabel() != null) {
                    
                    String sourceNodeId = appEdge.getSource().getLabel();
                    String targetNodeId = appEdge.getTarget().getLabel();
                    
                    // Ensure source and target nodes exist in the GraphStream graph
                    if (gsGraph.getNode(sourceNodeId) != null && gsGraph.getNode(targetNodeId) != null) {
                        String edgeId = sourceNodeId + "->" + targetNodeId + "_" + appEdge.getType().toString() + "_" + edgeCounter++; // Generate unique edge ID
                        // Check if an edge with this ID already exists (optional, but good for complex graphs)
                        if (gsGraph.getEdge(edgeId) == null) {
                           org.graphstream.graph.Edge gsEdge = gsGraph.addEdge(edgeId, sourceNodeId, targetNodeId, true); // true for directed edge
                           gsEdge.setAttribute("ui.label", appEdge.getType().toString());
                        } else {
                            System.err.println("Skipping duplicate edge ID: " + edgeId);
                        }
                    } else {
                        System.err.println("Skipping edge due to missing source/target node in gsGraph. Source: " + sourceNodeId + ", Target: " + targetNodeId);
                    }
                } else {
                    System.err.println("Skipping null edge, or edge with null source/target node or labels.");
                }
            }
        }

        org.graphstream.ui.fx_viewer.FxViewer viewer = new org.graphstream.ui.fx_viewer.FxViewer(gsGraph, org.graphstream.ui.fx_viewer.FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout(); // Enable auto layout
        org.graphstream.ui.fx_viewer.FxViewPanel viewPanel = (org.graphstream.ui.fx_viewer.FxViewPanel)viewer.addDefaultView(false); // false for JavaFX panel
        
        // Clear previous graph and add the new one
        if (graphVisualizationPane.getChildren().size() > 0 && graphVisualizationPane.getChildren().get(0) instanceof org.graphstream.ui.fx_viewer.FxViewPanel) {
            org.graphstream.ui.fx_viewer.FxViewPanel oldViewPanel = (org.graphstream.ui.fx_viewer.FxViewPanel) graphVisualizationPane.getChildren().get(0);
            if (oldViewPanel.getViewer() != null) {
                 oldViewPanel.getViewer().close(); // Properly close the old viewer
            }
        }
        graphVisualizationPane.getChildren().clear();
        graphVisualizationPane.getChildren().add(viewPanel);

        // Make the graph visualization area visible
        graphVisualizationLabel.setVisible(true);
        graphUserStoriesLabel.setVisible(true);
        graphUserStoriesListView.setVisible(true);
        this.graphButtonBox.setVisible(true); 
        // graphVisualizationArea.setVisible(true); // Old TextArea
        graphVisualizationPane.setVisible(true); // New Pane
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
    public void loadFile(Stage primaryStage, String type, File file) {
        if (file != null) {
            currentFileName = file.getName().replaceFirst("[.][^.] + $", ""); // Store filename without extension
            if ("txt".equals(type)) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    currentUserStories.clear(); // Clear previous stories
                    txtOutputArea.clear(); // Clear previous text
                    jsonOutputArea.clear(); // Clear previous json
                    warningsArea.clear(); // Clear previous warnings
                    for (String line : lines) {
                        UserStory story = manager.parseUserStoryFromText(line);
                        if (story != null) {
                            currentUserStories.add(story);
                            txtOutputArea.appendText(story.getText() + "\n");
                        }
                    }
                    // Convert to JSON and update JSON output area
                    if (!currentUserStories.isEmpty()) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                        try {
                            String jsonPreview = objectMapper.writeValueAsString(currentUserStories);
                            jsonOutputArea.setText(jsonPreview);
                        } catch (IOException e) {
                            showError(messages.getString("error.convertingToJsonPreview") + ": " + e.getMessage());
                        }
                    }
                    updateUserStoriesListView(); 
                    updateGraphUserStoriesListView(); 
                    showInfo(messages.getString("info.txtLoaded"));
                } catch (IOException e) {
                    showError(messages.getString("error.loadingTxt") + " " + e.getMessage());
                }
            } else if ("json".equals(type)) {
                try {
                    // Use the manager's readUserStories method to properly handle annotated JSON
                    currentUserStories = manager.readUserStories(file.getAbsolutePath());
                    ObjectMapper objectMapper = new ObjectMapper();
                    jsonOutputArea.setText(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(currentUserStories));
                    txtOutputArea.clear(); // Clear txt area as we loaded JSON
                    warningsArea.clear(); // Clear warnings
                    for(UserStory story : currentUserStories) {
                        txtOutputArea.appendText(story.getPid() + ": " + story.getText() + "\n");
                        // No need to generate graphs here as readUserStories handles annotated JSON
                    }
                    updateUserStoriesListView(); 
                    updateGraphUserStoriesListView(); 
                    showInfo(messages.getString("info.jsonLoaded"));
                } catch (IOException e) {
                    showError(messages.getString("error.loadingJson") + " " + e.getMessage());
                }
            }
            // Make graph visualization components visible if stories are loaded
            boolean storiesExist = !currentUserStories.isEmpty();
            boolean annotationGraphsExist = !manager.getAnnotationGraphs().isEmpty();
            boolean showGraphComponents = storiesExist && (type.equals("txt") || annotationGraphsExist);
            
            // Debug output
            System.out.println("DEBUG: storiesExist = " + storiesExist);
            System.out.println("DEBUG: annotationGraphsExist = " + annotationGraphsExist);
            System.out.println("DEBUG: showGraphComponents = " + showGraphComponents);
            System.out.println("DEBUG: type = " + type);
            System.out.println("DEBUG: annotationGraphs size = " + manager.getAnnotationGraphs().size());
            
            graphVisualizationLabel.setVisible(showGraphComponents);
            graphUserStoriesLabel.setVisible(showGraphComponents);
            graphUserStoriesListView.setVisible(showGraphComponents);
            if (this.graphButtonBox != null) { // Ensure graphButtonBox is initialized
                 this.graphButtonBox.setVisible(showGraphComponents);
            }
            graphVisualizationPane.setVisible(showGraphComponents); // New Pane
        }
    }

    private void processLoadedFile(File selectedFile, String type) {
        try {
            manager.loadResourceBundle(messages.getLocale());
            
            warningsArea.clear(); // UI update
            // graphVisualizationArea.clear(); // Old TextArea
            if (graphVisualizationPane.getChildren().size() > 0 && graphVisualizationPane.getChildren().get(0) instanceof org.graphstream.ui.fx_viewer.FxViewPanel) {
                org.graphstream.ui.fx_viewer.FxViewPanel viewPanel = (org.graphstream.ui.fx_viewer.FxViewPanel) graphVisualizationPane.getChildren().get(0);
                if (viewPanel.getViewer() != null) {
                    viewPanel.getViewer().close();
                }
            }
            graphVisualizationPane.getChildren().clear(); // Clear the pane on new file load

            List<UserStory> userStories = manager.readUserStories(selectedFile.getAbsolutePath());
            currentUserStories = userStories;
            currentFileName = selectedFile.getName();

            // Update quality analysis tab with loaded stories
            updateUserStoriesListView(); // UI update
            updateGraphUserStoriesListView(); // Update graph user stories list view

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

            // Check if this is an annotated JSON file
            if (type.equals("json") && !manager.getAnnotationGraphs().isEmpty()) {
                // Show the graph visualization area
                graphVisualizationLabel.setVisible(true);
                graphUserStoriesLabel.setVisible(true);
                graphUserStoriesListView.setVisible(true);
                this.graphButtonBox.setVisible(true);
                // graphVisualizationArea remains hidden until a graph is shown
            } else {
                // Hide them if no stories are loaded
                graphVisualizationLabel.setVisible(false);
                graphUserStoriesLabel.setVisible(false);
                graphUserStoriesListView.setVisible(false);
                this.graphButtonBox.setVisible(false);
                // graphVisualizationArea.setVisible(false); // Also hide the text area
                graphVisualizationPane.setVisible(false);
            }

        } catch (IOException e) {
            showError(messages.getString("error.loadingFile") + ": " + e.getMessage());
        }
    }

    private void updateUserStoriesListView() {
        userStoriesListView.getItems().clear();
        userStoriesListView.getItems().addAll(currentUserStories);
    }

    /**
     * Updates the user stories list in the graph visualization area
     */
    private void updateGraphUserStoriesListView() {
        graphUserStoriesListView.getItems().clear();
        graphUserStoriesListView.getItems().addAll(currentUserStories);
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
        if (currentUserStories.isEmpty()) {
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
                // Use the manager's saveToJson method for clean JSON output without prefixes
                manager.saveToJson(currentUserStories, file.getAbsolutePath());
                showInfo(messages.getString("info.jsonSaved") + " " + file.getAbsolutePath());
            } catch (IOException e) {
                showError(messages.getString("error.savingJson") + " " + e.getMessage());
            }
        }
    }

    // Overloaded method for testing: saveJsonToFile
    public void saveJsonToFile(Stage primaryStage, File file) {
        if (file != null) {
            if (currentUserStories.isEmpty()) {
                showInfo(messages.getString("info.noJsonToSave"));
                return;
            }
            
            try {
                // Use the manager's saveToJson method for clean JSON output without prefixes
                manager.saveToJson(currentUserStories, file.getAbsolutePath());
                showInfo(messages.getString("info.jsonSaved") + " " + file.getAbsolutePath());
            } catch (IOException e) {
                showError(messages.getString("error.savingJson") + " " + e.getMessage());
            }
        }
    }

    private void performQualityAnalysis() {
        List<UserStory> selectedUserStories = new ArrayList<>(userStoriesListView.getSelectionModel().getSelectedItems());

        if (selectedUserStories.isEmpty()) {
            showError(messages.getString("error.noUserStoriesSelected"));
            javafx.application.Platform.runLater(() -> {
                qualityResultsArea.setText(messages.getString("info.noAnalysisPerformed.noStories"));
            });
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
            showError(messages.getString("error.noCriteriaSelected"));
            javafx.application.Platform.runLater(() -> {
                qualityResultsArea.setText(messages.getString("info.noAnalysisPerformed.noCriteria"));
            });
            return;
        }

        javafx.application.Platform.runLater(() -> {
            qualityResultsArea.clear();
        });

        try {
            lastAnalysisResult = qualityManager.analyzeQuality(selectedUserStories, selectedCriteria);

            if (lastAnalysisResult == null) {
                javafx.application.Platform.runLater(() -> {
                    showError(messages.getString("error.analysisResultNull"));
                    qualityResultsArea.setText(messages.getString("error.analysisResultNullEncountered"));
                });
                return; 
            }

            String report = qualityManager.generateReport(lastAnalysisResult, currentFileName);

            javafx.application.Platform.runLater(() -> {
                if (report != null && !report.isEmpty()) {
                    qualityResultsArea.setText(report);
                } else {
                    qualityResultsArea.setText(messages.getString("info.analysisRanNoIssuesOrNoReport"));
                }
                showInfo(messages.getString("info.qualityAnalysisCompleted") + " " + lastAnalysisResult.getTotalProblems() + " " + messages.getString("info.problemsFound"));
            });

        } catch (Exception e) {
            javafx.application.Platform.runLater(() -> {
                showError(messages.getString("error.qualityAnalysisError") + ": " + e.getMessage());
                qualityResultsArea.setText(messages.getString("error.qualityAnalysisErrorEncountered"));
            });
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
    public void exportQualityReport(Stage primaryStage, File file) {
        if (file != null) {
            if (lastAnalysisResult == null) {
                showInfo(messages.getString("info.noQualityResults"));
                return;
            }
            processExportQualityReport(file);
        }
        // Optional: else block to handle null file if necessary for robustness, though test should provide a valid file.
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
    
    /**
     * Loads the resource bundle for the specified locale and updates UI components.
     * Called when the application starts or when the user changes the language.
     * @param stage The main application window to update
     * @param locale The target locale for localization
     */
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
                qualityResultsArea.setId("qualityResultsArea"); // Ensure ID is set during UI update
            }
            if (graphVisualizationLabel != null) {
                graphVisualizationLabel.setText(messages.getString("label.graphVisualization"));
            }
            if (graphUserStoriesLabel != null) {
                graphUserStoriesLabel.setText(messages.getString("label.selectGraphStory"));
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
                qualityResultsArea.setId("qualityResultsArea"); // Ensure ID is set during UI update
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