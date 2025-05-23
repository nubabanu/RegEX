package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Analyzes user stories for uniformity
 * A user story is uniform if it follows the standard "As a..., I want..., so that..." format
 */
public class UniformityAnalyzer extends QualityCriterion {

    private static final Pattern STANDARD_FORMAT = Pattern.compile(
        "^As (?:a|an) .+, I want .+(?:, so that .+)?\\.$", 
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
        if (story.getText() == null) {
            return false;
        }
        
        return STANDARD_FORMAT.matcher(story.getText().trim()).matches();
    }
}
