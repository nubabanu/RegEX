package de.uni_marburg.sp25;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestJsonSaveFix {
    public static void main(String[] args) {
        try {
            UserStoryManager manager = new UserStoryManager();
            
            // Load user stories from the test file
            List<UserStory> userStories = manager.readUserStories("test_fix.txt");
            
            // Save to JSON using the clean method
            String outputPath = "test_output_clean.json";
            manager.saveToJson(userStories, outputPath);
            
            // Read the saved file and check if it contains the prefix
            String jsonContent = Files.readString(Paths.get(outputPath));
            
            System.out.println("=== JSON Content ===");
            System.out.println(jsonContent);
            System.out.println("=== End of JSON Content ===");
            
            // Check if the prefix is present
            if (jsonContent.startsWith("JSON Representation of")) {
                System.out.println("❌ FAIL: JSON file still contains the unwanted prefix!");
            } else if (jsonContent.trim().startsWith("[")) {
                System.out.println("✅ SUCCESS: JSON file contains clean JSON without prefix!");
            } else {
                System.out.println("⚠️  WARNING: Unexpected JSON format");
            }
            
            // Verify JSON structure
            if (jsonContent.contains("\"PID\"") && jsonContent.contains("\"Text\"") && 
                jsonContent.contains("\"Action.Goal\"") && jsonContent.contains("\"Entity.Goal\"")) {
                System.out.println("✅ SUCCESS: JSON contains expected user story fields!");
            } else {
                System.out.println("❌ FAIL: JSON missing expected user story fields!");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
