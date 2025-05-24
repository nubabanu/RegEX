package de.uni_marburg.sp25;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

class JsonSaveFixTest {
    
    @Test
    void testSaveJsonWithoutPrefix(@TempDir Path tempDir) throws Exception {
        UserStoryManager manager = new UserStoryManager(Locale.ENGLISH);
        
        // Create a test text file
        Path testTxtFile = tempDir.resolve("test.txt");
        Files.writeString(testTxtFile, "As a User, I want to log in to the system, so that I can access my personalized content.\n" +
                                      "As an Administrator, I want to manage user accounts, so that I can ensure system security.");
        
        // Load user stories from the text file
        List<UserStory> userStories = manager.readUserStories(testTxtFile.toString());
        
        // Save to JSON using the clean method
        Path outputJsonFile = tempDir.resolve("clean_output.json");
        manager.saveToJson(userStories, outputJsonFile.toString());
        
        // Read the saved file and verify it doesn't contain the prefix
        String jsonContent = Files.readString(outputJsonFile);
        
        System.out.println("=== Generated JSON Content ===");
        System.out.println(jsonContent);
        System.out.println("=== End of JSON Content ===");
        
        // Verify the JSON doesn't start with the unwanted prefix
        assertFalse(jsonContent.startsWith("JSON Representation of"), 
                   "JSON file should not contain the 'JSON Representation of' prefix");
        
        // Verify it starts with proper JSON array
        assertTrue(jsonContent.trim().startsWith("["), 
                  "JSON file should start with an array");
        
        // Verify it contains expected user story fields
        assertTrue(jsonContent.contains("\"PID\""), "JSON should contain PID field");
        assertTrue(jsonContent.contains("\"Text\""), "JSON should contain Text field");
        assertTrue(jsonContent.contains("\"Action.Goal\""), "JSON should contain Action.Goal field");
        assertTrue(jsonContent.contains("\"Entity.Goal\""), "JSON should contain Entity.Goal field");
        
        // Verify we have the expected number of user stories
        assertEquals(2, userStories.size(), "Should have parsed 2 user stories");
    }
}
