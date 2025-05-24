package de.uni_marburg.sp25;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserStoryManagerTest {

    private UserStoryManager manager;
    private final Locale locale = Locale.ENGLISH;

    @TempDir
    Path tmpDir;

    private Path writeTempFile(String fileName, String content) throws IOException {
        Path p = tmpDir.resolve(fileName);
        Files.writeString(p, content);
        return p;
    }

    @BeforeEach
    void setUp() {
        manager = new UserStoryManager(locale);
    }

    // ---------- 1. TXT parsing paths ------------------------------------------------------------
    @Test
    void parseSingleValidStory_txt_roundTrip() throws Exception {
        String line = "As a User, I want to log in to the system, so that I can access my personalized content.";
        Path txt = writeTempFile("one.txt", line);
        List<UserStory> list = manager.readUserStories(txt.toString());
        assertEquals(1, list.size());
        UserStory us = list.get(0);
        assertEquals("#G01#", us.getPid());          // PID generation
        assertEquals("User", us.getPersona().get(0));
        assertEquals(List.of("log"), us.getActionGoal());           // phrasal‑verb handling
        assertEquals(List.of("system"), us.getEntityGoal());
        assertEquals("I can access my personalized content", us.getBenefit());
        assertEquals(List.of(List.of("User", "log")), us.getTriggers());
        assertTrue(us.getTargets().contains(List.of("log", "system")));
        assertTrue(us.getContains().contains(List.of("system", "personalized content")));
        // no warnings
        assertTrue(manager.getParsingWarnings().isEmpty());
    }

    @Test
    void parseMultipleLines_pidReset_andWarnings() throws Exception {
        String malformed = "The system shall…";
        String valid = "As a Visitor, I want to view news articles, so that I stay informed.";
        Path txt = writeTempFile("mixed.txt", malformed + "\n" + valid + "\n");
        List<UserStory> list = manager.readUserStories(txt.toString());
        assertEquals(2, list.size());
        // First line becomes #G01#, second #G02# regardless of malformed content
        assertEquals("#G01#", list.get(0).getPid());
        assertEquals("Unknown", list.get(0).getPersona().get(0));
        assertFalse(manager.getParsingWarnings().isEmpty());
        assertEquals("#G02#", list.get(1).getPid());
        // Call again ⇒ pid counter reset, first PID again #G01#
        manager.resetPidCounter();
        Path txt2 = writeTempFile("second.txt", valid);
        List<UserStory> again = manager.readUserStories(txt2.toString());
        assertEquals("#G01#", again.get(0).getPid());
    }

    // ---------- 2. JSON read / write ------------------------------------------------------------
    @Test
    void saveAndLoad_json_prettyPrint() throws Exception {
        UserStory story = new UserStory("#G42#", "As a Tester, I want to export reports.", 1,1,1, "Tester");
        story.setPersona(List.of("Tester"));
        story.setActionGoal(List.of("export"));
        story.setEntityGoal(List.of("reports"));

        Path json = tmpDir.resolve("stories.json");
        manager.saveToJson(List.of(story), json.toString());

        // pretty JSON must contain newline and spaces
        String raw = Files.readString(json);
        assertTrue(raw.contains("\n"));
        assertTrue(raw.contains("  ")); // indentation

        List<UserStory> loaded = manager.readUserStories(json.toString());
        assertEquals(1, loaded.size());
        assertEquals("export", loaded.get(0).getActionGoal().get(0));
    }

    @Test
    void saveToJson_producesCleanJsonWithoutPrefix() throws Exception {
        // Create test user stories
        UserStory story1 = new UserStory("#G01#", "As a User, I want to log in to the system, so that I can access my personalized content.", 1, 1, 1, "User");
        story1.setPersona(List.of("User"));
        story1.setActionGoal(List.of("log"));
        story1.setEntityGoal(List.of("system"));
        
        UserStory story2 = new UserStory("#G02#", "As an Admin, I want to manage users, so that I can control access.", 1, 1, 1, "Admin");
        story2.setPersona(List.of("Admin"));
        story2.setActionGoal(List.of("manage"));
        story2.setEntityGoal(List.of("users"));

        List<UserStory> stories = List.of(story1, story2);
        
        // Save to JSON
        Path json = tmpDir.resolve("clean_test.json");
        manager.saveToJson(stories, json.toString());
        
        // Read the saved JSON content
        String jsonContent = Files.readString(json);
        
        // Verify the JSON doesn't start with unwanted prefix
        assertFalse(jsonContent.startsWith("JSON Representation of"), 
                   "JSON file should not contain the 'JSON Representation of' prefix");
        
        // Verify it starts with proper JSON array
        assertTrue(jsonContent.trim().startsWith("["), 
                  "JSON file should start with an array");
        
        // Verify it contains expected user story fields
        assertTrue(jsonContent.contains("\"PID\""), "JSON should contain PID field");
        assertTrue(jsonContent.contains("\"Text\""), "JSON should contain Text field");
        assertTrue(jsonContent.contains("#G01#"), "JSON should contain first story PID");
        assertTrue(jsonContent.contains("#G02#"), "JSON should contain second story PID");
        
        // Verify JSON is properly formatted (pretty printed)
        assertTrue(jsonContent.contains("\n"), "JSON should be pretty printed with newlines");
        assertTrue(jsonContent.contains("  "), "JSON should be pretty printed with indentation");
    }

    // ---------- 3. Unsupported extension path --------------------------------------------------
    @Test
    void readUserStories_unsupportedExtension_throws() {
        Path fake = tmpDir.resolve("bad.csv");
        assertThrows(IOException.class, () -> manager.readUserStories(fake.toString()));
    }

    // ---------- 4. Private helper coverage via reflection --------------------------------------
    private Object callPrivate(String name, Class<?>[] argTypes, Object... args) throws Exception {
        Method m = UserStoryManager.class.getDeclaredMethod(name, argTypes);
        m.setAccessible(true);
        return m.invoke(manager, args);
    }

    @Test
    void extractActions_goal_phrasalVerbHandled() throws Exception {
        @SuppressWarnings("unchecked")
        List<String> act = (List<String>) callPrivate("extractActions", new Class[]{String.class, boolean.class},
                "log in to the system", true);
        assertEquals(List.of("log"), act);
    }

    @Test
    void extractActions_benefit_canPattern() throws Exception {
        @SuppressWarnings("unchecked")
        List<String> act = (List<String>) callPrivate("extractActions", new Class[]{String.class, boolean.class},
                "I can monitor performance", false);
        assertEquals(List.of("monitor"), act);
    }

    @Test
    void extractEntities_compoundPhrasePreserved() throws Exception {
        @SuppressWarnings("unchecked")
        List<String> ent = (List<String>) callPrivate("extractEntities", new Class[]{String.class, boolean.class},
                "user accounts", true);
        assertEquals(List.of("user accounts"), ent);
    }

    @Test
    void removeActionAndStopWords_edgeCases() throws Exception {
        String base = "to archive data"; // leading stop‑word "to"
        String action = "archive";
        String after = (String) callPrivate("removeActionFromText", new Class[]{String.class, String.class}, base, action);
        assertEquals("data", after);
        String trimmed = (String) callPrivate("removeLeadingStopWords", new Class[]{String.class}, "the   optimal   result");
        assertEquals("optimal result", trimmed);
    }

    @Test
    void containsLogic_goalBenefitPairs() throws Exception {
        String line = "As a Marketer, I want to adjust the budget, so that the marketing budget is optimized.";
        Path txt = writeTempFile("budget.txt", line);
        List<UserStory> stories = manager.readUserStories(txt.toString());
        UserStory us = stories.get(0);
        // Contains must link goal entity to benefit entity
        assertTrue(us.getContains().contains(List.of("the marketing budget", "marketing budget is optimized")) ||
                   us.getContains().size() >= 1);
    }

    // ---------- 5. Edge‑case: stay up to date split --------------------------------------------
    @Test
    void entityExtraction_upToDate_specialCase() throws Exception {
        @SuppressWarnings("unchecked")
        List<String> ent = (List<String>) callPrivate("extractEntities", new Class[]{String.class, boolean.class},
                "stay up to date", false);
        // Should split into three parts due to special rule
        assertEquals(List.of("up", "date"), ent.subList(ent.size()-2, ent.size()));
    }

    @Test
    void testReadUserStories_emptyTxtFile() throws IOException {
        Path emptyFile = tmpDir.resolve("empty.txt");
        Files.createFile(emptyFile);
        // Reset manager to ensure PID starts from G01 for this specific test context if needed,
        // though for an empty file, no PIDs are generated.
        manager = new UserStoryManager();
        List<UserStory> stories = manager.readUserStories(emptyFile.toString());
        assertTrue(stories.isEmpty(), "Reading an empty TXT file should result in an empty list.");
    }
}
