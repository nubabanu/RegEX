package de.uni_marburg.sp25;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
public class UserStoryAppE2ETest {

    private UserStoryApp app;
    private Stage primaryStage;

    private final String DUMMY_TXT_PATH = "SoSe25/src/test/resources/dummy_story.txt";
    private final String DUMMY_JSON_PATH = "SoSe25/src/test/resources/dummy_story.json";
    private final String TEST_OUTPUT_JSON_PATH = "test_output.json";
    private final String TEST_REPORT_PATH = "quality_report.txt";

    @Start
    private void start(Stage stage) throws Exception {
        primaryStage = stage;
        app = new UserStoryApp();
        app.start(primaryStage);
    }

    @BeforeEach
    void setUp() {
        // Ensure files from previous tests are cleaned up
        try {
            Files.deleteIfExists(Paths.get(TEST_OUTPUT_JSON_PATH));
            Files.deleteIfExists(Paths.get(TEST_REPORT_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        // Clean up any created files during the test to avoid interference
        try {
            Files.deleteIfExists(Paths.get(TEST_OUTPUT_JSON_PATH));
            Files.deleteIfExists(Paths.get(TEST_REPORT_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testFullApplicationFlow(FxRobot robot) throws InterruptedException, TimeoutException {
        // 1. Initial state check
        verifyThat("#mainTabPane", NodeMatchers.isVisible());
        verifyThat("#fileOperationsTab", NodeMatchers.isVisible());
        verifyThat("#qualityAnalysisTab", NodeMatchers.isVisible());

        // 2. Load TXT file
        robot.interact(() -> {
            File dummyTxtFile = new File(DUMMY_TXT_PATH);
            app.loadFile(primaryStage, "txt", dummyTxtFile);
        });
        waitForFxEvents();
        verifyThat("#txtOutputArea", (TextArea textArea) -> !textArea.getText().isEmpty());
        verifyThat("#jsonOutputArea", (TextArea textArea) -> !textArea.getText().isEmpty());

        // 3. Save JSON to file
        robot.interact(() -> {
            File testJsonFile = new File(TEST_OUTPUT_JSON_PATH);
            app.saveJsonToFile(primaryStage, testJsonFile);
        });
        waitForFxEvents();
        Path savedJsonPath = Paths.get(TEST_OUTPUT_JSON_PATH);
        assert(Files.exists(savedJsonPath));

        // 4. Clear outputs
        robot.clickOn("#clearButton");
        waitForFxEvents();
        verifyThat("#txtOutputArea", (TextArea textArea) -> textArea.getText().isEmpty());
        verifyThat("#jsonOutputArea", (TextArea textArea) -> textArea.getText().isEmpty());

        // 5. Load JSON file
        robot.interact(() -> {
            File dummyJsonFile = new File(DUMMY_JSON_PATH);
            app.loadFile(primaryStage, "json", dummyJsonFile);
        });
        waitForFxEvents();
        verifyThat("#jsonOutputArea", (TextArea textArea) -> !textArea.getText().isEmpty());
        verifyThat("#userStoriesListView", (ListView<UserStory> listView) -> !listView.getItems().isEmpty());
        verifyThat("#graphUserStoriesListView", (ListView<UserStory> listView) -> !listView.getItems().isEmpty());

        // 6. Switch to Quality Analysis Tab
        robot.clickOn((Node) robot.lookup(".tab-pane > .tab-header-area .tab").nth(1).query());
        waitForFxEvents();
        verifyThat("#qualityAnalysisLabel", NodeMatchers.isVisible());

        // 7. Select a user story for analysis
        robot.interact(() -> {
            ListView<UserStory> listView = robot.lookup("#userStoriesListView").queryListView();
            if (!listView.getItems().isEmpty()) {
                listView.getSelectionModel().select(0);
            }
        });
        waitForFxEvents();

        // 8. Select quality criteria (e.g., select all)
        robot.clickOn("#selectAllCriteriaButton");
        waitForFxEvents();

        // 9. Perform quality analysis
        robot.clickOn("#analyzeQualityButton");
        waitForFxEvents(); // Initial wait for click processing

        // Wait for the Platform.runLater() UI update to complete with extended timeout
        TextArea qaResultsAreaNode = null;
        try {
            qaResultsAreaNode = WaitForAsyncUtils.asyncFx(() -> {
                TextArea node = robot.lookup("#qualityResultsArea").queryAs(TextArea.class);
                // Ensure node is not null, visible, and its text is not empty.
                if (node != null && node.isVisible() && node.getText() != null && !node.getText().isEmpty()) {
                    return node;
                }
                return null; // Continue waiting if conditions not met
            }).get(15, TimeUnit.SECONDS); // Wait up to 15 seconds to match UserStoryAppTest
        } catch (ExecutionException e) {
            // Handle or rethrow if appropriate for the test's design
            e.printStackTrace(); // Basic error handling
            org.junit.jupiter.api.Assertions.fail("ExecutionException during async wait for #qualityResultsArea: " + e.getMessage());
        } catch (TimeoutException e) {
            // Handle timeout exception specifically
            e.printStackTrace();
            org.junit.jupiter.api.Assertions.fail("Timeout waiting for quality results area to be populated: " + e.getMessage());
        }

        assertNotNull(qaResultsAreaNode, "QualityResultsArea node should be found, visible, and not empty.");

        // 10. Export quality report
        robot.interact(() -> {
            File reportFile = new File(TEST_REPORT_PATH);
            app.exportQualityReport(primaryStage, reportFile);
        });
        waitForFxEvents();
        Path reportPath = Paths.get(TEST_REPORT_PATH);
        assert(Files.exists(reportPath));

        // 11. Switch back to File Operations Tab and test graph visualization
        robot.clickOn((Node) robot.lookup(".tab-pane > .tab-header-area .tab").nth(0).query());
        waitForFxEvents();

        // 12. Select a user story for graph visualization
        robot.interact(() -> {
            ListView<UserStory> listView = robot.lookup("#graphUserStoriesListView").queryListView();
            if (!listView.getItems().isEmpty()) {
                listView.getSelectionModel().select(0);
            }
        });
        waitForFxEvents();

    }
}
