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
 * Analyzes user stories for completeness.
 * Checks if for every entity that has an "edit", "view", or "delete" action,
 * there is also a corresponding "create" action for that same entity.
 */
public class CompletenessAnalyzer extends QualityCriterion {

    private static final List<String> ACTIONS_REQUIRING_CREATE = Arrays.asList("edit", "view", "delete", "manage");
    private static final String CREATE_ACTION = "create";

    public CompletenessAnalyzer(ResourceBundle messages) {
        super("completeness", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();
        if (userStories == null || userStories.isEmpty()) {
            return problems;
        }

        Map<String, Set<String>> entityActions = new HashMap<>();
        Map<String, List<UserStory>> entityStoryMap = new HashMap<>();

        for (UserStory story : userStories) {
            if (story.getEntityGoal() != null && !story.getEntityGoal().isEmpty() &&
                story.getActionGoal() != null && !story.getActionGoal().isEmpty()) {
                
                // Normalize entity names to handle minor variations (e.g., "profile page" vs "my profile page")
                // This is a simple normalization; more sophisticated NLP could be used.
                String primaryEntity = normalizeEntity(story.getEntityGoal().get(0));
                String primaryAction = story.getActionGoal().get(0).toLowerCase();

                entityActions.computeIfAbsent(primaryEntity, _ -> new HashSet<>()).add(primaryAction);
                entityStoryMap.computeIfAbsent(primaryEntity, _ -> new ArrayList<>()).add(story);
            }
        }

        for (Map.Entry<String, Set<String>> entry : entityActions.entrySet()) {
            String entity = entry.getKey();
            Set<String> actionsForEntity = entry.getValue();

            boolean hasCreateAction = actionsForEntity.contains(CREATE_ACTION);
            List<UserStory> affectedStoriesForEntity = new ArrayList<>();

            for (String action : actionsForEntity) {
                if (ACTIONS_REQUIRING_CREATE.contains(action) && !hasCreateAction) {
                    // Collect all stories for this entity that are of an action type requiring create
                    // but lack a create action for this entity.
                    if (entityStoryMap.containsKey(entity)) {
                         entityStoryMap.get(entity).stream()
                            .filter(s -> s.getActionGoal() != null && !s.getActionGoal().isEmpty() &&
                                         action.equals(s.getActionGoal().get(0).toLowerCase()))
                            .forEach(affectedStoriesForEntity::add);
                    }
                }
            }
            
            if (!affectedStoriesForEntity.isEmpty()) {
                 // Check if a problem for this entity has already been added
                boolean problemExists = problems.stream()
                    .anyMatch(p -> p.getProblemDescription().contains("Missing 'create' action for entity: '" + entity + "'"));

                if (!problemExists) {
                    problems.add(new QualityProblem(
                            criterionName,
                            messages.getString("quality.problem.incomplete") + ": Missing 'create' action for entity: '" + entity + "'", // Updated message key
                            new ArrayList<>(new HashSet<>(affectedStoriesForEntity)), // Ensure unique stories
                            "INCOMPLETE_MISSING_CREATE" 
                    ));
                }
            }
        }
        return problems;
    }

    // Simple entity normalization: lowercase and remove common possessives/articles.
    // This can be expanded.
    private String normalizeEntity(String entityName) {
        if (entityName == null) return "";
        String normalized = entityName.toLowerCase();
        normalized = normalized.replace("my ", "");
        normalized = normalized.replace("a ", "");
        normalized = normalized.replace("an ", "");
        normalized = normalized.replace("the ", "");
        normalized = normalized.replace("content of ", ""); // Handles "content of X" -> "X"
        normalized = normalized.replace("person's ", "");   // Handles "person's X" -> "X"
        // The following was .replace(" of a person's", "") which was not effective after "content of " removal.
        // Now "person's " handles cases like "a person's profile page" (after "a " is removed) 
        // or "person's profile page" directly.
        normalized = normalized.replace(" new", ""); // Handles " new X" -> "X"
        normalized = normalized.replace(" rights", ""); // Handles "X rights" -> "X" (if " rights" is at the end)
        return normalized.trim();
    }
}
