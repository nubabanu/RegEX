package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for independence
 * User stories should be implementable independently without dependencies on other stories
 */
public class IndependenceAnalyzer extends QualityCriterion {

    private static final List<String> DEPENDENCY_WORDS = Arrays.asList(
        "after", "before", "once", "when", "if", "provided that", "assuming",
        "depending on", "requires", "needs", "following", "subsequent to"
    );

    public IndependenceAnalyzer(ResourceBundle messages) {
        super("independence", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (UserStory story : userStories) {
            if (!isIndependent(story)) {
                problems.add(new QualityProblem(
                    criterionName,
                    messages.getString("quality.problem.dependent"),
                    List.of(story),
                    "DEPENDENT"
                ));
            }
        }

        return problems;
    }

    private boolean isIndependent(UserStory story) {
        if (story.getText() == null) {
            return true;
        }
        
        String text = story.getText().toLowerCase();
        
        // Check for explicit dependency words
        for (String dependencyWord : DEPENDENCY_WORDS) {
            if (text.contains(dependencyWord)) {
                return false;
            }
        }
        
        // Check for references to other user stories or features
        if (text.contains("previous") || text.contains("existing") || 
            text.contains("already") || text.contains("first")) {
            return false;
        }
        
        return true;
    }
}
