package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages quality analysis of user stories
 */
public class QualityAnalysisManager {
    private ResourceBundle messages;
    private Map<String, QualityCriterion> availableCriteria;

    public QualityAnalysisManager(ResourceBundle messages) {
        this.messages = messages;
        initializeCriteria();
    }

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

    public List<String> getAvailableCriteriaNames() {
        return new ArrayList<>(availableCriteria.keySet());
    }

    public String getCriterionDisplayName(String criterionKey) {
        return messages.getString("quality.criterion." + criterionKey);
    }

    /**
     * Performs quality analysis on the given user stories
     */
    public QualityAnalysisResult analyzeQuality(List<UserStory> userStories, List<String> selectedCriteria) {
        QualityAnalysisResult result = new QualityAnalysisResult();
        result.setAnalyzedStories(userStories);
        result.setAnalysisDate(new Date());

        Map<String, List<QualityProblem>> problemsByCriterion = new HashMap<>();
        int totalProblems = 0;
        int perfectStories = 0;

        for (String criterionKey : selectedCriteria) {
            QualityCriterion criterion = availableCriteria.get(criterionKey);
            if (criterion != null) {
                List<QualityProblem> problems = criterion.analyze(userStories);
                problemsByCriterion.put(criterionKey, problems);
                totalProblems += problems.size();
            }
        }

        // Count perfect stories (stories with no quality problems)
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
     * Generates a text report from analysis results
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
     * Saves quality analysis results to JSON file
     */
    public void saveResultsToJson(QualityAnalysisResult result, String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filePath), result);
    }
}
