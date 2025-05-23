package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for minimality
 * A user story is minimal if it contains only essential information (role, goal, benefit)
 */
public class MinimalityAnalyzer extends QualityCriterion {

    private static final List<String> EXCESSIVE_WORDS = Arrays.asList(
        "obviously", "clearly", "definitely", "certainly", "absolutely", "totally",
        "completely", "extremely", "very", "quite", "really", "actually", "basically",
        "essentially", "specifically", "particularly", "especially", "mainly", "primarily"
    );

    public MinimalityAnalyzer(ResourceBundle messages) {
        super("minimality", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (UserStory story : userStories) {
            if (!isMinimal(story)) {
                problems.add(new QualityProblem(
                    criterionName,
                    messages.getString("quality.problem.notMinimal"),
                    List.of(story),
                    "NOT_MINIMAL"
                ));
            }
        }

        return problems;
    }

    private boolean isMinimal(UserStory story) {
        if (story.getText() == null) {
            return true;
        }
        
        String text = story.getText().toLowerCase();
        
        // Check for excessive adjectives and adverbs
        for (String excessiveWord : EXCESSIVE_WORDS) {
            if (text.contains(" " + excessiveWord + " ")) {
                return false;
            }
        }
        
        // Check if the story is too long (simple heuristic)
        if (text.length() > 200) {
            return false;
        }
        
        // Check for implementation details
        if (text.contains("using") || text.contains("through") || text.contains("via") || 
            text.contains("by clicking") || text.contains("button") || text.contains("menu")) {
            return false;
        }
        
        return true;
    }
}
