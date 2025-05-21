package de.uni_marburg.sp25;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages user stories, including reading from files and saving to JSON.
 */
public class UserStoryManager {

    private int pidCounter = 1; // Counter for generating unique PIDs

    /**
     * Reads user stories from a file.
     *
     * @param filePath the path to the file
     * @return a list of user stories
     * @throws IOException if an I/O error occurs
     */
    public List<UserStory> readUserStories(String filePath) throws IOException {
        List<UserStory> userStories = new ArrayList<>();
        String fileExtension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();

        if ("txt".equals(fileExtension)) {
            // Read each line as a separate user story
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    UserStory userStory = parseUserStory(line.trim());
                    if (userStory != null) {
                        userStories.add(userStory);
                    }
                }
            }
        } else if ("json".equals(fileExtension)) {
            // Read JSON file
            ObjectMapper mapper = new ObjectMapper();
            userStories = mapper.readValue(new File(filePath), mapper.getTypeFactory().constructCollectionType(List.class, UserStory.class));
        } else {
            throw new IOException("Unsupported file format: " + fileExtension);
        }

        return userStories;
    }

    /**
     * Saves a list of user stories to a JSON file.
     *
     * @param userStories the list of user stories to save
     * @param outputPath  the path to the output JSON file
     * @throws IOException if an I/O error occurs
     */
    public void saveToJson(List<UserStory> userStories, String outputPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();

        // Customize indentation for objects and arrays
        prettyPrinter.indentObjectsWith(new DefaultPrettyPrinter.FixedSpaceIndenter() {
            @Override
            public void writeIndentation(JsonGenerator g, int level) throws IOException {
                g.writeRaw("\n" + "    ".repeat(level)); // Add 4 spaces per level
            }
        });
        prettyPrinter.indentArraysWith(new DefaultPrettyPrinter.FixedSpaceIndenter() {
            @Override
            public void writeIndentation(JsonGenerator g, int level) throws IOException {
                g.writeRaw("\n" + "    ".repeat(level)); // Add 4 spaces per level
            }
        });

        mapper.writer(prettyPrinter).writeValue(new File(outputPath), userStories);
    }

    /**
     * Parses a single user story from a line of text.
     *
     * @param line the line of text
     * @return the parsed user story
     */
    private UserStory parseUserStory(String line) {
        if (!line.trim().endsWith(".")) {
            line += ".";
        }

        String actionStart = null;
        if (line.contains("I want to")) {
            actionStart = "I want to";
        } else if (line.contains("I need to")) {
            actionStart = "I need to";
        } else {
            System.err.println("Warning: No action phrase found. Line: \"" + line + "\"");
            return null;
        }

        UserStory userStory = new UserStory();
        userStory.setPid("#G" + String.format("%02d", pidCounter++) + "#");
        userStory.setText(line);
        userStory.setPersona(List.of(extractPart(line, "As a", actionStart)));

        UserStory.Action action = new UserStory.Action();
        action.setGoal(List.of(extractPart(line, actionStart, "so that")));
        action.setBenefit(List.of("obtain")); // Replace with logic to extract from text if available
        userStory.setAction(action);

        UserStory.Entity entity = new UserStory.Entity();
        entity.setGoalEntity(List.of("Information")); // Replace with logic to extract from text if available
        entity.setBenefitEntity(List.of("properties", "County services")); // Replace with logic to extract from text if available
        userStory.setEntity(entity);

        userStory.setBenefit(extractPart(line, "so that", "."));

        userStory.setTriggers(List.of(List.of(userStory.getPersona().get(0), action.getGoal().get(0))));
        userStory.setTargets(List.of(List.of(action.getGoal().get(0), "Information")));
        userStory.setContains(List.of(List.of("Information", "properties")));

        return userStory;
    }

    /**
     * Extracts a part of a string between two delimiters.
     *
     * @param line  the string to extract from
     * @param start the starting delimiter
     * @param end   the ending delimiter
     * @return the extracted part
     */
    private String extractPart(String line, String start, String end) {
        String lowerLine = line.toLowerCase().trim();
        String lowerStart = start.toLowerCase().trim();
        String lowerEnd = end.toLowerCase().trim();

        int startIndex = lowerLine.indexOf(lowerStart);
        if (startIndex == -1) {
            System.err.println("Warning: Start delimiter not found. Line: \"" + line + "\", Start: \"" + start + "\"");
            return "Unknown";
        }

        startIndex += lowerStart.length();
        int endIndex = lowerLine.indexOf(lowerEnd, startIndex);

        if (endIndex == -1) {
            System.err.println("Warning: End delimiter not found. Line: \"" + line + "\", End: \"" + end + "\"");
            return line.substring(startIndex).trim();
        }

        return line.substring(startIndex, endIndex).trim();
    }
}
