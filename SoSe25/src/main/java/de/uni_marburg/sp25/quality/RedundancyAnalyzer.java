package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for redundancy
 * User stories are redundant if they describe the same functionality
 */
public class RedundancyAnalyzer extends QualityCriterion {

    public RedundancyAnalyzer(ResourceBundle messages) {
        super("redundancy", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (int i = 0; i < userStories.size(); i++) {
            for (int j = i + 1; j < userStories.size(); j++) {
                UserStory story1 = userStories.get(i);
                UserStory story2 = userStories.get(j);
                
                if (areRedundant(story1, story2)) {
                    problems.add(new QualityProblem(
                        criterionName,
                        messages.getString("quality.problem.redundant"),
                        List.of(story1, story2),
                        "REDUNDANT"
                    ));
                }
            }
        }

        return problems;
    }

    private boolean areRedundant(UserStory story1, UserStory story2) {
        // Check if personas are the same
        if (!isSamePersona(story1, story2)) {
            return false;
        }
        
        // Check if actions and entities are similar
        return isSimilarAction(story1, story2) && isSimilarEntity(story1, story2);
    }
    
    private boolean isSamePersona(UserStory story1, UserStory story2) {
        if (story1.getPersona() == null || story2.getPersona() == null) {
            return false;
        }
        if (story1.getPersona().isEmpty() || story2.getPersona().isEmpty()) {
            return false;
        }
        
        return story1.getPersona().get(0).equalsIgnoreCase(story2.getPersona().get(0));
    }
    
    private boolean isSimilarAction(UserStory story1, UserStory story2) {
        if (story1.getActionGoal() == null || story2.getActionGoal() == null) {
            return false;
        }
        if (story1.getActionGoal().isEmpty() || story2.getActionGoal().isEmpty()) {
            return false;
        }
        
        String action1 = story1.getActionGoal().get(0).toLowerCase();
        String action2 = story2.getActionGoal().get(0).toLowerCase();
        
        return action1.equals(action2) || areSynonyms(action1, action2);
    }
    
    private boolean isSimilarEntity(UserStory story1, UserStory story2) {
        if (story1.getEntityGoal() == null || story2.getEntityGoal() == null) {
            return false;
        }
        if (story1.getEntityGoal().isEmpty() || story2.getEntityGoal().isEmpty()) {
            return false;
        }
        
        String entity1 = story1.getEntityGoal().get(0).toLowerCase();
        String entity2 = story2.getEntityGoal().get(0).toLowerCase();
        
        return entity1.equals(entity2) || entity1.contains(entity2) || entity2.contains(entity1);
    }
    
    private boolean areSynonyms(String action1, String action2) {
        // Simple synonym detection
        if ((action1.equals("view") || action1.equals("see") || action1.equals("display")) &&
            (action2.equals("view") || action2.equals("see") || action2.equals("display"))) {
            return true;
        }
        if ((action1.equals("create") || action1.equals("add") || action1.equals("generate")) &&
            (action2.equals("create") || action2.equals("add") || action2.equals("generate"))) {
            return true;
        }
        if ((action1.equals("edit") || action1.equals("modify") || action1.equals("update")) &&
            (action2.equals("edit") || action2.equals("modify") || action2.equals("update"))) {
            return true;
        }
        return false;
    }
}
