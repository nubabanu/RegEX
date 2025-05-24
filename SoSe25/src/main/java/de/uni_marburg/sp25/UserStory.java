package de.uni_marburg.sp25;

// Jackson annotations for JSON serialization/deserialization
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model representing a user story with its components and relationships.
 * 
 * This class serves as the core data structure for user story management, supporting:
 * - JSON serialization/deserialization via Jackson annotations
 * - Structured representation of user story components (persona, action, benefit)
 * - Relationship tracking between user stories (triggers, targets, contains)
 * - Integration with quality analysis and graph visualization systems
 * 
 * User Story Structure:
 * - PID: Unique project identifier (format: #G01#, #G02#, etc.)
 * - Text: Original natural language text of the user story
 * - Persona: Who the user is (role/actor)
 * - Action.Goal & Entity.Goal: What the user wants to do
 * - Action.Benefit & Entity.Benefit: Why the user wants it (separated by type)
 * - Benefit: Consolidated benefit description
 * 
 * Relationship Types:
 * - Triggers: Stories that activate this story
 * - Targets: Stories that this story activates
 * - Contains: Stories that are part of this story (composition)
 * 
 * Jackson Configuration:
 * - @JsonIgnoreProperties: Allows flexible JSON parsing, ignoring unknown fields
 * - @JsonProperty: Maps Java field names to JSON property names for consistency
 */
@JsonIgnoreProperties(ignoreUnknown = true) 
public class UserStory {
    
    @JsonProperty("PID")
    private String pid;

    @JsonProperty("Text")
    private String text;

    @JsonProperty("Persona")
    private List<String> persona;

    @JsonProperty("Action.Goal")
    private List<String> actionGoal;

    @JsonProperty("Action.Benefit")
    private List<String> actionBenefit;

    @JsonProperty("Entity.Goal")
    private List<String> entityGoal;

    @JsonProperty("Entity.Benefit")
    private List<String> entityBenefit;

    @JsonProperty("Benefit")
    private String benefit;

    @JsonProperty("Triggers")
    private List<List<String>> triggers;

    @JsonProperty("Targets")
    private List<List<String>> targets;

    @JsonProperty("Contains")
    private List<List<String>> contains;

    /**
     * Default constructor for Jackson deserialization and general usage.
     * Initializes relationship collections to prevent null pointer exceptions.
     */
    public UserStory() {
        this.triggers = new ArrayList<>();
        this.targets = new ArrayList<>();
        this.contains = new ArrayList<>();
    }

    // Constructor used in tests
    public UserStory(String pid, String text, int i, int j, int k, String role) {
        this.pid = pid;
        this.text = text;
        this.persona = List.of(role); 
        // Basic initialization for actionGoal and benefit for testing purposes.
        // Tests for specific analyzers might need to override these.
        if (text != null && !text.isBlank()) {
            if (text.toLowerCase().contains("i want to")) {
                this.actionGoal = List.of(text.substring(text.toLowerCase().indexOf("i want to") + "i want to".length()).trim().split(",")[0].trim());
            } else {
                this.actionGoal = List.of("default goal"); // Default if pattern not found
            }
            if (text.toLowerCase().contains("so that i can")) { // More specific pattern
                this.benefit = text.substring(text.toLowerCase().indexOf("so that i can") + "so that i can".length()).trim();
            } else if (text.toLowerCase().contains("so that")) { // Original fallback
                this.benefit = text.substring(text.toLowerCase().indexOf("so that") + "so that".length()).trim();
            } else {
                this.benefit = "default benefit"; // Default if pattern not found
            }
            // Initialize actionBenefit and entityBenefit to avoid nulls, can be refined
            this.actionBenefit = List.of("default action benefit");
            this.entityBenefit = List.of("default entity benefit");
            this.entityGoal = List.of("default entity goal");

        } else {
            this.actionGoal = List.of();
            this.benefit = "";
            this.actionBenefit = List.of();
            this.entityBenefit = List.of();
            this.entityGoal = List.of();
        }
        
        // Initialize the annotation relationship fields
        this.triggers = new ArrayList<>();
        this.targets = new ArrayList<>();
        this.contains = new ArrayList<>();
    }

    // Getters and setters for the new fields
    public List<String> getActionGoal() { return actionGoal; }
    public void setActionGoal(List<String> actionGoal) { this.actionGoal = actionGoal; }
    public List<String> getActionBenefit() { return actionBenefit; }
    public void setActionBenefit(List<String> actionBenefit) { this.actionBenefit = actionBenefit; }
    public List<String> getEntityGoal() { return entityGoal; }
    public void setEntityGoal(List<String> entityGoal) { this.entityGoal = entityGoal; }
    public List<String> getEntityBenefit() { return entityBenefit; }
    public void setEntityBenefit(List<String> entityBenefit) { this.entityBenefit = entityBenefit; }


    // Flat structure specification: Action and Entity data stored directly as lists within UserStory
    // The fields are now directly part of the UserStory class.

    public String getPid() { return pid; }
    public void setPid(String pid) { this.pid = pid; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<String> getPersona() { return persona; }
    public void setPersona(List<String> persona) { this.persona = persona; }
    public String getBenefit() { return benefit; }
    public void setBenefit(String benefit) { this.benefit = benefit; }
    public List<List<String>> getTriggers() { return triggers; }
    public void setTriggers(List<List<String>> triggers) { this.triggers = triggers; }
    public List<List<String>> getTargets() { return targets; }
    public void setTargets(List<List<String>> targets) { this.targets = targets; }
    public List<List<String>> getContains() { return contains; }
    public void setContains(List<List<String>> contains) { this.contains = contains; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStory userStory = (UserStory) o;
        return java.util.Objects.equals(pid, userStory.pid) &&
               java.util.Objects.equals(text, userStory.text) &&
               java.util.Objects.equals(persona, userStory.persona) &&
               java.util.Objects.equals(actionGoal, userStory.actionGoal) &&
               java.util.Objects.equals(actionBenefit, userStory.actionBenefit) &&
               java.util.Objects.equals(entityGoal, userStory.entityGoal) &&
               java.util.Objects.equals(entityBenefit, userStory.entityBenefit) &&
               java.util.Objects.equals(benefit, userStory.benefit) &&
               java.util.Objects.equals(triggers, userStory.triggers) &&
               java.util.Objects.equals(targets, userStory.targets) &&
               java.util.Objects.equals(contains, userStory.contains);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(pid, text, persona, actionGoal, actionBenefit, entityGoal, entityBenefit, benefit, triggers, targets, contains);
    }

    /**
     * Provides JSON-like string representation for debugging and logging.
     * Note: For actual JSON serialization, use Jackson ObjectMapper instead.
     * @return String representation in JSON-like format
     */
    @Override
    public String toString() {
        return "{" +
                "\\\"PID\\\": \\\"" + pid + "\\\"," +
                "\\\"Text\\\": \\\"" + text + "\\\"," +
                "\\\"Persona\\\": " + (persona != null ? persona.toString() : "[]") + "," +
                "\\\"Action.Goal\\\": " + (actionGoal != null ? actionGoal.toString() : "[]") + "," +
                "\\\"Action.Benefit\\\": " + (actionBenefit != null ? actionBenefit.toString() : "[]") + "," +
                "\\\"Entity.Goal\\\": " + (entityGoal != null ? entityGoal.toString() : "[]") + "," +
                "\\\"Entity.Benefit\\\": " + (entityBenefit != null ? entityBenefit.toString() : "[]") + "," +
                "\\\"Benefit\\\": \\\"" + benefit + "\\\"," +
                "\\\"Triggers\\\": " + (triggers != null ? triggers.toString() : "[]") + "," +
                "\\\"Targets\\\": " + (targets != null ? targets.toString() : "[]") + "," +
                "\\\"Contains\\\": " + (contains != null ? contains.toString() : "[]") +
                "}";
    }
}