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
 * Analyzes user stories for completeness
 * Checks if essential user stories are missing from common workflows
 */
public class CompletenessAnalyzer extends QualityCriterion {

    private static final Map<String, List<String>> COMMON_WORKFLOWS = new HashMap<>();
    
    static {
        COMMON_WORKFLOWS.put("CRUD", Arrays.asList("create", "view", "edit", "delete"));
        COMMON_WORKFLOWS.put("Authentication", Arrays.asList("login", "logout", "register"));
        COMMON_WORKFLOWS.put("Search", Arrays.asList("search", "filter", "sort"));
    }

    public CompletenessAnalyzer(ResourceBundle messages) {
        super("completeness", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        // Analyze for each workflow pattern
        for (Map.Entry<String, List<String>> workflow : COMMON_WORKFLOWS.entrySet()) {
            String workflowName = workflow.getKey();
            List<String> requiredActions = workflow.getValue();
            
            Set<String> presentActions = extractActionsFromStories(userStories);
            
            for (String requiredAction : requiredActions) {
                if (hasPartialWorkflow(presentActions, requiredActions) && 
                    !presentActions.contains(requiredAction)) {
                    
                    problems.add(new QualityProblem(
                        criterionName,
                        messages.getString("quality.problem.incomplete") + ": Missing '" + 
                        requiredAction + "' action in " + workflowName + " workflow",
                        new ArrayList<>(), // No specific stories affected
                        "INCOMPLETE"
                    ));
                }
            }
        }

        return problems;
    }

    private Set<String> extractActionsFromStories(List<UserStory> userStories) {
        Set<String> actions = new HashSet<>();
        
        for (UserStory story : userStories) {
            if (story.getActionGoal() != null && !story.getActionGoal().isEmpty()) {
                actions.add(story.getActionGoal().get(0).toLowerCase());
            }
        }
        
        return actions;
    }
    
    private boolean hasPartialWorkflow(Set<String> presentActions, List<String> workflowActions) {
        int matchCount = 0;
        for (String action : workflowActions) {
            if (presentActions.contains(action)) {
                matchCount++;
            }
        }
        // If at least half of the workflow actions are present, consider it a partial workflow
        return matchCount >= Math.ceil(workflowActions.size() / 2.0);
    }
}
