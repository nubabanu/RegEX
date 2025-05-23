package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for atomicity
 * A user story is atomic if it describes exactly one action
 */
public class AtomicityAnalyzer extends QualityCriterion {

    private static final List<String> CONJUNCTION_WORDS = Arrays.asList(
        "and", "or", "then", "also", "additionally", "furthermore", "moreover"
    );

    public AtomicityAnalyzer(ResourceBundle messages) {
        super("atomicity", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (UserStory story : userStories) {
            if (!isAtomic(story)) {
                problems.add(new QualityProblem(
                    criterionName,
                    messages.getString("quality.problem.notAtomic"),
                    List.of(story),
                    "NOT_ATOMIC"
                ));
            }
        }

        return problems;
    }

    private boolean isAtomic(UserStory story) {
        String text = story.getText().toLowerCase();
        
        // Check for conjunction words that might indicate multiple actions
        for (String conjunction : CONJUNCTION_WORDS) {
            if (text.contains(" " + conjunction + " ")) {
                return false;
            }
        }

        // Check if there are multiple actions in the goal
        if (story.getActionGoal() != null && story.getActionGoal().size() > 1) {
            return false;
        }

        // Check for multiple verbs in the text (simplified approach)
        String goalPart = extractGoalPart(text);
        if (goalPart != null && hasMultipleActions(goalPart)) {
            return false;
        }

        return true;
    }

    private String extractGoalPart(String text) {
        int wantIndex = text.indexOf("i want");
        if (wantIndex == -1) return null;
        
        int soThatIndex = text.indexOf("so that");
        int endIndex = soThatIndex != -1 ? soThatIndex : text.length();
        
        return text.substring(wantIndex + 6, endIndex).trim();
    }

    private boolean hasMultipleActions(String goalText) {
        // Simple heuristic: count action verbs
        String[] commonVerbs = {"create", "edit", "delete", "view", "search", "update", "add", "remove", "modify", "generate", "send", "receive"};
        int verbCount = 0;
        
        for (String verb : commonVerbs) {
            if (goalText.contains(verb)) {
                verbCount++;
            }
        }
        
        return verbCount > 1;
    }
}
