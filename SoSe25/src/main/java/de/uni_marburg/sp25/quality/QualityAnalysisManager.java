package de.uni_marburg.sp25.quality;

// Application imports
import de.uni_marburg.sp25.UserStory;

// Jackson library for JSON report export
import com.fasterxml.jackson.databind.ObjectMapper;

// Standard Java imports
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Central coordinator for user story quality analysis across multiple criteria.
 * 
 * This manager orchestrates quality assessment by:
 * - Maintaining a registry of available quality criteria analyzers
 * - Coordinating analysis execution across selected criteria
 * - Aggregating results from multiple analyzers into comprehensive reports
 * - Providing internationalized criterion names and descriptions
 * - Generating both text and JSON format analysis reports
 * 
 * Quality Criteria Architecture:
 * Each quality criterion is implemented as a separate analyzer class extending QualityCriterion.
 * This allows for modular addition of new quality measures and independent testing of each criterion.
 * 
 * Available Quality Criteria:
 * - Well-formedness: Checks for proper user story structure (As a..., I want..., So that...)
 * - Atomicity: Ensures each story focuses on a single functionality 
 * - Uniformity: Validates consistent language and terminology usage
 * - Minimality: Verifies stories contain only essential information
 * - Redundancy: Identifies duplicate or overlapping stories
 * - Completeness: Checks for missing information in story components
 * - Independence: Ensures stories can be implemented independently
 * - Conflict-free: Detects contradictory requirements between stories
 * 
 * Usage Pattern:
 * 1. Load criteria via initializeCriteria()
 * 2. Select desired criteria for analysis
 * 3. Execute analyzeQuality() with user stories and selected criteria
 * 4. Generate reports via generateReport() or export as JSON
 */
public class QualityAnalysisManager {
    
    private ResourceBundle messages;
    private Map<String, QualityCriterion> availableCriteria;

    /**
     * Initializes the quality analysis manager with localized messages.
     * @param messages ResourceBundle for internationalized text and criterion names
     */
    public QualityAnalysisManager(ResourceBundle messages) {
        this.messages = messages;
        initializeCriteria();
    }

    /**
     * Updates the ResourceBundle and reinitializes criteria for language changes.
     * @param messages New ResourceBundle with different locale
     */
    public void setMessages(ResourceBundle messages) {
        this.messages = messages;
        initializeCriteria();
    }

    /**
     * Initializes all available quality criteria analyzers.
     * Each analyzer is instantiated with the current ResourceBundle for localized messages.
     * Additional criteria can be integrated here following the same pattern.
     */
    private void initializeCriteria() {
        availableCriteria = new HashMap<>();
        availableCriteria.put("wellFormedness", new WellFormednessAnalyzer(messages));
        availableCriteria.put("atomicity", new AtomicityAnalyzer(messages));
        availableCriteria.put("uniformity", new UniformityAnalyzer(messages));
        availableCriteria.put("minimality", new MinimalityAnalyzer(messages));
        availableCriteria.put("redundancy", new RedundancyAnalyzer(messages));
        availableCriteria.put("completeness", new CompletenessAnalyzer(messages));
        availableCriteria.put("independence", new IndependenceAnalyzer(messages));
        availableCriteria.put("conflictFree", new ConflictFreeAnalyzer(messages));
    }

    /**
     * Returns the list of available criterion keys for UI selection.
     * @return List of criterion identifiers that can be used for analysis
     */
    public List<String> getAvailableCriteriaNames() {
        return new ArrayList<>(availableCriteria.keySet());
    }

    /**
     * Provides access to the complete criteria registry.
     * @return Map of criterion keys to their analyzer implementations
     */
    public Map<String, QualityCriterion> getAvailableCriteria() {
        return availableCriteria;
    }

    /**
     * Gets the localized display name for a quality criterion.
     * @param criterionKey The internal key for the criterion
     * @return Localized human-readable name for the criterion
     */
    public String getCriterionDisplayName(String criterionKey) {
        return messages.getString("quality.criterion." + criterionKey);
    }

    /**
     * Performs comprehensive quality analysis on user stories using selected criteria.
     * 
     * @param userStories The user stories to analyze
     * @param selectedCriteria List of criterion keys to apply during analysis
     * @return QualityAnalysisResult containing problems found, statistics, and metadata
     */
    public QualityAnalysisResult analyzeQuality(List<UserStory> userStories, List<String> selectedCriteria) {
        QualityAnalysisResult result = new QualityAnalysisResult();
        result.setAnalyzedStories(userStories);
        result.setAnalysisDate(new Date());

        Map<String, List<QualityProblem>> problemsByCriterion = new HashMap<>();
        int totalProblems = 0;
        int perfectStories = 0;

        // Execute analysis for each selected criterion
        for (String criterionKey : selectedCriteria) {
            QualityCriterion criterion = availableCriteria.get(criterionKey);
            if (criterion != null) {
                List<QualityProblem> problems = criterion.analyze(userStories);
                problemsByCriterion.put(criterionKey, problems);
                totalProblems += problems.size();
            }
        }

        // Calculate statistics: count stories with no quality problems
        Set<String> storiesWithProblems = new HashSet<>();
        for (List<QualityProblem> problems : problemsByCriterion.values()) {
            for (QualityProblem problem : problems) {
                for (UserStory story : problem.getAffectedStories()) {
                    storiesWithProblems.add(story.getPid());
                }
            }
        }
        perfectStories = userStories.size() - storiesWithProblems.size();

        result.setProblemsByCriterion(problemsByCriterion);
        result.setTotalProblems(totalProblems);
        result.setPerfectStories(perfectStories);

        return result;
    }

    /**
     * Generates a human-readable text report from quality analysis results.
     * 
     * @param result The analysis results to format
     * @param fileName Original filename for context in the report
     * @return Formatted text report with statistics and detailed problem descriptions
     */
    public String generateReport(QualityAnalysisResult result, String fileName) {
        StringBuilder report = new StringBuilder();
        
        report.append("Quality Report\n");
        report.append("• User Stories: \"").append(fileName).append("\"\n");
        report.append("• Total Stories Analyzed: ").append(result.getAnalyzedStories().size()).append("\n");
        report.append("• Perfect Stories: ").append(result.getPerfectStories()).append("\n");
        report.append("• Total Problems Found: ").append(result.getTotalProblems()).append("\n\n");

        for (Map.Entry<String, List<QualityProblem>> entry : result.getProblemsByCriterion().entrySet()) {
            String criterionKey = entry.getKey();
            List<QualityProblem> problems = entry.getValue();
            
            report.append("• Quality criterion: \"").append(getCriterionDisplayName(criterionKey)).append("\"\n");
            report.append("  - Number of quality problems: ").append(problems.size()).append("\n");
            
            if (!problems.isEmpty()) {
                report.append("  - Quality problems:\n");
                for (QualityProblem problem : problems) {
                    report.append("    * Quality problem\n");
                    for (UserStory story : problem.getAffectedStories()) {
                        report.append("      · User Story: \"").append(story.getText()).append("\"\n");
                    }
                    report.append("      · Problem: \"").append(problem.getProblemDescription()).append("\"\n");
                }
            }
            report.append("\n");
        }

        return report.toString();
    }

    /**
     * Saves quality analysis results to JSON file using Jackson ObjectMapper.
     * 
     * @param result The analysis results to serialize
     * @param filePath Target file path for JSON output
     * @throws IOException If file writing fails
     */
    public void saveResultsToJson(QualityAnalysisResult result, String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(filePath), result);
    }
}
