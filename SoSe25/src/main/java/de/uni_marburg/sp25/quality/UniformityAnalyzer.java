package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher; // Re-add import
import java.util.regex.Pattern;

/**
 * Analyzes user stories for uniformity
 * A user story is uniform if it follows the standard "As a..., I want..., so that..." format
 */
public class UniformityAnalyzer extends QualityCriterion {

    // Pattern 1: "As a R, I want G." - Captures G in a group named "goal"
    private static final Pattern P_GOAL_ONLY = Pattern.compile(
        "^As (?:a|an)\\s+[^,]+,\\s*I want\\s+(?<goal>.+?)\\.$",
        Pattern.CASE_INSENSITIVE
    );
    // Pattern 2: "As a R, I want G, so that B."
    private static final Pattern P_GOAL_SO_THAT_BENEFIT = Pattern.compile(
        "^As (?:a|an)\\s+[^,]+,\\s*I want\\s+(?<goal>.+?),\\s*so that\\s+.+\\.$",
        Pattern.CASE_INSENSITIVE
    );

    public UniformityAnalyzer(ResourceBundle messages) {
        super("uniformity", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (UserStory story : userStories) {
            if (!isUniform(story)) {
                problems.add(new QualityProblem(
                    criterionName,
                    messages.getString("quality.problem.notUniform"),
                    List.of(story),
                    "NOT_UNIFORM"
                ));
            }
        }
        return problems;
    }

    private boolean isUniform(UserStory story) {
        if (story.getText() == null || story.getText().trim().isEmpty()) {
            return false; // An empty story is not uniform.
        }
        String text = story.getText().trim();

        // Check 1: Does it match "As a R, I want G, so that B."?
        if (P_GOAL_SO_THAT_BENEFIT.matcher(text).matches()) {
            return true; // Uniform
        }

        // Check 2: Does it match "As a R, I want G."?
        java.util.regex.Matcher goalOnlyMatcher = P_GOAL_ONLY.matcher(text);
        if (goalOnlyMatcher.matches()) {
            String goalContent = goalOnlyMatcher.group("goal").toLowerCase();

            if (goalContent.contains(" for ")) {
                // If " for " is present, uniformity depends on whether it's a legitimate usage.
                // isLegitimateForUsage = true means it's like "search for", so uniform.
                // isLegitimateForUsage = false means it's like "for security reasons", so non-uniform.
                boolean isLegitimateForUsage =
                    goalContent.contains("search for") || goalContent.contains("look for") ||
                    goalContent.contains("apply for") || goalContent.contains("prepare for") ||
                    goalContent.contains("register for") || goalContent.contains("pay for") ||
                    goalContent.contains("ask for") || goalContent.contains("account for") ||
                    goalContent.contains("care for") || goalContent.contains("plan for") ||
                    goalContent.contains("opt for") || goalContent.contains("arrange for");
                return isLegitimateForUsage;
            } else {
                // No " for " in goal, so it's a clean goal-only story (e.g., "I want to logout.")
                return true; 
            }
        }

        // If neither of the specific uniform patterns matched, it's not uniform.
        // This covers cases like missing "As a", missing "I want", or other structural issues.
        return false;
    }
}
