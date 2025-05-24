package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for well-formedness according to the INVEST criteria.
 * 
 * A user story is considered well-formed if it contains the fundamental structural elements
 * required for meaningful implementation: at least a role (persona) and a goal (action).
 * This represents the minimum viable structure for a user story to be actionable.
 * 
 * Well-formedness criteria:
 * 1. Must have at least one persona (user role) defined
 * 2. Must have at least one action goal defined
 * 3. Must not contain "Unknown" values indicating parsing or extraction failures
 * 
 * This analyzer serves as a prerequisite check for other quality criteria, as poorly
 * structured stories cannot be meaningfully evaluated for atomicity, minimality, or
 * other quality aspects. It integrates with the natural language processing pipeline
 * to identify stories that failed to extract essential components.
 * 
 * Dependencies:
 * - ResourceBundle: For internationalized error messages
 * - Extends QualityCriterion: Provides base functionality for quality analysis
 * - Integrates with UserStory NLP components: persona and actionGoal extraction
 */
public class WellFormednessAnalyzer extends QualityCriterion {

    /**
     * Constructs a WellFormednessAnalyzer with internationalized messages.
     * 
     * @param messages ResourceBundle containing localized error messages and descriptions
     */
    public WellFormednessAnalyzer(ResourceBundle messages) {
        super("wellFormedness", messages);
    }

    /**
     * Analyzes a collection of user stories for well-formedness violations.
     * 
     * Performs fundamental structural validation to ensure each story contains
     * the minimum required elements for implementation. Stories that lack basic
     * structure cannot be effectively analyzed by other quality criteria.
     * 
     * @param userStories List of UserStory objects to analyze
     * @return List of QualityProblem objects identifying malformed stories
     */
    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (UserStory story : userStories) {
            if (!isWellFormed(story)) {
                problems.add(new QualityProblem(
                    criterionName,
                    messages.getString("quality.problem.notWellFormed"),
                    List.of(story),
                    "NOT_WELL_FORMED"
                ));
            }
        }

        return problems;
    }

    /**
     * Determines if a user story meets well-formedness requirements.
     * 
     * Validates that the story has been successfully parsed and contains
     * the essential structural elements:
     * - At least one persona (user role)
     * - At least one action goal
     * - No "Unknown" placeholder values indicating extraction failures
     * 
     * @param story UserStory object to evaluate
     * @return true if story meets well-formedness criteria, false otherwise
     */
    private boolean isWellFormed(UserStory story) {
        // Verify persona exists and is properly extracted
        if (story.getPersona() == null || story.getPersona().isEmpty()) {
            return false;
        }
        
        // Verify action goal exists and is properly extracted
        if (story.getActionGoal() == null || story.getActionGoal().isEmpty()) {
            return false;
        }

        // Check for extraction failures indicated by "Unknown" placeholders
        // These indicate the NLP pipeline failed to identify essential components
        if (story.getPersona().contains("Unknown") || story.getActionGoal().contains("Unknown")) {
            return false;
        }

        return true;
    }
}
