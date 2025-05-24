package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Analyzes user stories for conflicts according to the INVEST criteria.
 * 
 * User stories are considered conflicting if they contain contradictory requirements
 * that cannot be simultaneously satisfied. This analyzer identifies three types of conflicts:
 * 
 * 1. Action Conflicts: Stories with opposite actions on the same entity
 *    Example: "I want to create a user account" vs "I want to delete a user account"
 * 
 * 2. Permission Conflicts: Stories with contradictory access control requirements
 *    Example: "I want to edit any landmark" vs "I want to edit only my landmarks"
 * 
 * 3. Scope Conflicts: Stories with conflicting scope limitations on similar actions
 *    Example: "I want to view all documents" vs "I want to view only approved documents"
 * 
 * The analyzer uses several detection strategies:
 * - Static mapping of conflicting action pairs (create/delete, enable/disable, etc.)
 * - Permission term analysis using keyword detection
 * - Scope restriction analysis through linguistic pattern matching
 * - Entity similarity checking to ensure conflicts apply to the same domain objects
 * 
 * Dependencies:
 * - HashMap and HashSet: For efficient storage of conflicting action mappings
 * - ResourceBundle: For internationalized error messages
 * - Extends QualityCriterion: Provides base functionality for quality analysis
 */
public class ConflictFreeAnalyzer extends QualityCriterion {

    /**
     * Static mapping of conflicting action pairs for conflict detection.
     * Each key-value pair represents actions that are mutually exclusive.
     * Used bidirectionally - both (key->value) and (value->key) are checked.
     */
    private static final Map<String, String> CONFLICTING_ACTIONS = new HashMap<>();
    
    static {
        // Initialize conflicting action pairs for comprehensive conflict detection
        CONFLICTING_ACTIONS.put("create", "delete");
        CONFLICTING_ACTIONS.put("add", "remove");
        CONFLICTING_ACTIONS.put("enable", "disable");
        CONFLICTING_ACTIONS.put("show", "hide");
        CONFLICTING_ACTIONS.put("open", "close");
        CONFLICTING_ACTIONS.put("start", "stop");
        CONFLICTING_ACTIONS.put("allow", "prevent");
        CONFLICTING_ACTIONS.put("grant", "deny");
        CONFLICTING_ACTIONS.put("include", "exclude");
        CONFLICTING_ACTIONS.put("activate", "deactivate");
        CONFLICTING_ACTIONS.put("lock", "unlock");
        CONFLICTING_ACTIONS.put("block", "unblock");
        CONFLICTING_ACTIONS.put("expand", "collapse");
        CONFLICTING_ACTIONS.put("enable", "forbid");
        CONFLICTING_ACTIONS.put("permit", "restrict");
    }
    
    /**
     * Keywords that indicate permission or access control contexts.
     * Used to identify stories that deal with authorization and access rights,
     * which are common sources of conflicts in user requirements.
     */
    private static final Set<String> PERMISSION_TERMS = new HashSet<>(List.of(
        "permission", "access", "right", "role", "privilege", "security", "authorization"
    ));

    /**
     * Constructs a ConflictFreeAnalyzer with internationalized messages.
     * 
     * @param messages ResourceBundle containing localized error messages and descriptions
     */
    public ConflictFreeAnalyzer(ResourceBundle messages) {
        super("conflictFree", messages);
    }

    /**
     * Analyzes a collection of user stories for conflict violations.
     * 
     * Performs comprehensive pairwise comparison to detect conflicts through:
     * - Direct action conflicts (opposite actions on same entities)
     * - Permission conflicts (contradictory access control requirements)
     * - Scope conflicts (conflicting limitations on action scope)
     * 
     * Uses hierarchical checking with else-if logic to prevent duplicate reporting
     * and prioritize the most specific type of conflict found.
     * 
     * @param userStories List of UserStory objects to analyze
     * @return List of QualityProblem objects identifying conflicting story pairs
     */
    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (int i = 0; i < userStories.size(); i++) {
            for (int j = i + 1; j < userStories.size(); j++) {
                UserStory story1 = userStories.get(i);
                UserStory story2 = userStories.get(j);
                
                // Check for direct action conflicts (highest priority)
                if (hasOppositeActionConflict(story1, story2)) {
                    problems.add(createConflictProblem(story1, story2, "CONFLICTING_ACTIONS"));
                }
                // Check for permission/access conflicts
                else if (hasPermissionConflict(story1, story2)) {
                    problems.add(createConflictProblem(story1, story2, "CONFLICTING_PERMISSIONS"));
                }
                // Check for scope conflicts (general case)
                else if (hasScopeConflict(story1, story2)) {
                    problems.add(createConflictProblem(story1, story2, "CONFLICTING_SCOPE"));
                }
            }
        }

        return problems;
    }
    
    /**
     * Creates a conflict problem with contextually appropriate error messages.
     * 
     * Generates specific error messages based on the type of conflict detected
     * to provide clear feedback about the nature of the contradiction found.
     * 
     * @param story1 First conflicting user story
     * @param story2 Second conflicting user story
     * @param conflictType Type of conflict detected (CONFLICTING_ACTIONS, CONFLICTING_PERMISSIONS, CONFLICTING_SCOPE)
     * @return QualityProblem object with appropriate error message and metadata
     */
    private QualityProblem createConflictProblem(UserStory story1, UserStory story2, String conflictType) {
        String message;
        switch (conflictType) {
            case "CONFLICTING_ACTIONS":
                message = messages.getString("quality.problem.conflicting") + 
                          ": Stories contain opposite actions on the same entity.";
                break;
            case "CONFLICTING_PERMISSIONS":
                message = messages.getString("quality.problem.conflicting") + 
                          ": Stories contain conflicting permission requirements.";
                break;
            case "CONFLICTING_SCOPE":
                message = messages.getString("quality.problem.conflicting") + 
                          ": Stories have conflicting scope of action on the same entity.";
                break;
            default:
                message = messages.getString("quality.problem.conflicting");
        }
        
        return new QualityProblem(
            criterionName,
            message,
            List.of(story1, story2),
                        conflictType
        );
    }
    
    /**
     * Detects direct action conflicts between two user stories.
     * 
     * Identifies conflicts where stories have opposite actions on the same entity,
     * such as create vs delete, enable vs disable, etc. Uses the static CONFLICTING_ACTIONS
     * mapping to determine if actions are mutually exclusive.
     * 
     * @param story1 First user story to compare
     * @param story2 Second user story to compare
     * @return true if stories have conflicting actions on same entity, false otherwise
     */
    private boolean hasOppositeActionConflict(UserStory story1, UserStory story2) {
        // Verify both stories target the same or similar entities
        if (!isSameOrSimilarEntity(story1, story2)) {
            return false;
        }
        
        String action1 = getFirstAction(story1);
        String action2 = getFirstAction(story2);
        
        if (action1 == null || action2 == null) {
            return false;
        }
        
        // Check bidirectional conflict mapping
        if (CONFLICTING_ACTIONS.containsKey(action1) && 
            CONFLICTING_ACTIONS.get(action1).equals(action2)) {
            return true;
        }
        
        if (CONFLICTING_ACTIONS.containsKey(action2) && 
            CONFLICTING_ACTIONS.get(action2).equals(action1)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Detects permission conflicts between user stories.
     * 
     * Identifies conflicts in access control requirements, such as:
     * - "I want to edit any landmark" vs "I want to delete only my landmarks"
     * - Stories with different permission scopes for the same entity
     * 
     * Uses keyword detection for permission-related terms and restriction analysis
     * to identify contradictory access control patterns.
     * 
     * @param story1 First user story to compare
     * @param story2 Second user story to compare
     * @return true if stories have conflicting permission requirements, false otherwise
     */
    private boolean hasPermissionConflict(UserStory story1, UserStory story2) {
        // Verify both stories target the same or similar entities
        if (!isSameOrSimilarEntity(story1, story2)) {
            return false;
        }
        
        String text1 = story1.getText() != null ? story1.getText().toLowerCase() : "";
        String text2 = story2.getText() != null ? story2.getText().toLowerCase() : "";
        
        // Detect permission-related terms in both stories
        boolean story1HasPermissionTerms = false;
        boolean story2HasPermissionTerms = false;
        
        for (String term : PERMISSION_TERMS) {
            if (text1.contains(term)) {
                story1HasPermissionTerms = true;
            }
            if (text2.contains(term)) {
                story2HasPermissionTerms = true;
            }
        }
        
        // Analyze conflicts when both stories involve permissions
        if (story1HasPermissionTerms && story2HasPermissionTerms) {
            // Check for asymmetric restriction patterns
            boolean story1HasRestriction = containsRestriction(text1);
            boolean story2HasRestriction = containsRestriction(text2);
            
            // Conflict exists when one story is restrictive and the other is not
            if (story1HasRestriction != story2HasRestriction) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Detects scope conflicts between user stories.
     * 
     * Identifies conflicts where stories have the same action but different scope limitations:
     * - One allows unrestricted action, another imposes limitations
     * - Different levels of specificity for the same functional area
     * 
     * Example: "edit all content" vs "edit only my content"
     * 
     * @param story1 First user story to compare
     * @param story2 Second user story to compare
     * @return true if stories have conflicting action scopes, false otherwise
     */
    private boolean hasScopeConflict(UserStory story1, UserStory story2) {
        // Verify actions are the same or similar
        String action1 = getFirstAction(story1);
        String action2 = getFirstAction(story2);
        
        if (action1 == null || action2 == null || !action1.equals(action2)) {
            return false;
        }
        
        // Analyze entity relationships for scope differences
        List<String> entities1 = story1.getEntityGoal();
        List<String> entities2 = story2.getEntityGoal();
        
        if (entities1 == null || entities1.isEmpty() || entities2 == null || entities2.isEmpty()) {
            return false;
        }
        
        // Check for related but different entity scopes
        String entity1 = entities1.get(0).toLowerCase();
        String entity2 = entities2.get(0).toLowerCase();
        
        // Detect scope relationship (one entity contains another but they're not identical)
        if ((entity1.contains(entity2) || entity2.contains(entity1)) && !entity1.equals(entity2)) {
            String text1 = story1.getText() != null ? story1.getText().toLowerCase() : "";
            String text2 = story2.getText() != null ? story2.getText().toLowerCase() : "";
            
            boolean story1HasRestriction = containsRestriction(text1);
            boolean story2HasRestriction = containsRestriction(text2);
            
            // Conflict exists when restriction patterns differ
            if (story1HasRestriction != story2HasRestriction) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Detects linguistic indicators of scope restrictions in user story text.
     * 
     * Identifies words and phrases that indicate limitations or constraints:
     * - Restrictive quantifiers: "only", "specific", "certain", "selected"
     * - Possessive indicators: "my", "their"
     * - Conditional phrases: "that I", "particular"
     * 
     * @param text User story text to analyze (should be lowercase)
     * @return true if text contains restriction indicators, false otherwise
     */
    private boolean containsRestriction(String text) {
        return text.contains(" only ") || 
               text.contains(" my ") ||
               text.contains(" their ") ||
               text.contains(" specific ") ||
               text.contains(" certain ") ||
               text.contains(" selected ") ||
               text.contains(" limited ") ||
               text.contains(" particular ") ||
               text.contains(" that i ");
    }
    
    /**
     * Determines if two user stories refer to the same or similar entities.
     * 
     * Uses exact matching and containment checking to identify related entities.
     * Handles cases where entity names overlap or one is a subset of another.
     * 
     * @param story1 First user story to compare
     * @param story2 Second user story to compare
     * @return true if stories refer to same or similar entities, false otherwise
     */
    private boolean isSameOrSimilarEntity(UserStory story1, UserStory story2) {
        if (story1.getEntityGoal() == null || story2.getEntityGoal() == null) {
            return false;
        }
        if (story1.getEntityGoal().isEmpty() || story2.getEntityGoal().isEmpty()) {
            return false;
        }
        
        String entity1 = story1.getEntityGoal().get(0).toLowerCase();
        String entity2 = story2.getEntityGoal().get(0).toLowerCase();
        
        // Check for exact match or containment relationship
        return entity1.equals(entity2) || entity1.contains(entity2) || entity2.contains(entity1);
    }
    
    /**
     * Extracts the primary action from a user story for conflict analysis.
     * 
     * Assumes atomicity (single primary action) and returns the first action
     * from the story's action goal list in lowercase for consistent comparison.
     * 
     * @param story UserStory object to extract action from
     * @return First action in lowercase, or null if no actions available
     */
    private String getFirstAction(UserStory story) {
        if (story.getActionGoal() == null || story.getActionGoal().isEmpty()) {
            return null;
        }
        return story.getActionGoal().get(0).toLowerCase();
    }
}
