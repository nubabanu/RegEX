package de.uni_marburg.sp25;

// Jackson library for JSON processing and graph creation
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

// Standard Java imports
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Manages annotation graph creation and manipulation from annotated JSON files.
 * 
 * This manager coordinates the process of:
 * - Importing annotated JSON files containing user story relationships
 * - Creating AnnotationGraph objects representing story dependencies
 * - Validating JSON schema and relationship data integrity
 * - Extracting relationship information (triggers, targets, contains) from annotations
 * - Converting annotation data back into UserStory objects with relationship metadata
 * 
 * Annotation Graph Structure:
 * - Each user story can have multiple relationship types
 * - Triggers: Stories that must be completed before this story can start
 * - Targets: Stories that are activated by completing this story  
 * - Contains: Sub-stories that are part of this story's implementation
 * 
 * JSON Schema Expectations:
 * - Array of user story objects at root level
 * - Each story contains PID, Text, and optional relationship arrays
 * - Relationship arrays contain lists of PIDs referencing other stories
 * 
 * Integration:
 * - Works with UserStoryManager for complete story lifecycle management
 * - Provides relationship data for GraphStream visualization
 * - Supports quality analysis by understanding story dependencies
 */
public class AnnotationGraphManager {
    
    private ResourceBundle messages;
    private List<String> validationWarnings = new ArrayList<>();
    
    /**
     * Constructs the annotation graph manager with localized messages.
     * @param messages ResourceBundle for internationalized warning and error messages
     */
    public AnnotationGraphManager(ResourceBundle messages) {
        this.messages = messages;
    }
    
    /**
     * Imports annotated JSON file and creates annotation graphs for each user story.
     * 
     * Processes the JSON file to extract user stories with their relationship annotations,
     * validates the schema, and creates AnnotationGraph objects containing the relationship
     * information that can be used for visualization and dependency analysis.
     * 
     * @param jsonFile Path to the annotated JSON file
     * @return Map of PIDs to their corresponding AnnotationGraph objects
     * @throws IOException If file reading or JSON parsing fails
     */
    public Map<String, AnnotationGraph> importAnnotatedJson(Path jsonFile) throws IOException {
        validationWarnings.clear();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode stories = mapper.readTree(jsonFile.toFile());
        Map<String, AnnotationGraph> graphs = new HashMap<>();
        
        if (!stories.isArray()) {
            validationWarnings.add("JSON file must contain an array of user stories");
            return graphs;
        }
        
        for (JsonNode story : stories) {
            if (!validateSchema(story)) {
                continue;
            }
            
            AnnotationGraph graph = new AnnotationGraph();
            String pid = story.get("PID").asText();
            
            // Create nodes for all elements
            
            // Persona nodes
            if (story.has("Persona") && story.get("Persona").isArray()) {
                ArrayNode personas = (ArrayNode) story.get("Persona");
                for (JsonNode persona : personas) {
                    String personaLabel = persona.asText();
                    graph.addNode(new Node(personaLabel, Node.Type.ROLE));
                }
            }
            
            // Action.Goal nodes
            if (story.has("Action.Goal") && story.get("Action.Goal").isArray()) {
                ArrayNode actionGoals = (ArrayNode) story.get("Action.Goal");
                for (JsonNode action : actionGoals) {
                    String actionLabel = action.asText();
                    graph.addNode(new Node(actionLabel, Node.Type.GOAL_ACTION));
                }
            }
            
            // Entity.Goal nodes
            if (story.has("Entity.Goal") && story.get("Entity.Goal").isArray()) {
                ArrayNode entityGoals = (ArrayNode) story.get("Entity.Goal");
                for (JsonNode entity : entityGoals) {
                    String entityLabel = entity.asText();
                    graph.addNode(new Node(entityLabel, Node.Type.GOAL_ENTITY));
                }
            }
            
            // Action.Benefit nodes
            if (story.has("Action.Benefit") && story.get("Action.Benefit").isArray()) {
                ArrayNode actionBenefits = (ArrayNode) story.get("Action.Benefit");
                for (JsonNode action : actionBenefits) {
                    String actionLabel = action.asText();
                    graph.addNode(new Node(actionLabel, Node.Type.BENEFIT_ACTION));
                }
            }
            
            // Entity.Benefit nodes
            if (story.has("Entity.Benefit") && story.get("Entity.Benefit").isArray()) {
                ArrayNode entityBenefits = (ArrayNode) story.get("Entity.Benefit");
                for (JsonNode entity : entityBenefits) {
                    String entityLabel = entity.asText();
                    graph.addNode(new Node(entityLabel, Node.Type.BENEFIT_ENTITY));
                }
            }
            
            // Create edges for all relationships
            
            // Triggers edges
            if (story.has("Triggers") && story.get("Triggers").isArray()) {
                ArrayNode triggers = (ArrayNode) story.get("Triggers");
                for (JsonNode trigger : triggers) {
                    if (trigger.isArray() && trigger.size() == 2) {
                        String sourceLabel = trigger.get(0).asText();
                        String targetLabel = trigger.get(1).asText();
                        
                        Node source = graph.findNode(sourceLabel, Node.Type.ROLE);
                        Node target = graph.findNode(targetLabel, Node.Type.GOAL_ACTION);
                        
                        if (source != null && target != null) {
                            graph.addEdge(new Edge(source, target, Edge.Type.TRIGGER));
                        }
                    }
                }
            }
            
            // Targets edges
            if (story.has("Targets") && story.get("Targets").isArray()) {
                ArrayNode targets = (ArrayNode) story.get("Targets");
                for (JsonNode target : targets) {
                    if (target.isArray() && target.size() == 2) {
                        String sourceLabel = target.get(0).asText();
                        String targetLabel = target.get(1).asText();
                        
                        // First check if it's a goal action -> goal entity
                        Node source = graph.findNode(sourceLabel, Node.Type.GOAL_ACTION);
                        Node target1 = graph.findNode(targetLabel, Node.Type.GOAL_ENTITY);
                        
                        if (source != null && target1 != null) {
                            graph.addEdge(new Edge(source, target1, Edge.Type.TARGET));
                        } else {
                            // Check if it's a benefit action -> benefit entity
                            source = graph.findNode(sourceLabel, Node.Type.BENEFIT_ACTION);
                            target1 = graph.findNode(targetLabel, Node.Type.BENEFIT_ENTITY);
                            
                            if (source != null && target1 != null) {
                                graph.addEdge(new Edge(source, target1, Edge.Type.TARGET));
                            }
                        }
                    }
                }
            }
            
            // Contains edges
            if (story.has("Contains") && story.get("Contains").isArray()) {
                ArrayNode contains = (ArrayNode) story.get("Contains");
                for (JsonNode containsRelation : contains) {
                    if (containsRelation.isArray() && containsRelation.size() == 2) {
                        String sourceLabel = containsRelation.get(0).asText();
                        String targetLabel = containsRelation.get(1).asText();
                        
                        // Find source and target nodes - they could be goal entities, benefit entities, or a mix
                        Node source = graph.findNode(sourceLabel, Node.Type.GOAL_ENTITY);
                        if (source == null) {
                            source = graph.findNode(sourceLabel, Node.Type.BENEFIT_ENTITY);
                        }
                        
                        Node target = graph.findNode(targetLabel, Node.Type.GOAL_ENTITY);
                        if (target == null) {
                            target = graph.findNode(targetLabel, Node.Type.BENEFIT_ENTITY);
                        }
                        
                        if (source != null && target != null) {
                            graph.addEdge(new Edge(source, target, Edge.Type.CONTAINS));
                        }
                    }
                }
            }
            
            graphs.put(pid, graph);
        }
        
        return graphs;
    }
    
    /**
     * Validate the schema of a user story JSON object
     * @param story The JSON node representing a user story
     * @return True if valid, false otherwise
     */
    private boolean validateSchema(JsonNode story) {
        List<String> errors = new ArrayList<>();
        
        // Check required fields
        String[] requiredFields = {"PID", "Text", "Persona", "Action.Goal", "Entity.Goal", "Benefit"};
        for (String field : requiredFields) {
            if (!story.has(field)) {
                errors.add("Missing required field: " + field);
            }
        }
        
        // Check field types
        validateArrayField(story, "Persona", errors);
        validateArrayField(story, "Action.Goal", errors);
        validateArrayField(story, "Action.Benefit", errors);
        validateArrayField(story, "Entity.Goal", errors);
        validateArrayField(story, "Entity.Benefit", errors);
        validateArrayField(story, "Triggers", errors);
        validateArrayField(story, "Targets", errors);
        validateArrayField(story, "Contains", errors);
        
        // Check string fields
        validateStringField(story, "PID", errors);
        validateStringField(story, "Text", errors);
        validateStringField(story, "Benefit", errors);
        
        // Check if any errors were found
        if (!errors.isEmpty()) {
            String pid = story.has("PID") ? story.get("PID").asText() : "Unknown";
            for (String error : errors) {
                validationWarnings.add("Story " + pid + ": " + error);
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate that a field is an array
     * @param story The JSON node
     * @param fieldName The field to validate
     * @param errors The list to add errors to
     */
    private void validateArrayField(JsonNode story, String fieldName, List<String> errors) {
        if (story.has(fieldName) && !story.get(fieldName).isArray()) {
            errors.add("Field " + fieldName + " should be an array");
        }
    }
    
    /**
     * Validate that a field is a string
     * @param story The JSON node
     * @param fieldName The field to validate
     * @param errors The list to add errors to
     */
    private void validateStringField(JsonNode story, String fieldName, List<String> errors) {
        if (story.has(fieldName) && !story.get(fieldName).isTextual()) {
            errors.add("Field " + fieldName + " should be a string");
        }
    }
    
    /**
     * Get the list of validation warnings
     * @return The list of warnings
     */
    public List<String> getValidationWarnings() {
        return new ArrayList<>(validationWarnings);
    }
    
    /**
     * Clear the validation warnings
     */
    public void clearValidationWarnings() {
        this.validationWarnings.clear();
    }
    
    /**
     * Convert annotation graphs back to user stories
     * @param graphs The map of PIDs to annotation graphs
     * @return A list of user stories
     */
    public List<UserStory> convertGraphsToUserStories(Map<String, AnnotationGraph> graphs) {
        List<UserStory> userStories = new ArrayList<>();
        
        for (Map.Entry<String, AnnotationGraph> entry : graphs.entrySet()) {
            String pid = entry.getKey();
            AnnotationGraph graph = entry.getValue();
            
            UserStory story = new UserStory();
            story.setPid(pid);
            
            // Extract data from nodes
            List<String> personas = new ArrayList<>();
            List<String> actionGoals = new ArrayList<>();
            List<String> actionBenefits = new ArrayList<>();
            List<String> entityGoals = new ArrayList<>();
            List<String> entityBenefits = new ArrayList<>();
            
            for (Node node : graph.getNodes()) {
                switch (node.getType()) {
                    case ROLE:
                        personas.add(node.getLabel());
                        break;
                    case GOAL_ACTION:
                        actionGoals.add(node.getLabel());
                        break;
                    case GOAL_ENTITY:
                        entityGoals.add(node.getLabel());
                        break;
                    case BENEFIT_ACTION:
                        actionBenefits.add(node.getLabel());
                        break;
                    case BENEFIT_ENTITY:
                        entityBenefits.add(node.getLabel());
                        break;
                }
            }
            
            // Extract relationships from edges
            List<List<String>> triggers = new ArrayList<>();
            List<List<String>> targets = new ArrayList<>();
            List<List<String>> contains = new ArrayList<>();
            
            for (Edge edge : graph.getEdges()) {
                List<String> relationship = new ArrayList<>();
                relationship.add(edge.getSource().getLabel());
                relationship.add(edge.getTarget().getLabel());
                
                switch (edge.getType()) {
                    case TRIGGER:
                        triggers.add(relationship);
                        break;
                    case TARGET:
                        targets.add(relationship);
                        break;
                    case CONTAINS:
                        contains.add(relationship);
                        break;
                }
            }
            
            // Set the extracted data in the UserStory
            story.setPersona(personas);
            story.setActionGoal(actionGoals);
            story.setActionBenefit(actionBenefits);
            story.setEntityGoal(entityGoals);
            story.setEntityBenefit(entityBenefits);
            story.setTriggers(triggers);
            story.setTargets(targets);
            story.setContains(contains);
            
            // Text and Benefit fields need to be set separately as they're not directly in the graph
            // For now, we'll use simple placeholder text that should be replaced with actual values from the original JSON
            story.setText("[Reconstructed from Graph]");
            story.setBenefit("[Reconstructed from Graph]");
            
            userStories.add(story);
        }
        
        return userStories;
    }
}
