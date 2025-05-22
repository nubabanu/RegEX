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

        // Make sure we start with a fresh PID counter
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
        assertEquals(List.of("access", "my", "account"), story.getEntityBenefit(), "EntityBenefit from 'I can access my account'");
        assertEquals(List.of(List.of("User", "login")), story.getTriggers(), "Triggers should be correctly populated.");
        assertEquals(List.of(List.of("login", "unidentified_entity")), story.getTargets(), "Targets should be correctly populated.");
        assertTrue(story.getContains().isEmpty(), "Contains should be empty for this story.");
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

        // Make sure we start with a fresh PID counter
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

        UserStory story2 = stories.get(1);
        assertEquals("#G02#", story2.getPid());
        assertEquals("As a Manager, I want to generate sales reports, so that I can track performance.", story2.getText());
        assertEquals(List.of("Manager"), story2.getPersona());
        assertEquals(List.of("generate"), story2.getActionGoal());
        assertEquals(List.of("sales", "reports"), story2.getEntityGoal());
        assertEquals("I can track performance", story2.getBenefit());
        assertEquals(List.of("track"), story2.getActionBenefit());
        assertEquals(List.of("performance"), story2.getEntityBenefit());
    }

    @Test
    void testReadUserStories_malformedStoryTxt() throws IOException {
        Path testFile = tempDir.resolve("malformed_story.txt");
        String content = "This is not a user story.";
        Files.writeString(testFile, content);

        // Make absolutely sure we start with a fresh PID counter
        manager = new UserStoryManager();
        
        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(1, stories.size(), "Should still process the line and create a story object.");
        UserStory story = stories.get(0);
        // PID #G02# is expected due to UserStoryManager calling generatePid() twice for malformed stories.
        // First call for initial UserStory object (gets #G01# internally), second for partiallyParsedStory (gets #G02# and is returned).
        assertEquals("#G02#", story.getPid(), "PID for single malformed story with fresh manager.");
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
        // manager is from setUp(), pidCounter starts at 1.
        List<UserStory> stories = manager.readUserStories(testFile.toString());
        assertEquals(3, stories.size(), "Should read three entries (2 valid, 1 malformed, empty line ignored).");

        // Check first valid story
        UserStory story1 = stories.get(0);
        assertEquals("#G01#", story1.getPid()); // First story, PID is #G01#
        assertEquals("As a User, I want to logout.", story1.getText());
        assertEquals(List.of("User"), story1.getPersona());
        assertEquals(List.of("logout"), story1.getActionGoal());
        assertEquals(List.of("unidentified_entity"), story1.getEntityGoal()); // "logout" is action, no further entities
        assertEquals("", story1.getBenefit());

        // Check malformed story
        UserStory story2 = stories.get(1);
        // PID for malformed: manager's pidCounter was 2. parseUserStoryFromText calls generatePid() (becomes 3, #G02#),
        // then again for partiallyParsedStory (becomes 4, #G03#). So, #G03#.
        assertEquals("#G03#", story2.getPid());
        assertEquals("This is another malformed line.", story2.getText());
        assertEquals(List.of("Unknown"), story2.getPersona());
        assertEquals(List.of("Unknown"), story2.getActionGoal());
        assertEquals(List.of("Unknown"), story2.getEntityGoal());

        // Check second valid story
        UserStory story3 = stories.get(2);
        // PID for third story (second valid): manager's pidCounter was 4. generatePid() called once (becomes 5, #G04#).
        assertEquals("#G04#", story3.getPid());
        assertEquals("As an Editor, I want to publish an article, so that readers can see it.", story3.getText());
        assertEquals(List.of("Editor"), story3.getPersona());
        assertEquals(List.of("publish"), story3.getActionGoal());
        assertEquals(List.of("article"), story3.getEntityGoal());
        assertEquals("readers can see it", story3.getBenefit());
        assertEquals(List.of("readers"), story3.getActionBenefit()); // extractActions takes first word for non "I can"
        assertEquals(List.of("see"), story3.getEntityBenefit()); // extractEntities skips first, "can" and "it" are stopwords
    }

    @Test
    void testSaveAndReadJson() throws IOException {
        Path jsonFile = tempDir.resolve("test_stories.json");
        manager = new UserStoryManager(); // Reset manager for predictable PIDs if generating them here

        UserStory story1 = new UserStory();
        story1.setPid("#G01#"); // Manually set for test consistency
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
        story2.setPid("#G02#"); // Manually set
        story2.setText("As a User, I want to see my profile, so that I can update my details.");
        story2.setPersona(List.of("User"));
        story2.setActionGoal(List.of("see"));
        story2.setEntityGoal(List.of("my", "profile"));
        story2.setBenefit("I can update my details");
        story2.setActionBenefit(List.of("update"));
        story2.setEntityBenefit(List.of("my", "details"));
        story2.setTriggers(List.of(List.of("User", "see")));
        story2.setTargets(List.of(List.of("see", "my"))); // Assuming first entity after action
        story2.setContains(List.of());


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
        // This test focuses on the regex and overall structure parsing,
        // and the placeholder nature of extractActions/Entities for complex phrases.
        String line = "As a Data Analyst, I want to generate complex monthly sales reports with detailed charts, so that I can present findings to the board effectively.";
        manager = new UserStoryManager(); // Fresh manager for predictable PID
        Path testFile = tempDir.resolve("complex_story.txt");
        try {
            Files.writeString(testFile, line);
            List<UserStory> stories = manager.readUserStories(testFile.toString());
            assertEquals(1, stories.size());
            UserStory story = stories.get(0);

            assertEquals("#G01#", story.getPid());
            assertEquals(List.of("Data Analyst"), story.getPersona());
            assertEquals(List.of("generate"), story.getActionGoal()); 
            // Entities from "complex monthly sales reports with detailed charts" after removing "generate" and stopwords
            assertEquals(List.of("complex", "monthly", "sales", "reports", "detailed", "charts"), story.getEntityGoal());
            assertEquals("I can present findings to the board effectively", story.getBenefit());
            assertEquals(List.of("present"), story.getActionBenefit()); 
            // Entities from "findings to the board effectively" after removing "I can present" and stopwords
            assertEquals(List.of("findings", "board", "effectively"), story.getEntityBenefit());
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
        assertEquals(List.of("campaign", "results"), story.getEntityGoal()); // "results" not a stopword
        assertEquals("the marketing budget is optimized", story.getBenefit());
        // extractActions for "the marketing budget is optimized" -> "the"
        assertEquals(List.of("the"), story.getActionBenefit());
        // extractEntities for "the marketing budget is optimized" (skips "the") -> "marketing", "budget", "optimized"
        // "is" is a stopword.
        assertEquals(List.of("marketing", "budget", "optimized"), story.getEntityBenefit());
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
        // "the", "new" are not stopwords in the current list, but "the" is.
        // "the" is a stopword. "new" is not.
        assertEquals(List.of("new", "network", "firewall", "rules"), story.getEntityGoal());
        assertEquals("", story.getBenefit());
    }
}
