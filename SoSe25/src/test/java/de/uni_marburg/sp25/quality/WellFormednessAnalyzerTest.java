package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class WellFormednessAnalyzerTest {

    private WellFormednessAnalyzer analyzer;
    private UserStory wellFormedStory;
    private UserStory malformedStoryMissingRole;
    private UserStory malformedStoryMissingGoal;
    private UserStory malformedStoryMissingBenefit;
    private UserStory malformedStoryEmpty;
    private ResourceBundle messages;

    @BeforeEach
    void setUp() {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH);
        analyzer = new WellFormednessAnalyzer(messages);

        // Well-formed story
        wellFormedStory = new UserStory();
        wellFormedStory.setPid("US1");
        wellFormedStory.setText("As a user, I want to log in, so that I can access my account.");
        wellFormedStory.setPersona(List.of("user"));
        wellFormedStory.setActionGoal(List.of("log in"));
        wellFormedStory.setBenefit("I can access my account.");

        // Malformed: Missing Role (Persona)
        malformedStoryMissingRole = new UserStory();
        malformedStoryMissingRole.setPid("US2");
        malformedStoryMissingRole.setText("I want to see my dashboard, so that I can view my stats.");
        malformedStoryMissingRole.setPersona(List.of()); // Empty persona
        malformedStoryMissingRole.setActionGoal(List.of("see my dashboard"));
        malformedStoryMissingRole.setBenefit("I can view my stats.");

        // Malformed: Missing Goal (ActionGoal)
        malformedStoryMissingGoal = new UserStory();
        malformedStoryMissingGoal.setPid("US3");
        malformedStoryMissingGoal.setText("As a customer, so that I can receive support.");
        malformedStoryMissingGoal.setPersona(List.of("customer"));
        malformedStoryMissingGoal.setActionGoal(List.of()); // Empty action goal
        malformedStoryMissingGoal.setBenefit("I can receive support.");

        // Malformed: Missing Benefit (Benefit field is not checked by WellFormednessAnalyzer)
        // This story should be considered well-formed by the current WellFormednessAnalyzer logic
        malformedStoryMissingBenefit = new UserStory();
        malformedStoryMissingBenefit.setPid("US4");
        malformedStoryMissingBenefit.setText("As an admin, I want to manage users.");
        malformedStoryMissingBenefit.setPersona(List.of("admin"));
        malformedStoryMissingBenefit.setActionGoal(List.of("manage users"));
        malformedStoryMissingBenefit.setBenefit(null); // Benefit is null

        // Malformed: Empty text and effectively missing role and goal for the analyzer
        malformedStoryEmpty = new UserStory();
        malformedStoryEmpty.setPid("US5");
        malformedStoryEmpty.setText("");
        malformedStoryEmpty.setPersona(List.of()); // Empty persona
        malformedStoryEmpty.setActionGoal(List.of()); // Empty action goal
        malformedStoryEmpty.setBenefit("");
    }

    @Test
    void testAnalyze_wellFormedStory() {
        List<QualityProblem> problems = analyzer.analyze(List.of(wellFormedStory));
        assertTrue(problems.isEmpty(), "Well-formed story should have no problems.");
    }

    @Test
    void testAnalyze_malformedStory_missingRole() {
        // Ensure persona is explicitly set to empty or "Unknown" if that's the test case
        malformedStoryMissingRole.setPersona(List.of());
        List<QualityProblem> problems = analyzer.analyze(List.of(malformedStoryMissingRole));
        assertEquals(1, problems.size(), "Malformed story (missing role) should have one problem.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notWellFormed"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(malformedStoryMissingRole));
    }

    @Test
    void testAnalyze_malformedStory_missingGoal() {
        // Ensure actionGoal is explicitly set to empty or "Unknown"
        malformedStoryMissingGoal.setActionGoal(List.of());
        List<QualityProblem> problems = analyzer.analyze(List.of(malformedStoryMissingGoal));
        assertEquals(1, problems.size(), "Malformed story (missing goal) should have one problem.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notWellFormed"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(malformedStoryMissingGoal));
    }

    @Test
    void testAnalyze_malformedStory_missingBenefit() {
        // This test should assert that NO problem is found, as benefit is not part of well-formedness check.
        List<QualityProblem> problems = analyzer.analyze(List.of(malformedStoryMissingBenefit));
        assertTrue(problems.isEmpty(), "Story missing only benefit should be considered well-formed by this analyzer.");
    }
    
    @Test
    void testAnalyze_malformedStory_emptyText() {
        // Ensure relevant fields are empty for this test
        malformedStoryEmpty.setPersona(List.of());
        malformedStoryEmpty.setActionGoal(List.of());
        List<QualityProblem> problems = analyzer.analyze(List.of(malformedStoryEmpty));
        assertEquals(1, problems.size(), "Malformed story (empty text) should have one problem.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notWellFormed"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(malformedStoryEmpty));
    }

    @Test
    void testAnalyze_multipleStories_mixed() {
        // Ensure the malformed stories are indeed missing the critical parts for this test
        malformedStoryMissingGoal.setActionGoal(List.of());
        malformedStoryMissingRole.setPersona(List.of());
        
        List<QualityProblem> problems = analyzer.analyze(List.of(wellFormedStory, malformedStoryMissingGoal, malformedStoryMissingRole));
        assertEquals(2, problems.size(), "Should find problems in two malformed stories.");
        // Additional validation checks can be implemented to ensure correct story flagging.
    }

    @Test
    void testAnalyze_noStories() {
        List<QualityProblem> problems = analyzer.analyze(List.of());
        assertTrue(problems.isEmpty(), "No stories should result in no problems.");
    }
}
