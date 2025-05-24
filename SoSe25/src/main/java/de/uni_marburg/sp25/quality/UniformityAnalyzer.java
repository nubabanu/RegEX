package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Analyzes user stories for uniformity according to the INVEST criteria.
 * 
 * A user story is considered uniform if it follows the standard template structure:
 * "As a [role], I want [goal], so that [benefit]" (full format)
 * OR "As a [role], I want [goal]." (simplified format)
 * 
 * This analyzer uses regular expressions to parse user story text and validate structural conformity.
 * It checks for proper template usage while allowing flexibility in the "goal" section to accommodate
 * legitimate uses of prepositions like "for" in phrases such as "search for" or "apply for".
 * 
 * Key features:
 * - Uses Java Pattern class with CASE_INSENSITIVE matching for robust text analysis
 * - Validates both full template format (with benefit) and simplified format (goal only)
 * - Distinguishes between legitimate preposition usage and non-uniform benefit inclusion
 * - Provides specific feedback for non-uniform stories to guide improvement
 * 
 * Dependencies:
 * - ResourceBundle: For internationalized error messages
 * - Pattern: For regular expression matching and text validation
 * - Extends QualityCriterion: Provides base functionality for quality analysis
 */
public class UniformityAnalyzer extends QualityCriterion {

    /**
     * Regular expression pattern for goal-only user stories.
     * Matches: "As a [role], I want [goal]."
     * Named capture group "goal" extracts the goal content for further analysis.
     */
    private static final Pattern P_GOAL_ONLY = Pattern.compile(
        "^As (?:a|an)\\s+[^,]+,\\s*I want\\s+(?<goal>.+?)\\.$",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Regular expression pattern for full template user stories.
     * Matches: "As a [role], I want [goal], so that [benefit]."
     * Named capture group "goal" extracts the goal content.
     */
    private static final Pattern P_GOAL_SO_THAT_BENEFIT = Pattern.compile(
        "^As (?:a|an)\\s+[^,]+,\\s*I want\\s+(?<goal>.+?),\\s*so that\\s+.+\\.$",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Constructs a UniformityAnalyzer with internationalized messages.
     * 
     * @param messages ResourceBundle containing localized error messages and descriptions
     */
    public UniformityAnalyzer(ResourceBundle messages) {
        super("uniformity", messages);
    }

    /**
     * Analyzes a collection of user stories for uniformity violations.
     * 
     * Checks each story against the standard template formats and identifies stories
     * that don't conform to the expected structure. Returns problems for stories that
     * lack proper template formatting or contain structural issues.
     * 
     * @param userStories List of UserStory objects to analyze
     * @return List of QualityProblem objects identifying non-uniform stories
     */
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

    /**
     * Determines if a user story follows the uniform template structure.
     * 
     * A story is considered uniform if it matches either:
     * 1. Full format: "As a [role], I want [goal], so that [benefit]."
     * 2. Simplified format: "As a [role], I want [goal]." (with legitimate preposition usage)
     * 
     * Special handling for prepositions:
     * - Allows legitimate uses like "search for", "apply for", "register for"
     * - Rejects benefit-like usage such as "for security reasons" in goal section
     * 
     * @param story UserStory object to evaluate for uniformity
     * @return true if story follows uniform template structure, false otherwise
     */
    private boolean isUniform(UserStory story) {
        if (story.getText() == null || story.getText().trim().isEmpty()) {
            return false; // Empty or null stories are not uniform
        }
        String text = story.getText().trim();

        // Check for full template format: "As a [role], I want [goal], so that [benefit]."
        if (P_GOAL_SO_THAT_BENEFIT.matcher(text).matches()) {
            return true; // Properly structured with explicit benefit
        }

        // Check for simplified format: "As a [role], I want [goal]."
        java.util.regex.Matcher goalOnlyMatcher = P_GOAL_ONLY.matcher(text);
        if (goalOnlyMatcher.matches()) {
            String goalContent = goalOnlyMatcher.group("goal").toLowerCase();

            if (goalContent.contains(" for ")) {
                // Determine if "for" usage is legitimate (action-related) or benefit-like
                boolean isLegitimateForUsage =
                    goalContent.contains("search for") || goalContent.contains("look for") ||
                    goalContent.contains("apply for") || goalContent.contains("prepare for") ||
                    goalContent.contains("register for") || goalContent.contains("pay for") ||
                    goalContent.contains("ask for") || goalContent.contains("account for") ||
                    goalContent.contains("care for") || goalContent.contains("plan for") ||
                    goalContent.contains("opt for") || goalContent.contains("arrange for");
                return isLegitimateForUsage;
            } else {
                // Clean goal-only story without problematic prepositions
                return true; 
            }
        }

        // Story doesn't match either uniform pattern (missing template elements, wrong structure, etc.)
        return false;
    }
}
