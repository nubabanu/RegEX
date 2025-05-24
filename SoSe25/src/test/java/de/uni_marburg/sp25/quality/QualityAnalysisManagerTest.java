package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class QualityAnalysisManagerTest {

    private QualityAnalysisManager qualityManager;
    private ResourceBundle messages;
    private UserStory wellFormedStory;
    private UserStory malformedStory;
    private UserStory nonAtomicStory; 

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Locale testLocale = Locale.ENGLISH; // Or Locale.GERMAN if you want to test German messages
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", testLocale);
        qualityManager = new QualityAnalysisManager(messages);

        wellFormedStory = new UserStory();
        wellFormedStory.setPid("#G01#");
        wellFormedStory.setText("As a User, I want to login, so that I can access my account.");
        wellFormedStory.setPersona(List.of("User"));
        wellFormedStory.setActionGoal(List.of("login"));
        wellFormedStory.setEntityGoal(List.of("account")); 
        wellFormedStory.setBenefit("I can access my account.");
        wellFormedStory.setActionBenefit(List.of("access"));
        wellFormedStory.setEntityBenefit(List.of("my account"));


        malformedStory = new UserStory();
        malformedStory.setPid("#G02#");
        malformedStory.setText("This is not a user story.");
        malformedStory.setPersona(List.of("Unknown"));
        malformedStory.setActionGoal(List.of("Unknown"));
        malformedStory.setEntityGoal(List.of("Unknown"));
        malformedStory.setBenefit("Unknown");
        malformedStory.setActionBenefit(List.of());
        malformedStory.setEntityBenefit(List.of());

        nonAtomicStory = new UserStory();
        nonAtomicStory.setPid("#G03#");
        nonAtomicStory.setText("As a User, I want to view products and add them to the cart.");
        nonAtomicStory.setPersona(List.of("User"));
        nonAtomicStory.setActionGoal(List.of("view", "add")); // Multiple actions
        nonAtomicStory.setEntityGoal(List.of("products", "cart"));
        nonAtomicStory.setBenefit("I can purchase items.");
        nonAtomicStory.setActionBenefit(List.of("purchase"));
        nonAtomicStory.setEntityBenefit(List.of("items"));
    }

    @Test
    void testGetAvailableCriteriaNames() {
        List<String> criteriaNames = qualityManager.getAvailableCriteriaNames();
        assertNotNull(criteriaNames);
        assertFalse(criteriaNames.isEmpty());
        assertTrue(criteriaNames.contains("wellFormedness"));
        assertTrue(criteriaNames.contains("atomicity"));
       
    }

    @Test
    void testGetCriterionDisplayName() {
        String displayName = qualityManager.getCriterionDisplayName("wellFormedness");
        assertEquals("Well-Formedness", displayName); // Assuming English locale
        
        // Test with a different criterion
        String atomicityDisplayName = qualityManager.getCriterionDisplayName("atomicity");
        assertEquals("Atomicity", atomicityDisplayName);

       
    }

    @Test
    void testAnalyzeQuality_noStories() {
        List<UserStory> emptyList = new ArrayList<>();
        List<String> criteria = List.of("wellFormedness");
        QualityAnalysisResult result = qualityManager.analyzeQuality(emptyList, criteria);

        assertNotNull(result);
        assertEquals(0, result.getAnalyzedStories().size());
        assertEquals(0, result.getTotalProblems());
        assertEquals(0, result.getPerfectStories());
        assertTrue(result.getProblemsByCriterion().get("wellFormedness").isEmpty());
    }

    @Test
    void testAnalyzeQuality_oneWellFormedStory() {
        List<UserStory> stories = List.of(wellFormedStory);
        List<String> criteria = List.of("wellFormedness");
        QualityAnalysisResult result = qualityManager.analyzeQuality(stories, criteria);

        assertNotNull(result);
        assertEquals(1, result.getAnalyzedStories().size());
        assertEquals(0, result.getTotalProblems());
        assertEquals(1, result.getPerfectStories());
        assertTrue(result.getProblemsByCriterion().get("wellFormedness").isEmpty());
    }

    @Test
    void testAnalyzeQuality_oneMalformedStory() {
        List<UserStory> stories = List.of(malformedStory);
        List<String> criteria = List.of("wellFormedness");
        QualityAnalysisResult result = qualityManager.analyzeQuality(stories, criteria);

        assertNotNull(result);
        assertEquals(1, result.getAnalyzedStories().size());
        assertEquals(1, result.getTotalProblems());
        assertEquals(0, result.getPerfectStories());
        List<QualityProblem> problems = result.getProblemsByCriterion().get("wellFormedness");
        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals(messages.getString("quality.problem.notWellFormed"), problems.get(0).getProblemDescription());
        assertEquals(malformedStory.getPid(), problems.get(0).getAffectedStories().get(0).getPid());
    }

    @Test
    void testAnalyzeQuality_mixedStories_wellFormednessCriterion() {
        List<UserStory> stories = List.of(wellFormedStory, malformedStory);
        List<String> criteria = List.of("wellFormedness");
        QualityAnalysisResult result = qualityManager.analyzeQuality(stories, criteria);

        assertNotNull(result);
        assertEquals(2, result.getAnalyzedStories().size());
        assertEquals(1, result.getTotalProblems()); // Only malformedStory has a problem
        assertEquals(1, result.getPerfectStories()); // wellFormedStory is perfect
        
        List<QualityProblem> problems = result.getProblemsByCriterion().get("wellFormedness");
        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals(malformedStory.getPid(), problems.get(0).getAffectedStories().get(0).getPid());
    }

    @Test
    void testAnalyzeQuality_noCriteriaSelected() {
        List<UserStory> stories = List.of(wellFormedStory, malformedStory);
        List<String> criteria = new ArrayList<>(); // No criteria selected
        QualityAnalysisResult result = qualityManager.analyzeQuality(stories, criteria);

        assertNotNull(result);
        assertEquals(2, result.getAnalyzedStories().size());
        assertEquals(0, result.getTotalProblems()); // No criteria, so no problems found
        assertEquals(2, result.getPerfectStories()); // No criteria, so all are considered "perfect" in this context
        assertTrue(result.getProblemsByCriterion().isEmpty());
    }
    
    @Test
    void testAnalyzeQuality_multipleCriteria_oneProblem() {
        // Assuming only WellFormedness will find a problem with malformedStory
        // and Atomicity (or others) will find no problems with these simple stories.
        List<UserStory> stories = List.of(wellFormedStory, malformedStory);
        List<String> criteria = List.of("wellFormedness", "atomicity");
        QualityAnalysisResult result = qualityManager.analyzeQuality(stories, criteria);

        assertNotNull(result);
        assertEquals(2, result.getAnalyzedStories().size());
        assertEquals(1, result.getTotalProblems()); // Only from wellFormedness
        assertEquals(1, result.getPerfectStories());

        // Check wellFormedness problems
        List<QualityProblem> wellFormednessProblems = result.getProblemsByCriterion().get("wellFormedness");
        assertNotNull(wellFormednessProblems);
        assertEquals(1, wellFormednessProblems.size());
        assertEquals(malformedStory.getPid(), wellFormednessProblems.get(0).getAffectedStories().get(0).getPid());

        // Check atomicity problems (assuming it finds none for these stories)
        List<QualityProblem> atomicityProblems = result.getProblemsByCriterion().get("atomicity");
        assertNotNull(atomicityProblems);
        assertTrue(atomicityProblems.isEmpty(), "Atomicity should not find problems for these specific stories.");
    }


    @Test
    void testAnalyzeQuality_wellFormedButNonAtomicStory() {
        List<UserStory> stories = List.of(nonAtomicStory);
        List<String> criteria = List.of("wellFormedness", "atomicity");
        QualityAnalysisResult result = qualityManager.analyzeQuality(stories, criteria);

        assertNotNull(result);
        assertEquals(1, result.getAnalyzedStories().size());
        assertEquals(1, result.getTotalProblems()); // Should have 1 problem (atomicity)
        assertEquals(0, result.getPerfectStories());

        List<QualityProblem> wellFormednessProblems = result.getProblemsByCriterion().get("wellFormedness");
        assertNotNull(wellFormednessProblems);
        assertTrue(wellFormednessProblems.isEmpty(), "Story should be well-formed.");

        List<QualityProblem> atomicityProblems = result.getProblemsByCriterion().get("atomicity");
        assertNotNull(atomicityProblems);
        assertEquals(1, atomicityProblems.size(), "Should find one atomicity problem.");
        assertEquals(messages.getString("quality.problem.notAtomic"), atomicityProblems.get(0).getProblemDescription());
        assertEquals(nonAtomicStory.getPid(), atomicityProblems.get(0).getAffectedStories().get(0).getPid());
    }

    @Test
    void testAnalyzeQuality_invalidCriterionKey() {
        List<UserStory> stories = List.of(wellFormedStory);
        List<String> criteria = List.of("wellFormedness", "invalidCriterionKey");
        QualityAnalysisResult result = qualityManager.analyzeQuality(stories, criteria);

        assertNotNull(result);
        assertEquals(1, result.getAnalyzedStories().size());
        assertEquals(0, result.getTotalProblems()); // Invalid key should be ignored, no problem from wellFormedness
        assertEquals(1, result.getPerfectStories());
        assertNotNull(result.getProblemsByCriterion().get("wellFormedness"));
        assertTrue(result.getProblemsByCriterion().get("wellFormedness").isEmpty());
        assertNull(result.getProblemsByCriterion().get("invalidCriterionKey"), "Invalid criterion should not have an entry or it should be empty if initialized.");
    }
    
    @Test
    void testGenerateReport_multipleProblemsDifferentCriteria() {
        List<UserStory> stories = List.of(malformedStory, nonAtomicStory);
        List<String> criteria = List.of("wellFormedness", "atomicity");
        QualityAnalysisResult analysisResult = qualityManager.analyzeQuality(stories, criteria);
        
        assertEquals(2, analysisResult.getTotalProblems());

        String report = qualityManager.generateReport(analysisResult, "multi_problem_file.txt");

        assertNotNull(report);
        assertTrue(report.contains("Quality Report"));
        assertTrue(report.contains("User Stories: \"multi_problem_file.txt\"")); // Single escape for quote in string literal
        assertTrue(report.contains("Total Stories Analyzed: 2"));
        assertTrue(report.contains("Perfect Stories: 0"));
        assertTrue(report.contains("Total Problems Found: 2"));

        // Check Well-Formedness section
        assertTrue(report.contains("Quality criterion: \"Well-Formedness\""));
        assertTrue(report.contains("Number of quality problems: 1"));
        assertTrue(report.contains("User Story: \"" + malformedStory.getText() + "\""));
        assertTrue(report.contains("Problem: \"" + messages.getString("quality.problem.notWellFormed") + "\""));

        // Check Atomicity section
        assertTrue(report.contains("Quality criterion: \"Atomicity\""));
        assertTrue(report.contains("Number of quality problems: 1"));
        assertTrue(report.contains("User Story: \"" + nonAtomicStory.getText() + "\""));
        assertTrue(report.contains("Problem: \"" + messages.getString("quality.problem.notAtomic") + "\""));
    }

    @Test
    void testSaveResultsToJson_emptyResults() throws IOException {
        List<UserStory> emptyStories = new ArrayList<>();
        List<String> criteria = List.of("wellFormedness");
        QualityAnalysisResult analysisResult = qualityManager.analyzeQuality(emptyStories, criteria);

        Path outputFile = tempDir.resolve("empty_quality_report.json");
        qualityManager.saveResultsToJson(analysisResult, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        String jsonContent = Files.readString(outputFile);
        assertTrue(jsonContent.contains("\"analyzedStories\":[]"));
        assertTrue(jsonContent.contains("\"totalProblems\":0"));
        assertTrue(jsonContent.contains("\"perfectStories\":0"));
        // Check that the problemsByCriterion map for wellFormedness is present and empty
        assertTrue(jsonContent.contains("\"problemsByCriterion\":{\"wellFormedness\":[]}") || jsonContent.contains("\"problemsByCriterion\":{\"wellFormedness\": []}")); // Allow for space variation
    }

    @Test
    void testSaveResultsToJson_multipleCriteriaResults() throws IOException {
        List<UserStory> stories = List.of(malformedStory, nonAtomicStory, wellFormedStory);
        List<String> criteria = List.of("wellFormedness", "atomicity");
        QualityAnalysisResult analysisResult = qualityManager.analyzeQuality(stories, criteria);

        Path outputFile = tempDir.resolve("multi_criteria_quality_report.json");
        qualityManager.saveResultsToJson(analysisResult, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        String jsonContent = Files.readString(outputFile);

        assertTrue(jsonContent.contains(malformedStory.getPid()));
        assertTrue(jsonContent.contains(nonAtomicStory.getPid()));
        assertTrue(jsonContent.contains(wellFormedStory.getPid()));
        assertTrue(jsonContent.contains("\"totalProblems\":2"));
        assertTrue(jsonContent.contains("\"perfectStories\":1"));
        
        // Check for wellFormedness problems
        // Construct the expected JSON snippet for the problem description
        String expectedWellFormednessProblemJson = "\"problemDescription\":\"" + messages.getString("quality.problem.notWellFormed").replace("\"", "\\\"") + "\"";
        assertTrue(jsonContent.contains("\"wellFormedness\":[")); 
        assertTrue(jsonContent.contains(expectedWellFormednessProblemJson));
        
        // Check for atomicity problems
        String expectedAtomicityProblemJson = "\"problemDescription\":\"" + messages.getString("quality.problem.notAtomic").replace("\"", "\\\"") + "\"";
        assertTrue(jsonContent.contains("\"atomicity\":["));
        assertTrue(jsonContent.contains(expectedAtomicityProblemJson));
    }
}
