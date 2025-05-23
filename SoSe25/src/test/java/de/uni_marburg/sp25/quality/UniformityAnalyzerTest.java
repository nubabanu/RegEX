package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class UniformityAnalyzerTest {

    private UniformityAnalyzer uniformityAnalyzer;
    private ResourceBundle messages;
    private UserStory uniformStory;
    private UserStory nonUniformStory_noWant;
    private UserStory nonUniformStory_noAsA;
    private UserStory nonUniformStory_noSoThat;
    private UserStory uniformStoryWithOptionalBenefit;

    @BeforeEach
    void setUp() {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH);
        uniformityAnalyzer = new UniformityAnalyzer(messages);

        uniformStory = new UserStory();
        uniformStory.setPid("#UNI01#");
        uniformStory.setText("As a User, I want to login, so that I can access my account.");
        // Assuming setters populate relevant fields for analysis if the analyzer uses them
        // For a text-based check, the text itself is primary.

        nonUniformStory_noWant = new UserStory();
        nonUniformStory_noWant.setPid("#UNI02#");
        nonUniformStory_noWant.setText("As an Administrator, I receive an email notification when a new user is registered.");
        // Example from document: "Dieses Beispiel ist nicht uniform, da kein Wunsch formuliert worden ist."

        nonUniformStory_noAsA = new UserStory();
        nonUniformStory_noAsA.setPid("#UNI03#");
        nonUniformStory_noAsA.setText("The system shall allow users to reset their password.");

        // Story that is uniform but the benefit part is optional
        uniformStoryWithOptionalBenefit = new UserStory();
        uniformStoryWithOptionalBenefit.setPid("#UNI04#");
        uniformStoryWithOptionalBenefit.setText("As a Customer, I want to view my order history.");

        nonUniformStory_noSoThat = new UserStory();
        nonUniformStory_noSoThat.setPid("#UNI05#");
        nonUniformStory_noSoThat.setText("As a User, I want to logout for security reasons."); // Missing "so that"
    }

    @Test
    void testAnalyze_noStories() {
        List<QualityProblem> problems = uniformityAnalyzer.analyze(List.of());
        assertTrue(problems.isEmpty(), "No problems should be found in an empty list.");
    }

    @Test
    void testAnalyze_uniformStory() {
        List<QualityProblem> problems = uniformityAnalyzer.analyze(List.of(uniformStory));
        assertTrue(problems.isEmpty(), "A uniform story should not have problems.");
    }

    @Test
    void testAnalyze_uniformStory_optionalBenefit() {
        List<QualityProblem> problems = uniformityAnalyzer.analyze(List.of(uniformStoryWithOptionalBenefit));
        assertTrue(problems.isEmpty(), "A uniform story with an optional (missing) benefit should be fine.");
    }

    @Test
    void testAnalyze_nonUniformStory_noWant() {
        // Example from the document
        List<QualityProblem> problems = uniformityAnalyzer.analyze(List.of(nonUniformStory_noWant));
        assertEquals(1, problems.size(), "Should detect one uniformity problem because 'I want' is missing or incorrect.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notUniform"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonUniformStory_noWant));
    }

    @Test
    void testAnalyze_nonUniformStory_noAsA() {
        List<QualityProblem> problems = uniformityAnalyzer.analyze(List.of(nonUniformStory_noAsA));
        assertEquals(1, problems.size(), "Should detect one uniformity problem because 'As a' is missing.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notUniform"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonUniformStory_noAsA));
    }
    
    @Test
    void testAnalyze_nonUniformStory_noSoThatButHasBenefitClause() {
        // This case tests if the story has a benefit but doesn't use "so that"
        // The definition: "As a <Role>, I want <Goal>, [so that <Benefit>]"
        // If a benefit exists, it should ideally be introduced by "so that"
        List<QualityProblem> problems = uniformityAnalyzer.analyze(List.of(nonUniformStory_noSoThat));
        assertEquals(1, problems.size(), "Should detect one uniformity problem because 'so that' is missing before benefit.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notUniform"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonUniformStory_noSoThat));
    }

    @Test
    void testAnalyze_mixedStories() {
        List<UserStory> stories = List.of(uniformStory, nonUniformStory_noWant, nonUniformStory_noAsA, uniformStoryWithOptionalBenefit);
        List<QualityProblem> problems = uniformityAnalyzer.analyze(stories);
        assertEquals(2, problems.size(), "Should detect two uniformity problems.");

        int problemCountForNoWant = 0;
        int problemCountForNoAsA = 0;

        for (QualityProblem p : problems) {
            if (p.getAffectedStories().contains(nonUniformStory_noWant)) {
                problemCountForNoWant++;
            }
            if (p.getAffectedStories().contains(nonUniformStory_noAsA)) {
                problemCountForNoAsA++;
            }
        }
        assertEquals(1, problemCountForNoWant, "Uniformity problem for story without 'I want' not found or found multiple times.");
        assertEquals(1, problemCountForNoAsA, "Uniformity problem for story without 'As a' not found or found multiple times.");
    }

    @Test
    void testGetCriterionName() {
        assertEquals("uniformity", uniformityAnalyzer.getCriterionName());
    }
}
