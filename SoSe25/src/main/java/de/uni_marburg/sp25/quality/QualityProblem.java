package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.List;

/**
 * Represents a quality problem found in user stories
 */
public class QualityProblem {
    private String criterionName;
    private String problemDescription;
    private List<UserStory> affectedStories;
    private String problemType;

    public QualityProblem(String criterionName, String problemDescription, List<UserStory> affectedStories, String problemType) {
        this.criterionName = criterionName;
        this.problemDescription = problemDescription;
        this.affectedStories = affectedStories;
        this.problemType = problemType;
    }

    // Getters and setters
    public String getCriterionName() { return criterionName; }
    public void setCriterionName(String criterionName) { this.criterionName = criterionName; }
    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }
    public List<UserStory> getAffectedStories() { return affectedStories; }
    public void setAffectedStories(List<UserStory> affectedStories) { this.affectedStories = affectedStories; }
    public String getProblemType() { return problemType; }
    public void setProblemType(String problemType) { this.problemType = problemType; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Problem: ").append(problemDescription).append("\n");
        for (UserStory story : affectedStories) {
            sb.append("  User Story: ").append(story.getText()).append("\n");
        }
        return sb.toString();
    }
}
