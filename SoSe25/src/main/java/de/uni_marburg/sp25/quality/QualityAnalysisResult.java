package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Contains the results of a quality analysis
 */
public class QualityAnalysisResult {
    @JsonProperty("analysisDate")
    private Date analysisDate;
    
    @JsonProperty("analyzedStories")
    private List<UserStory> analyzedStories;
    
    @JsonProperty("problemsByCriterion")
    private Map<String, List<QualityProblem>> problemsByCriterion;
    
    @JsonProperty("totalProblems")
    private int totalProblems;
    
    @JsonProperty("perfectStories")
    private int perfectStories;

    // Getters and setters
    public Date getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(Date analysisDate) { this.analysisDate = analysisDate; }
    
    public List<UserStory> getAnalyzedStories() { return analyzedStories; }
    public void setAnalyzedStories(List<UserStory> analyzedStories) { this.analyzedStories = analyzedStories; }
    
    public Map<String, List<QualityProblem>> getProblemsByCriterion() { return problemsByCriterion; }
    public void setProblemsByCriterion(Map<String, List<QualityProblem>> problemsByCriterion) { this.problemsByCriterion = problemsByCriterion; }
    
    public int getTotalProblems() { return totalProblems; }
    public void setTotalProblems(int totalProblems) { this.totalProblems = totalProblems; }
    
    public int getPerfectStories() { return perfectStories; }
    public void setPerfectStories(int perfectStories) { this.perfectStories = perfectStories; }
}
