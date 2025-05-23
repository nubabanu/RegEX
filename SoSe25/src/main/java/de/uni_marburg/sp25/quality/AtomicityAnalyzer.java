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
        "and", "or", "then", "also", "additionally", "furthermore", "moreover", "but" // Added "but"
    );

    public AtomicityAnalyzer(ResourceBundle messages) {
        super("atomicity", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (UserStory story : userStories) {
            if (!isAtomic(story)) {
                // Check if a problem for this specific criterion but for a different story (if stories are grouped by problem type)
                // For atomicity, each non-atomic story is its own problem instance.
                problems.add(new QualityProblem(
                    criterionName,
                    messages.getString("quality.problem.notAtomic"),
                    List.of(story), // Each problem instance is tied to the specific non-atomic story
                    "NOT_ATOMIC"
                ));
            }
        }

        return problems;
    }

    private boolean isAtomic(UserStory story) {
        // Check 1: Based on parsed actionGoal list from UserStory object
        // This assumes UserStory.java correctly parses multiple actions into this list.
        if (story.getActionGoal() != null && story.getActionGoal().size() > 1) {
            return false; 
        }

        String lowerCaseText = story.getText().toLowerCase();

        // Check 2: Textual analysis of the goal part for conjunctions
        String goalPart = extractGoalPart(lowerCaseText);
        if (goalPart != null) {
            for (String conjunction : CONJUNCTION_WORDS) {
                if (goalPart.contains(" " + conjunction + " ")) {
                    return false;
                }
            }
        }

        // Check 3: Textual analysis of the benefit part for conjunctions
        String benefitPart = extractBenefitPart(lowerCaseText);
        if (benefitPart != null) {
            for (String conjunction : CONJUNCTION_WORDS) {
                if (benefitPart.contains(" " + conjunction + " ")) {
                    return false;
                }
            }
        }

        return true;
    }

    private String extractGoalPart(String lowerCaseText) {
        int wantIndex = lowerCaseText.indexOf("i want ");
        int startIndex = -1;

        if (wantIndex != -1) {
            startIndex = wantIndex + "i want ".length();
        } else {
            wantIndex = lowerCaseText.indexOf("i want to ");
            if (wantIndex != -1) {
                startIndex = wantIndex + "i want to ".length();
            } else {
                return null; // No "I want" or "I want to" found
            }
        }
        
        int soThatIndex = lowerCaseText.indexOf(" so that ", startIndex);
        int endIndex = soThatIndex != -1 ? soThatIndex : lowerCaseText.length();
        
        return lowerCaseText.substring(startIndex, endIndex).trim();
    }

    private String extractBenefitPart(String lowerCaseText) {
        int soThatIndex = lowerCaseText.indexOf("so that ");
        if (soThatIndex == -1) {
            return null; // No "so that" found
        }
        // Ensure "so that " is not part of the goal if goal also contains "so that"
        // This is generally handled by extractGoalPart's endIndex.
        // Benefit starts after "so that ".
        return lowerCaseText.substring(soThatIndex + "so that ".length()).trim();
    }

    
}
