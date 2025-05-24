package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive container for quality analysis results across multiple criteria and user stories.
 * 
 * QualityAnalysisResult aggregates the outcomes of running multiple quality criteria against
 * a set of user stories, providing both detailed problem information and summary statistics.
 * 
 * Jackson JSON Integration:
 * - Uses @JsonProperty annotations for clean JSON serialization/deserialization
 * - Supports export to external systems and persistent storage
 * - Enables API integration and automated quality reporting workflows
 * - Maintains data structure compatibility across different versions
 * 
 * Analysis Metrics:
 * - analysisDate: Timestamp for audit trails and temporal analysis
 * - analyzedStories: Complete set of user stories that were evaluated
 * - problemsByCriterion: Organized problem categorization for detailed reporting
 * - totalProblems: Quick summary metric for dashboard displays
 * - perfectStories: Count of stories with no quality issues detected
 * 
 * Reporting Integration:
 * - Used by QualityAnalysisManager for generating human-readable reports
 * - Displayed in JavaFX UI for interactive quality feedback
 * - Exported to JSON format for external tool integration
 * - Supports quality trend analysis over time
 * 
 * Quality Assurance Workflow:
 * 1. Analysis runs against selected user stories and criteria
 * 2. Results are aggregated into this comprehensive structure
 * 3. Summary statistics provide quick quality overview
 * 4. Detailed problems enable targeted improvement efforts
 * 5. Export capabilities support continuous integration pipelines
 * 
 * @see QualityAnalysisManager
 * @see QualityProblem
 * @see QualityCriterion
 */
public class QualityAnalysisResult {
    /** Timestamp when the quality analysis was performed, used for audit trails */
    @JsonProperty("analysisDate")
    private Date analysisDate;
    
    /** Complete list of user stories that were analyzed for quality issues */
    @JsonProperty("analyzedStories")
    private List<UserStory> analyzedStories;
    
    /** Problems organized by quality criterion for structured reporting and filtering */
    @JsonProperty("problemsByCriterion")
    private Map<String, List<QualityProblem>> problemsByCriterion;
    
    /** Total count of quality problems found across all criteria and stories */
    @JsonProperty("totalProblems")
    private int totalProblems;
    
    /** Count of user stories that passed all quality criteria without issues */
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
