package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for conflicts
 * User stories are in conflict if they have contradictory requirements
 */
public class ConflictFreeAnalyzer extends QualityCriterion {

    private static final Map<String, String> CONFLICTING_ACTIONS = new HashMap<>();
    
    static {
        CONFLICTING_ACTIONS.put("create", "delete");
        CONFLICTING_ACTIONS.put("add", "remove");
        CONFLICTING_ACTIONS.put("enable", "disable");
        CONFLICTING_ACTIONS.put("show", "hide");
        CONFLICTING_ACTIONS.put("open", "close");
        CONFLICTING_ACTIONS.put("start", "stop");
        CONFLICTING_ACTIONS.put("allow", "prevent");
        CONFLICTING_ACTIONS.put("grant", "deny");
    }

    public ConflictFreeAnalyzer(ResourceBundle messages) {
        super("conflictFree", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (int i = 0; i < userStories.size(); i++) {
            for (int j = i + 1; j < userStories.size(); j++) {
                UserStory story1 = userStories.get(i);
                UserStory story2 = userStories.get(j);
                
                if (areInConflict(story1, story2)) {
                    problems.add(new QualityProblem(
                        criterionName,
                        messages.getString("quality.problem.conflicting"),
                        List.of(story1, story2),
                        "CONFLICTING"
                    ));
                }
            }
        }

        return problems;
    }

    private boolean areInConflict(UserStory story1, UserStory story2) {
        // Check if they involve the same entity but conflicting actions
        if (!isSameEntity(story1, story2)) {
            return false;
        }
        
        String action1 = getFirstAction(story1);
        String action2 = getFirstAction(story2);
        
        if (action1 == null || action2 == null) {
            return false;
        }
        
        // Check for direct conflicts
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
    
    private boolean isSameEntity(UserStory story1, UserStory story2) {
        if (story1.getEntityGoal() == null || story2.getEntityGoal() == null) {
            return false;
        }
        if (story1.getEntityGoal().isEmpty() || story2.getEntityGoal().isEmpty()) {
            return false;
        }
        
        String entity1 = story1.getEntityGoal().get(0).toLowerCase();
        String entity2 = story2.getEntityGoal().get(0).toLowerCase();
        
        return entity1.equals(entity2);
    }
    
    private String getFirstAction(UserStory story) {
        if (story.getActionGoal() == null || story.getActionGoal().isEmpty()) {
            return null;
        }
        return story.getActionGoal().get(0).toLowerCase();
    }
}
