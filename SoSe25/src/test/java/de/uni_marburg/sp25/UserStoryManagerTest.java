package de.uni_marburg.sp25;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserStoryManagerTest {

    private UserStoryManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserStoryManager(); // Creates a fresh manager with PID counter at 1
    }

    @TempDir
    Path tempDir; // JUnit 5 temporary directory

    @Test
    void testReadUserStories_emptyTxtFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);
        // Reset manager to ensure PID starts from G01 for this specific test context if needed,
        // though for an empty file, no PIDs are generated.
        manager = new UserStoryManager();
        List<UserStory> stories = manager.readUserStories(emptyFile.toString());
        assertTrue(stories.isEmpty(), "Reading an empty TXT file should result in an empty list.");
    }

    @Test
    void testReadUserStories_singleWellFormedStoryTxt() throws IOException {
        Path testFile = tempDir.resolve("single_story.txt");
        String content = "As a User, I want to login, so that I can access my account.";
        Files.writeString(testFile, content);

        manager = new UserStoryManager();
        
        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(1, stories.size(), "Should read one user story.");

        UserStory story = stories.get(0);
        assertEquals("#G01#", story.getPid());
        assertEquals("As a User, I want to login, so that I can access my account.", story.getText());
        assertEquals(List.of("User"), story.getPersona());
        assertEquals(List.of("login"), story.getActionGoal());
        assertEquals(List.of("unidentified_entity"), story.getEntityGoal(), "EntityGoal for 'I want to login'");
        assertEquals("I can access my account", story.getBenefit());
        assertEquals(List.of("access"), story.getActionBenefit(), "ActionBenefit from 'I can access my account'");
        assertEquals(List.of("account"), story.getEntityBenefit(), "EntityBenefit from 'I can access my account'");
        assertEquals(List.of(List.of("User", "login")), story.getTriggers(), "Triggers should be correctly populated.");
        // Targets: [[ActionGoal, EntityGoal], [ActionBenefit, EntityBenefit[0]], [ActionBenefit, EntityBenefit[1]]...]
        assertEquals(List.of(
                List.of("login", "unidentified_entity"),
                List.of("access", "account")
        ), story.getTargets(), "Targets should be correctly populated.");
        // Contains: [[EntityGoal, EntityBenefit[0]], [EntityBenefit[0], EntityBenefit[1]]...]
        // If EntityGoal is "unidentified_entity", it might not form "Contains" relationships.
        // Or, if "my account" is seen as contained within the broader context of "login".
        // Based on the provided example, "Information" (goal) contains "publicly available information" (benefit).
        // So, "unidentified_entity" (goal) could contain "my account" (benefit).
        assertEquals(List.of(List.of("unidentified_entity", "account")), story.getContains(), "Contains relationship between goal entity and benefit entity.");
    }

    @Test
    void testReadUserStories_storyWithoutBenefitTxt() throws IOException {
        Path testFile = tempDir.resolve("no_benefit_story.txt");
        String content = "As an Admin, I want to create a report.";
        Files.writeString(testFile, content);

        // Make sure we start with a fresh PID counter
        manager = new UserStoryManager();
        
        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(1, stories.size());

        UserStory story = stories.get(0);
        assertEquals("#G01#", story.getPid());
        assertEquals("As an Admin, I want to create a report.", story.getText());
        assertEquals(List.of("Admin"), story.getPersona());
        assertEquals(List.of("create"), story.getActionGoal());
        assertEquals(List.of("report"), story.getEntityGoal());
        assertEquals("", story.getBenefit(), "Benefit should be empty if not provided.");
        assertTrue(story.getActionBenefit().isEmpty(), "ActionBenefit should be empty if no benefit.");
        assertTrue(story.getEntityBenefit().isEmpty(), "EntityBenefit should be empty if no benefit.");
        assertEquals(List.of(List.of("Admin", "create")), story.getTriggers());
        assertEquals(List.of(List.of("create", "report")), story.getTargets());
        assertTrue(story.getContains().isEmpty());
    }

    @Test
    void testReadUserStories_multipleStoriesTxt() throws IOException {
        Path testFile = tempDir.resolve("multiple_stories.txt");
        String content = "As a Customer, I want to view products.\n" +
                         "As a Manager, I want to generate sales reports, so that I can track performance.";
        Files.writeString(testFile, content);

        manager = new UserStoryManager();
        
        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(2, stories.size(), "Should read two user stories.");

        UserStory story1 = stories.get(0);
        assertEquals("#G01#", story1.getPid());
        assertEquals("As a Customer, I want to view products.", story1.getText());
        assertEquals(List.of("Customer"), story1.getPersona());
        assertEquals(List.of("view"), story1.getActionGoal());
        assertEquals(List.of("products"), story1.getEntityGoal());
        assertEquals("", story1.getBenefit());
        assertEquals(List.of(List.of("Customer", "view")), story1.getTriggers());
        assertEquals(List.of(List.of("view", "products")), story1.getTargets());
        assertTrue(story1.getContains().isEmpty());

        UserStory story2 = stories.get(1);
        assertEquals("#G02#", story2.getPid());
        assertEquals("As a Manager, I want to generate sales reports, so that I can track performance.", story2.getText());
        assertEquals(List.of("Manager"), story2.getPersona());
        assertEquals(List.of("generate"), story2.getActionGoal());
        assertEquals(List.of("sales reports"), story2.getEntityGoal());
        assertEquals("I can track performance", story2.getBenefit());
        assertEquals(List.of("track"), story2.getActionBenefit());
        assertEquals(List.of("performance"), story2.getEntityBenefit());
        assertEquals(List.of(List.of("Manager", "generate")), story2.getTriggers());
        assertEquals(List.of(
                List.of("generate", "sales reports"),
                List.of("track", "performance")
        ), story2.getTargets());
        assertEquals(List.of(List.of("sales reports", "performance")), story2.getContains());
    }

    @Test
    void testReadUserStories_malformedStoryTxt() throws IOException {
        Path testFile = tempDir.resolve("malformed_story.txt");
        String content = "This is not a user story.";
        Files.writeString(testFile, content);

        manager = new UserStoryManager();
        
        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(1, stories.size(), "Should still process the line and create a story object.");
        UserStory story = stories.get(0);
        // Malformed stories should get #G01# like any other story - no double PID generation
        assertEquals("#G01#", story.getPid(), "PID for single malformed story should be #G01#.");
        assertEquals("This is not a user story.", story.getText());
        assertEquals(List.of("Unknown"), story.getPersona(), "Persona should be 'Unknown' for malformed story.");
        assertEquals(List.of("Unknown"), story.getActionGoal(), "ActionGoal should be 'Unknown'.");
        assertEquals(List.of("Unknown"), story.getEntityGoal(), "EntityGoal should be 'Unknown'.");
        assertEquals("Unknown", story.getBenefit(), "Benefit should be 'Unknown'.");
        assertTrue(story.getActionBenefit().isEmpty(), "ActionBenefit should be empty.");
        assertTrue(story.getEntityBenefit().isEmpty(), "EntityBenefit should be empty.");
        assertTrue(story.getTriggers().isEmpty(), "Triggers should be empty.");
        assertTrue(story.getTargets().isEmpty(), "Targets should be empty.");
        assertTrue(story.getContains().isEmpty(), "Contains should be empty.");
    }

    @Test
    void testReadUserStories_mixedStoriesTxt() throws IOException {
        Path testFile = tempDir.resolve("mixed_stories.txt");
        String content = "As a User, I want to logout.\n" + // Story 1 (Valid)
                         "\n" + // Empty line
                         "This is another malformed line.\n" + // Story 2 (Malformed)
                         "As an Editor, I want to publish an article, so that readers can see it."; // Story 3 (Valid)
        
        Files.writeString(testFile, content);
        manager = new UserStoryManager();
        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(3, stories.size(), "Should read three entries (2 valid, 1 malformed, empty line ignored).");

        // Check first valid story
        UserStory story1 = stories.get(0);
        assertEquals("#G01#", story1.getPid()); 
        assertEquals("As a User, I want to logout.", story1.getText());
        assertEquals(List.of("User"), story1.getPersona());
        assertEquals(List.of("logout"), story1.getActionGoal());
        assertEquals(List.of("unidentified_entity"), story1.getEntityGoal()); 
        assertEquals("", story1.getBenefit());
        assertEquals(List.of(List.of("User", "logout")), story1.getTriggers());
        assertEquals(List.of(List.of("logout", "unidentified_entity")), story1.getTargets());
        assertTrue(story1.getContains().isEmpty());

        // Check malformed story - should be #G02# in sequence
        UserStory story2 = stories.get(1);
        assertEquals("#G02#", story2.getPid()); 
        assertEquals("This is another malformed line.", story2.getText());
        assertEquals(List.of("Unknown"), story2.getPersona());
        assertEquals(List.of("Unknown"), story2.getActionGoal());
        assertEquals(List.of("Unknown"), story2.getEntityGoal());

        // Check second valid story - should be #G03# in sequence
        UserStory story3 = stories.get(2);
        assertEquals("#G03#", story3.getPid()); 
        assertEquals("As an Editor, I want to publish an article, so that readers can see it.", story3.getText());
        assertEquals(List.of("Editor"), story3.getPersona());
        assertEquals(List.of("publish"), story3.getActionGoal());
        assertEquals(List.of("article"), story3.getEntityGoal());
        assertEquals("readers can see it", story3.getBenefit());
        assertEquals(List.of("see"), story3.getActionBenefit()); 
        assertEquals(List.of("it"), story3.getEntityBenefit()); 
        assertEquals(List.of(List.of("Editor", "publish")), story3.getTriggers());
        assertEquals(List.of(
                List.of("publish", "article"),
                List.of("see", "it")
        ), story3.getTargets());
        assertEquals(List.of(List.of("article", "it")), story3.getContains());
    }

    @Test
    void testSaveAndReadJson() throws IOException {
        Path jsonFile = tempDir.resolve("test_stories.json");
        manager = new UserStoryManager(); // Reset manager for predictable PIDs if generating them here

        UserStory story1 = new UserStory();
        story1.setPid("#G01#"); 
        story1.setText("As a Tester, I want to run tests.");
        story1.setPersona(List.of("Tester"));
        story1.setActionGoal(List.of("run"));
        story1.setEntityGoal(List.of("tests"));
        story1.setBenefit("");
        story1.setActionBenefit(List.of());
        story1.setEntityBenefit(List.of());
        story1.setTriggers(List.of(List.of("Tester", "run")));
        story1.setTargets(List.of(List.of("run","tests")));
        story1.setContains(List.of());

        UserStory story2 = new UserStory();
        story2.setPid("#G02#"); 
        story2.setText("As a User, I want to see my profile, so that I can update my details.");
        story2.setPersona(List.of("User"));
        story2.setActionGoal(List.of("see"));
        story2.setEntityGoal(List.of("my profile"));
        story2.setBenefit("I can update my details");
        story2.setActionBenefit(List.of("update"));
        story2.setEntityBenefit(List.of("my details"));
        story2.setTriggers(List.of(List.of("User", "see")));
        story2.setTargets(List.of(List.of("see", "my profile"), List.of("update", "my details"))); 
        story2.setContains(List.of(List.of("my profile", "my details")));


        List<UserStory> originalStories = List.of(story1, story2);
        manager.saveToJson(originalStories, jsonFile.toString());

        assertTrue(Files.exists(jsonFile), "JSON file should be created.");

        List<UserStory> readStories = manager.readUserStories(jsonFile.toString());
        assertEquals(2, readStories.size(), "Should read two stories from JSON.");

        UserStory readStory1 = readStories.get(0);
        assertEquals(story1.getPid(), readStory1.getPid());
        assertEquals(story1.getText(), readStory1.getText());
        assertEquals(story1.getPersona(), readStory1.getPersona());
        assertEquals(story1.getActionGoal(), readStory1.getActionGoal());
        assertEquals(story1.getEntityGoal(), readStory1.getEntityGoal());
        assertEquals(story1.getBenefit(), readStory1.getBenefit());
        assertEquals(story1.getActionBenefit(), readStory1.getActionBenefit());
        assertEquals(story1.getEntityBenefit(), readStory1.getEntityBenefit());
        assertEquals(story1.getTriggers(), readStory1.getTriggers());
        assertEquals(story1.getTargets(), readStory1.getTargets());
        assertEquals(story1.getContains(), readStory1.getContains());

        UserStory readStory2 = readStories.get(1);
        assertEquals(story2.getPid(), readStory2.getPid());
        assertEquals(story2.getText(), readStory2.getText());
        assertEquals(story2.getPersona(), readStory2.getPersona());
        assertEquals(story2.getActionGoal(), readStory2.getActionGoal());
        assertEquals(story2.getEntityGoal(), readStory2.getEntityGoal());
        assertEquals(story2.getBenefit(), readStory2.getBenefit());
        assertEquals(story2.getActionBenefit(), readStory2.getActionBenefit());
        assertEquals(story2.getEntityBenefit(), readStory2.getEntityBenefit());
        assertEquals(story2.getTriggers(), readStory2.getTriggers());
        assertEquals(story2.getTargets(), readStory2.getTargets());
        assertEquals(story2.getContains(), readStory2.getContains());
    }

    @Test
    void testReadUserStories_unsupportedFileFormat() {
        Path unsupportedFile = tempDir.resolve("test.doc");
        try {
            Files.createFile(unsupportedFile);
        } catch (IOException e) {
            fail("Failed to create temp file for testing unsupported format.");
        }

        IOException exception = assertThrows(IOException.class, () -> {
            manager.readUserStories(unsupportedFile.toString());
        });
        assertTrue(exception.getMessage().contains("Unsupported file format: doc"),
                "Exception message should indicate unsupported format.");
    }

    @Test
    void testParseUserStoryFromText_complexGoalAndBenefit() {
        String line = "As a Data Analyst, I want to generate complex monthly sales reports with detailed charts, so that I can present findings to the board effectively.";
        manager = new UserStoryManager(); 
        Path testFile = tempDir.resolve("complex_story.txt");
        try {
            Files.writeString(testFile, line);
            List<UserStory> stories = manager.readUserStories(testFile.toString());
            assertEquals(1, stories.size());
            UserStory story = stories.get(0);

            assertEquals("#G01#", story.getPid());
            assertEquals(List.of("Data Analyst"), story.getPersona());
            assertEquals(List.of("generate"), story.getActionGoal()); 
            assertEquals(List.of("complex monthly sales reports with detailed charts"), story.getEntityGoal());
            assertEquals("I can present findings to the board effectively", story.getBenefit());
            assertEquals(List.of("present"), story.getActionBenefit()); 
            assertEquals(List.of("findings to the board"), story.getEntityBenefit());
            assertEquals(List.of(List.of("Data Analyst", "generate")), story.getTriggers());
            
            // Targets: ActionGoal with each EntityGoal, ActionBenefit with each EntityBenefit
            assertEquals(List.of(
                    List.of("generate", "complex monthly sales reports with detailed charts"),
                    List.of("present", "findings to the board")
            ), story.getTargets());

            // Contains: Sequential within goals, cross-relationships goal->benefit, sequential within benefits
            assertEquals(List.of(
                    List.of("complex monthly sales reports with detailed charts", "findings to the board")
            ), story.getContains());
        } catch (IOException e) {
            fail("IOException during complex story test: " + e.getMessage());
        }
    }

    @Test
    void testPidGenerationAndReset() throws IOException {
        // Test initial PID generation
        Path testFile1 = tempDir.resolve("pid_test1.txt");
        Files.writeString(testFile1, "As a User, I want a feature.");
        List<UserStory> stories1 = manager.readUserStories(testFile1.toString()); // manager from setUp, pidCounter = 1
        assertEquals("#G01#", stories1.get(0).getPid(), "First PID should be #G01# with a fresh manager instance.");

        // Test subsequent PID generation
        Path testFile2 = tempDir.resolve("pid_test2.txt");
        Files.writeString(testFile2, "As a User, I want another feature.\nAs an Admin, I need a tool.");
        // manager instance is NOT reset, so PID should continue from where testFile1's read left off (pidCounter was 2)
        // However, readUserStories RESETS the counter internally. So this test needs to be careful.
       

        manager = new UserStoryManager(); // Fresh manager, pidCounter = 1
        UserStory s1 = manager.readUserStories(testFile1.toString()).get(0);
        assertEquals("#G01#", s1.getPid());

        // If we use the same manager instance and parse another file, readUserStories resets pidCounter.
        UserStory s2 = manager.readUserStories(testFile1.toString()).get(0); // Same file, but readUserStories resets counter
        assertEquals("#G01#", s2.getPid(), "PID should reset to #G01# for a new readUserStories call.");

        // Test resetPidCounter method explicitly
        manager.resetPidCounter();
        // To test generatePid, we need to simulate parsing or make generatePid package-private/public
        // For now, we rely on readUserStories which uses generatePid
        UserStory s3 = manager.readUserStories(testFile1.toString()).get(0);
        assertEquals("#G01#", s3.getPid(), "PID should be #G01# after explicit reset and new read.");
    }

    @Test
    void testReadUserStories_emptyJsonFile() throws IOException {
        Path emptyJsonFile = tempDir.resolve("empty.json");
        Files.writeString(emptyJsonFile, "[]"); // Empty JSON array
        manager = new UserStoryManager();
        List<UserStory> stories = manager.readUserStories(emptyJsonFile.toString());
        assertTrue(stories.isEmpty(), "Reading an empty JSON array should result in an empty list.");
    }

    @Test
    void testReadUserStories_malformedJsonFile() throws IOException {
        Path malformedJsonFile = tempDir.resolve("malformed.json");
        Files.writeString(malformedJsonFile, "[{\"PID\": \"#G01#\", \"Text\": \"Test\"}"); // Missing closing bracket and brace
        manager = new UserStoryManager();
        assertThrows(IOException.class, () -> {
            manager.readUserStories(malformedJsonFile.toString());
        }, "Reading a malformed JSON file should throw IOException.");
    }
    
    @Test
    void testParseUserStory_benefitWithoutICanPattern() throws IOException {
        Path testFile = tempDir.resolve("benefit_no_ican.txt");
        String content = "As a Marketer, I want to track campaign results, so that the marketing budget is optimized.";
        Files.writeString(testFile, content);
        manager = new UserStoryManager();

        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(1, stories.size());
        UserStory story = stories.get(0);

        assertEquals("#G01#", story.getPid());
        assertEquals(List.of("Marketer"), story.getPersona());
        assertEquals(List.of("track"), story.getActionGoal());
        assertEquals(List.of("campaign results"), story.getEntityGoal()); 
        assertEquals("the marketing budget is optimized", story.getBenefit());
        assertEquals(List.of("optimized"), story.getActionBenefit());
        assertEquals(List.of("the marketing budget"), story.getEntityBenefit());
        assertEquals(List.of(List.of("Marketer", "track")), story.getTriggers());
        assertEquals(List.of(
                List.of("track", "campaign results"),
                List.of("optimized", "the marketing budget")
        ), story.getTargets());
        assertEquals(List.of(List.of("campaign results", "the marketing budget")), story.getContains());
    }

    @Test
    void testReadUserStories_storyWithComplexActionAndEntityInGoal() throws IOException {
        Path testFile = tempDir.resolve("complex_goal_story.txt");
        String content = "As a System Administrator, I want to configure the new network firewall rules.";
        Files.writeString(testFile, content);
        manager = new UserStoryManager();

        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(1, stories.size());
        UserStory story = stories.get(0);

        assertEquals("#G01#", story.getPid());
        assertEquals(List.of("System Administrator"), story.getPersona());
        assertEquals(List.of("configure"), story.getActionGoal());
        assertEquals(List.of("new network firewall rules"), story.getEntityGoal());
        assertEquals("", story.getBenefit());
        assertEquals(List.of(List.of("System Administrator", "configure")), story.getTriggers());
        assertEquals(List.of(List.of("configure", "new network firewall rules")), story.getTargets());
        assertTrue(story.getContains().isEmpty());
    }

    @Test
    void testReadUserStories_providedExamplePublicUser() throws IOException {
        Path testFile = tempDir.resolve("public_user_story.txt");
        String content = "As a Public User, I want to Search for Information, so that I can obtain publicly available information concerning properties, County services, processes and other general information.";
        Files.writeString(testFile, content);

        manager = new UserStoryManager();

        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(1, stories.size(), "Should read one user story.");

        UserStory story = stories.get(0);
        assertEquals("#G01#", story.getPid());
        assertEquals(content, story.getText());
        assertEquals(List.of("Public User").toString(), story.getPersona().toString());
        assertEquals(List.of("Search").toString(), story.getActionGoal().toString());
        assertEquals(List.of("Information").toString(), story.getEntityGoal().toString());
        assertEquals("I can obtain publicly available information concerning properties, County services, processes and other general information", story.getBenefit());
        assertEquals(List.of("obtain").toString(), story.getActionBenefit().toString());
        
        // The actual parsing splits the long string into multiple entities
        List<String> expectedEntityBenefitList = List.of(
            "publicly available information concerning properties", 
            "County services", 
            "processes and other general information"
        );
        assertEquals(expectedEntityBenefitList.toString(), story.getEntityBenefit().toString());

        assertEquals(List.of(List.of("Public User", "Search")).toString(), story.getTriggers().toString());

        // Targets: ActionGoal->EntityGoal, ActionBenefit->each EntityBenefit
        List<List<String>> expectedTargetsList = List.of(
                List.of("Search", "Information"),
                List.of("obtain", "publicly available information concerning properties"),
                List.of("obtain", "County services"),
                List.of("obtain", "processes and other general information")
        );
        assertEquals(expectedTargetsList.toString(), story.getTargets().toString());

        // Contains: Goal->each Benefit entity, sequential relationships within benefit entities
        List<List<String>> expectedContainsList = List.of(
                List.of("Information", "publicly available information concerning properties"),
                List.of("Information", "County services"),
                List.of("Information", "processes and other general information"),
                List.of("publicly available information concerning properties", "County services"),
                List.of("County services", "processes and other general information")
        );
        assertEquals(expectedContainsList.toString(), story.getContains().toString());
    }
}
