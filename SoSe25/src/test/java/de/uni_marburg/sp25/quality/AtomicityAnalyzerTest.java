package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class AtomicityAnalyzerTest {

    private AtomicityAnalyzer analyzer;
    private UserStory atomicStory;
    private UserStory nonAtomicStoryAndInGoal;
    private UserStory nonAtomicStoryButInGoal;
    private UserStory nonAtomicStoryAndInBenefit;
    private UserStory nonAtomicStoryButInBenefit;
    private UserStory nonAtomicStoryMultipleConnectorsInGoal;
    private UserStory storyWithAndInRole;
    private ResourceBundle messages;

    @BeforeEach
    void setUp() {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH);
        analyzer = new AtomicityAnalyzer(messages);

        atomicStory = new UserStory();
        atomicStory.setPid("US1");
        atomicStory.setText("As a user, I want to log in, so that I can access my account.");
        atomicStory.setPersona(List.of("user"));
        atomicStory.setActionGoal(List.of("log in"));
        atomicStory.setBenefit("I can access my account");

        nonAtomicStoryAndInGoal = new UserStory();
        nonAtomicStoryAndInGoal.setPid("US2");
        nonAtomicStoryAndInGoal.setText("As a user, I want to register and log in, so that I can use the system.");
        nonAtomicStoryAndInGoal.setPersona(List.of("user"));
        nonAtomicStoryAndInGoal.setActionGoal(List.of("register", "log in")); 
        nonAtomicStoryAndInGoal.setBenefit("I can use the system");

        nonAtomicStoryButInGoal = new UserStory();
        nonAtomicStoryButInGoal.setPid("US3");
        nonAtomicStoryButInGoal.setText("As a customer, I want a refund but I also want to keep the product, so that I am satisfied.");
        nonAtomicStoryButInGoal.setPersona(List.of("customer"));
        nonAtomicStoryButInGoal.setActionGoal(List.of("a refund", "keep the product"));
        nonAtomicStoryButInGoal.setBenefit("I am satisfied");

        nonAtomicStoryMultipleConnectorsInGoal = new UserStory();
        nonAtomicStoryMultipleConnectorsInGoal.setPid("US4");
        nonAtomicStoryMultipleConnectorsInGoal.setText("As a manager, I want to view reports and export data but not share it, so I can make decisions.");
        nonAtomicStoryMultipleConnectorsInGoal.setPersona(List.of("manager"));
        nonAtomicStoryMultipleConnectorsInGoal.setActionGoal(List.of("view reports", "export data", "not share it"));
        nonAtomicStoryMultipleConnectorsInGoal.setBenefit("I can make decisions");

        storyWithAndInRole = new UserStory();
        storyWithAndInRole.setPid("US5");
        storyWithAndInRole.setText("As a husband and father, I want to plan a family vacation, so that we can relax.");
        storyWithAndInRole.setPersona(List.of("husband and father"));
        storyWithAndInRole.setActionGoal(List.of("plan a family vacation"));
        storyWithAndInRole.setBenefit("we can relax");
        
        nonAtomicStoryAndInBenefit = new UserStory();
        nonAtomicStoryAndInBenefit.setPid("US6");
        nonAtomicStoryAndInBenefit.setText("As a shopper, I want to compare prices, so that I save money and get the best deal.");
        nonAtomicStoryAndInBenefit.setPersona(List.of("shopper"));
        nonAtomicStoryAndInBenefit.setActionGoal(List.of("compare prices"));
        nonAtomicStoryAndInBenefit.setBenefit("I save money and get the best deal");

        nonAtomicStoryButInBenefit = new UserStory();
        nonAtomicStoryButInBenefit.setPid("US7");
        nonAtomicStoryButInBenefit.setText("As a user, I want to submit feedback, so that the product improves but I get a discount.");
        nonAtomicStoryButInBenefit.setPersona(List.of("user"));
        nonAtomicStoryButInBenefit.setActionGoal(List.of("submit feedback"));
        nonAtomicStoryButInBenefit.setBenefit("the product improves but I get a discount");
    }

    @Test
    void testAnalyze_atomicStory() {
        List<QualityProblem> problems = analyzer.analyze(List.of(atomicStory));
        assertTrue(problems.isEmpty(), "Atomic story should have no problems. Text: " + atomicStory.getText());
    }

    @Test
    void testAnalyze_nonAtomicStory_withAndInGoal() {
        List<QualityProblem> problems = analyzer.analyze(List.of(nonAtomicStoryAndInGoal));
        assertEquals(1, problems.size(), "Non-atomic story (with 'and' in goal) should have one problem. Text: " + nonAtomicStoryAndInGoal.getText());
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notAtomic"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonAtomicStoryAndInGoal));
    }

    @Test
    void testAnalyze_nonAtomicStory_withButInGoal() {
        List<QualityProblem> problems = analyzer.analyze(List.of(nonAtomicStoryButInGoal));
        assertEquals(1, problems.size(), "Non-atomic story (with 'but' in goal) should have one problem. Text: " + nonAtomicStoryButInGoal.getText());
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notAtomic"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonAtomicStoryButInGoal));
    }
    
    @Test
    void testAnalyze_nonAtomicStory_multipleConnectorsInGoal() {
        List<QualityProblem> problems = analyzer.analyze(List.of(nonAtomicStoryMultipleConnectorsInGoal));
        assertEquals(1, problems.size(), "Non-atomic story with multiple connectors in goal should have one problem. Text: " + nonAtomicStoryMultipleConnectorsInGoal.getText());
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notAtomic"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonAtomicStoryMultipleConnectorsInGoal));
    }

    @Test
    void testAnalyze_storyWithAndInRole_shouldBeAtomic() {
        List<QualityProblem> problems = analyzer.analyze(List.of(storyWithAndInRole));
        assertTrue(problems.isEmpty(), "Story with 'and' in the role part should still be considered atomic. Text: " + storyWithAndInRole.getText());
    }

    @Test
    void testAnalyze_nonAtomicStory_withAndInBenefit() {
        List<QualityProblem> problems = analyzer.analyze(List.of(nonAtomicStoryAndInBenefit));
         assertEquals(1, problems.size(), "Story with 'and' in the benefit part should be non-atomic. Text: " + nonAtomicStoryAndInBenefit.getText());
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notAtomic"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonAtomicStoryAndInBenefit));
    }

    @Test
    void testAnalyze_nonAtomicStory_withButInBenefit() {
        List<QualityProblem> problems = analyzer.analyze(List.of(nonAtomicStoryButInBenefit));
        assertEquals(1, problems.size(), "Story with 'but' in the benefit part should be non-atomic. Text: " + nonAtomicStoryButInBenefit.getText());
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notAtomic"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonAtomicStoryButInBenefit));
    }
    
    @Test
    void testAnalyze_multipleStories_mixed() {
        // atomicStory: 0 problems
        // nonAtomicStoryAndInGoal: 1 problem (and in goal)
        // storyWithAndInRole: 0 problems (and in role is fine)
        // nonAtomicStoryButInBenefit: 1 problem (but in benefit)
        List<QualityProblem> problems = analyzer.analyze(List.of(atomicStory, nonAtomicStoryAndInGoal, storyWithAndInRole, nonAtomicStoryButInBenefit));
        assertEquals(2, problems.size(), 
            "Should find 2 problems: one for nonAtomicStoryAndInGoal and one for nonAtomicStoryButInBenefit.");

        // Verify that the problems are for the correct stories
        boolean foundProblemForAndInGoal = false;
        boolean foundProblemForButInBenefit = false;
        for (QualityProblem p : problems) {
            if (p.getAffectedStories().contains(nonAtomicStoryAndInGoal)) {
                foundProblemForAndInGoal = true;
            }
            if (p.getAffectedStories().contains(nonAtomicStoryButInBenefit)) {
                foundProblemForButInBenefit = true;
            }
        }
        assertTrue(foundProblemForAndInGoal, "Problem for nonAtomicStoryAndInGoal not found.");
        assertTrue(foundProblemForButInBenefit, "Problem for nonAtomicStoryButInBenefit not found.");
    }

    @Test
    void testAnalyze_noStories() {
        List<QualityProblem> problems = analyzer.analyze(List.of());
        assertTrue(problems.isEmpty(), "No stories should result in no problems.");
    }
}
