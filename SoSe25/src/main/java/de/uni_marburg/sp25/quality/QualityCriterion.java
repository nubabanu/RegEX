package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Abstract base class for quality criteria analysis
 */
public abstract class QualityCriterion {
    protected ResourceBundle messages;
    protected String criterionName;

    public QualityCriterion(String criterionName, ResourceBundle messages) {
        this.criterionName = criterionName;
        this.messages = messages;
    }

    /**
     * Analyzes the given user stories for this quality criterion
     * @param userStories the list of user stories to analyze
     * @return list of quality problems found
     */
    public abstract List<QualityProblem> analyze(List<UserStory> userStories);

    public String getCriterionName() {
        return criterionName;
    }
}
