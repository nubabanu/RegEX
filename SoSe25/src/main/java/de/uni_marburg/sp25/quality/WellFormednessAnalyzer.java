package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for well-formedness
 * A user story is well-formed if it contains at least a role and a goal
 */
public class WellFormednessAnalyzer extends QualityCriterion {

    public WellFormednessAnalyzer(ResourceBundle messages) {
        super("wellFormedness", messages);
    }

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

    private boolean isWellFormed(UserStory story) {
        // Check if story has a role and a goal
        if (story.getPersona() == null || story.getPersona().isEmpty()) {
            return false;
        }
        
        if (story.getActionGoal() == null || story.getActionGoal().isEmpty()) {
            return false;
        }

        // Check for "Unknown" values which indicate parsing issues
        if (story.getPersona().contains("Unknown") || story.getActionGoal().contains("Unknown")) {
            return false;
        }

        return true;
    }
}
