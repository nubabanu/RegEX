package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Analyzes user stories for completeness according to the INVEST criteria.
 * 
 * Completeness ensures that user story sets provide adequate coverage of functionality
 * for each domain entity. This analyzer identifies two types of incompleteness:
 * 
 * 1. Basic Completeness: Entities with actions like "edit", "view", or "delete" should also
 *    have corresponding "create" actions, as you cannot operate on non-existent entities.
 *    Example: If you can "edit a user profile", there should be a story to "create a user profile"
 * 
 * 2. CRUD Completeness: Entities with multiple CRUD operations should ideally support
 *    full Create, Read, Update, Delete functionality for comprehensive data management.
 *    Example: If you can "create" and "view" users, you might also need "update" and "delete"
 * 
 * The analyzer uses entity normalization to handle variations in naming and applies
 * synonym detection for actions across different CRUD categories. It employs strategic
 * filtering to avoid false positives for entities that legitimately only need subset operations.
 * 
 * Key features:
 * - Entity normalization: Handles possessives, articles, and naming variations
 * - Action categorization: Maps actions to CRUD categories using comprehensive synonym sets
 * - Dependency analysis: Identifies logical dependencies between operations
 * - Duplicate prevention: Ensures unique problem reporting per entity
 * 
 * Dependencies:
 * - Arrays and Collections: For efficient action categorization and entity tracking
 * - HashMap and HashSet: For fast lookup and deduplication
 * - ResourceBundle: For internationalized error messages
 * - Extends QualityCriterion: Provides base functionality for quality analysis
 */
public class CompletenessAnalyzer extends QualityCriterion {

    /**
     * Actions that logically require a corresponding "create" action to be meaningful.
     * If you can edit, view, or delete something, it must first be created.
     */
    private static final List<String> ACTIONS_REQUIRING_CREATE = Arrays.asList(
        "edit", "view", "delete", "manage", "update", "modify", "remove"
    );
    
    /**
     * Actions that represent entity creation in various forms.
     * Used to verify that creation capabilities exist for entities that require them.
     */
    private static final List<String> CREATE_ACTIONS = Arrays.asList(
        "create", "add", "register", "generate", "make", "new"
    );
    
    /**
     * Comprehensive mapping of actions to CRUD categories for completeness analysis.
     * These sets enable detection of CRUD coverage gaps across different action vocabularies.
     */
    private static final Set<String> CREATE_SET = new HashSet<>(Arrays.asList(
        "create", "add", "register", "generate", "make", "new"
    ));
    private static final Set<String> READ_SET = new HashSet<>(Arrays.asList(
        "view", "read", "see", "retrieve", "get", "show", "display", "list", "browse"
    ));
    private static final Set<String> UPDATE_SET = new HashSet<>(Arrays.asList(
        "edit", "update", "modify", "change"
    ));
    private static final Set<String> DELETE_SET = new HashSet<>(Arrays.asList(
        "delete", "remove", "erase"
    ));

    /**
     * Constructs a CompletenessAnalyzer with internationalized messages.
     * 
     * @param messages ResourceBundle containing localized error messages and descriptions
     */
    public CompletenessAnalyzer(ResourceBundle messages) {
        super("completeness", messages);
    }

    /**
     * Analyzes a collection of user stories for completeness violations.
     * 
     * Performs comprehensive analysis through:
     * 1. Entity-action mapping: Groups actions by normalized entity names
     * 2. CRUD categorization: Maps actions to Create, Read, Update, Delete categories
     * 3. Basic completeness checking: Verifies creation capabilities for dependent operations
     * 4. CRUD completeness checking: Identifies missing operations in multi-operation entities
     * 
     * Uses entity normalization to handle naming variations and deduplication to prevent
     * multiple problems for the same entity deficiency.
     * 
     * @param userStories List of UserStory objects to analyze
     * @return List of QualityProblem objects identifying completeness violations
     */
    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();
        if (userStories == null || userStories.isEmpty()) {
            return problems;
        }

        // Data structures for tracking entity operations and relationships
        Map<String, Set<String>> entityActions = new HashMap<>();
        Map<String, List<UserStory>> entityStoryMap = new HashMap<>();
        Map<String, Set<String>> crudMappingPerEntity = new HashMap<>();

        // Process all stories to build entity-action mappings
        for (UserStory story : userStories) {
            if (story.getEntityGoal() != null && !story.getEntityGoal().isEmpty() &&
                story.getActionGoal() != null && !story.getActionGoal().isEmpty()) {
                
                // Normalize entity names to handle variations like "a user" vs "user"
                String primaryEntity = normalizeEntity(story.getEntityGoal().get(0));
                String primaryAction = story.getActionGoal().get(0).toLowerCase();

                // Build mappings for completeness analysis
                entityActions.computeIfAbsent(primaryEntity, _ -> new HashSet<>()).add(primaryAction);
                entityStoryMap.computeIfAbsent(primaryEntity, _ -> new ArrayList<>()).add(story);
                
                // Categorize actions into CRUD operations
                updateCrudMapping(crudMappingPerEntity, primaryEntity, primaryAction);
            }
        }
        
        // Perform completeness analysis
        problems.addAll(checkBasicCompleteness(entityActions, entityStoryMap));
        problems.addAll(checkCrudCompleteness(crudMappingPerEntity, entityStoryMap));

        return problems;
    }
    
    /**
     * Categorizes an action into appropriate CRUD categories for an entity.
     * 
     * Updates the CRUD mapping by checking action membership in predefined sets
     * and marking the corresponding CRUD categories (C, R, U, D) for the entity.
     * 
     * @param crudMap Map tracking CRUD categories per entity
     * @param entity Normalized entity name
     * @param action Action to categorize
     */
    private void updateCrudMapping(Map<String, Set<String>> crudMap, String entity, String action) {
        Set<String> crudTypes = crudMap.computeIfAbsent(entity, _ -> new HashSet<>());
        
        if (CREATE_SET.contains(action)) {
            crudTypes.add("C");
        }
        if (READ_SET.contains(action)) {
            crudTypes.add("R");
        }
        if (UPDATE_SET.contains(action)) {
            crudTypes.add("U");
        }
        if (DELETE_SET.contains(action)) {
            crudTypes.add("D");
        }
    }
    
    /**
     * Checks for basic completeness violations: missing creation capabilities.
     * 
     * Identifies entities that have dependent operations (edit, view, delete) but lack
     * the fundamental creation operations that would make those operations meaningful.
     * This follows the logical principle that you cannot operate on non-existent entities.
     * 
     * @param entityActions Map of entities to their available actions
     * @param entityStoryMap Map of entities to their associated user stories
     * @return List of QualityProblem objects for missing creation capabilities
     */
    private List<QualityProblem> checkBasicCompleteness(
            Map<String, Set<String>> entityActions, 
            Map<String, List<UserStory>> entityStoryMap) {
        
        List<QualityProblem> problems = new ArrayList<>();
        
        for (Map.Entry<String, Set<String>> entry : entityActions.entrySet()) {
            String entity = entry.getKey();
            Set<String> actionsForEntity = entry.getValue();

            // Check if entity has any creation actions
            boolean hasCreateAction = false;
            for (String action : actionsForEntity) {
                if (CREATE_ACTIONS.contains(action)) {
                    hasCreateAction = true;
                    break;
                }
            }
            
            // Collect stories that require creation but don't have it
            List<UserStory> affectedStoriesForEntity = new ArrayList<>();

            if (!hasCreateAction) {
                for (String action : actionsForEntity) {
                    if (ACTIONS_REQUIRING_CREATE.contains(action)) {
                        // Find all stories with dependent actions for this entity
                        if (entityStoryMap.containsKey(entity)) {
                             entityStoryMap.get(entity).stream()
                                .filter(s -> s.getActionGoal() != null && !s.getActionGoal().isEmpty() &&
                                             action.equals(s.getActionGoal().get(0).toLowerCase()))
                                .forEach(affectedStoriesForEntity::add);
                        }
                    }
                }
            }
            
            // Report problem if dependent actions exist without creation capability
            if (!affectedStoriesForEntity.isEmpty()) {
                 // Prevent duplicate problems for the same entity
                boolean problemExists = problems.stream()
                    .anyMatch(p -> p.getProblemDescription().contains("Missing 'create' action for entity: '" + entity + "'"));

                if (!problemExists) {
                    problems.add(new QualityProblem(
                            criterionName,
                            messages.getString("quality.problem.incomplete") + ": Missing 'create' action for entity: '" + entity + "'", 
                            new ArrayList<>(new HashSet<>(affectedStoriesForEntity)), // Deduplicate stories
                            "INCOMPLETE_MISSING_CREATE" 
                    ));
                }
            }
        }
        
        return problems;
    }
    
    /**
     * Checks for CRUD completeness violations: incomplete operation coverage.
     * 
     * Identifies entities that have multiple CRUD operations but lack comprehensive
     * coverage, particularly focusing on entities that can be created and read but
     * cannot be modified or destroyed. This suggests incomplete lifecycle management.
     * 
     * Filters out single-operation entities to avoid false positives for legitimately
     * simple or read-only entities.
     * 
     * @param crudMappingPerEntity Map of entities to their CRUD category coverage
     * @param entityStoryMap Map of entities to their associated user stories
     * @return List of QualityProblem objects for incomplete CRUD coverage
     */
    private List<QualityProblem> checkCrudCompleteness(
            Map<String, Set<String>> crudMappingPerEntity,
            Map<String, List<UserStory>> entityStoryMap) {
        
        List<QualityProblem> problems = new ArrayList<>();
        
        for (Map.Entry<String, Set<String>> entry : crudMappingPerEntity.entrySet()) {
            String entity = entry.getKey();
            Set<String> crudTypes = entry.getValue();
            
            // Skip entities with minimal operations - they may legitimately be simple
            if (crudTypes.size() <= 1) {
                continue;
            }
            
            // Focus on entities with creation and reading but missing update/delete
            // This indicates a managed entity that lacks complete lifecycle support
            if (crudTypes.contains("C") && crudTypes.contains("R") && 
                (!crudTypes.contains("U") || !crudTypes.contains("D"))) {
                
                List<UserStory> affectedStories = entityStoryMap.getOrDefault(entity, new ArrayList<>());
                
                if (!affectedStories.isEmpty()) {
                    // Build description of missing operations
                    String missingOperations = 
                        (crudTypes.contains("U") ? "" : "'update' ") + 
                        (crudTypes.contains("D") ? "" : ((crudTypes.contains("U") ? "" : "and ") + "'delete' "));
                    
                    problems.add(new QualityProblem(
                        criterionName,
                        messages.getString("quality.problem.incomplete") + ": Entity '" + entity + "' may be missing " + missingOperations.trim() + " operations for CRUD completeness",
                        new ArrayList<>(new HashSet<>(affectedStories)), // Deduplicate stories
                        "INCOMPLETE_CRUD"
                    ));
                }
            }
        }
        
        return problems;
    }

    /**
     * Normalizes entity names to handle common linguistic variations.
     * 
     * Performs text normalization to ensure consistent entity identification:
     * - Removes articles: "a", "an", "the"
     * - Removes possessives: "my", "person's"
     * - Handles content references: "content of X" -> "X"
     * - Removes qualifiers: "new", "rights"
     * 
     * This normalization helps identify the same logical entity across different
     * phrasings in user story text, improving completeness analysis accuracy.
     * 
     * @param entityName Raw entity name from user story
     * @return Normalized entity name for consistent comparison
     */
    private String normalizeEntity(String entityName) {
        if (entityName == null) return "";
        String normalized = entityName.toLowerCase();
        
        // Remove common linguistic elements that don't affect entity identity
        normalized = normalized.replace("my ", "");
        normalized = normalized.replace("a ", "");
        normalized = normalized.replace("an ", "");
        normalized = normalized.replace("the ", "");
        normalized = normalized.replace("content of ", ""); // "content of profile" -> "profile"
        normalized = normalized.replace("person's ", "");   // "person's data" -> "data"
        normalized = normalized.replace(" new", "");        // "new account" -> "account"
        normalized = normalized.replace(" rights", "");     // "access rights" -> "access"
        
        return normalized.trim();
    }
}
