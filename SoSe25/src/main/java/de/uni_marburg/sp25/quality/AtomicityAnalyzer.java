package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for atomicity - ensuring each story describes exactly one action.
 * 
 * The atomicity principle states that a user story should focus on a single, well-defined
 * functionality or feature. Stories that contain multiple actions or goals violate this
 * principle and should be split into separate, focused stories.
 * 
 * Detection Methods:
 * 1. Multiple actions in parsed actionGoal list (when UserStory parsing identifies multiple goals)
 * 2. Conjunction words that typically indicate multiple actions ("and", "or", "then", etc.)
 * 3. Text patterns suggesting compound functionality
 * 
 * Benefits of atomic stories:
 * - Easier estimation and planning
 * - Clearer acceptance criteria
 * - Better testability
 * - Reduced implementation complexity
 * - More accurate progress tracking
 */
public class AtomicityAnalyzer extends QualityCriterion {

    /**
     * Conjunction words that commonly indicate multiple actions or compound functionality.
     * These words are linguistic signals that a story may be describing more than one action.
     */
    private static final List<String> CONJUNCTION_WORDS = Arrays.asList(
        "and", "or", "then", "also", "additionally", "furthermore", "moreover", "but"
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

    /**
     * Determines if a user story is atomic by checking for multiple actions or goals.
     * 
     * @param story The user story to analyze
     * @return true if the story describes a single action, false if multiple actions are detected
     */
    private boolean isAtomic(UserStory story) {
        // Check parsed action goals - if multiple goals were identified during parsing
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
