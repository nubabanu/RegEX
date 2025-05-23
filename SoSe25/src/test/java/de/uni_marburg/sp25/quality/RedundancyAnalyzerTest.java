package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class RedundancyAnalyzerTest {

    private RedundancyAnalyzer redundancyAnalyzer;
    private ResourceBundle messages;
    private UserStory story1, story2, story3, story4, story5;

    @BeforeEach
    void setUp() {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH);
        redundancyAnalyzer = new RedundancyAnalyzer(messages);

        story1 = new UserStory();
        story1.setPid("#R01#");
        story1.setText("As a Visitor, I want to be able to see a list of news items, so that I stay up to date.");
        story1.setPersona(List.of("Visitor")); // Added persona
        story1.setActionGoal(List.of("see"));
        story1.setEntityGoal(List.of("list of news items"));
        story1.setBenefit("I stay up to date.");

        story2 = new UserStory();
        story2.setPid("#R02#");
        story2.setText("As a Visitor, I want to be able to view a list of news items, so that I stay up to date.");
        story2.setPersona(List.of("Visitor")); // Added persona
        story2.setActionGoal(List.of("view")); // Similar action to "see"
        story2.setEntityGoal(List.of("list of news items")); // Same entity
        story2.setBenefit("I stay up to date.");

        story3 = new UserStory();
        story3.setPid("#R03#");
        story3.setText("As an Administrator, I want to create a new user account, so that I am able to manage the users of the system.");
        story3.setPersona(List.of("Administrator")); // Added persona
        story3.setActionGoal(List.of("create"));
        story3.setEntityGoal(List.of("new user account"));
        story3.setBenefit("I am able to manage the users of the system.");

        story4 = new UserStory();
        story4.setPid("#R04#");
        story4.setText("As an Administrator, I want to edit the permission rights of a user account, so that I am able to manage the users of the system.");
        story4.setPersona(List.of("Administrator")); // Added persona
        story4.setActionGoal(List.of("edit"));
        story4.setEntityGoal(List.of("permission rights of a user account"));
        story4.setBenefit("I am able to manage the users of the system."); // Same benefit as story3

        story5 = new UserStory();
        story5.setPid("#R05#");
        story5.setText("As a User, I want to log out, so I can secure my session.");
        story5.setPersona(List.of("User")); // Added persona
        story5.setActionGoal(List.of("log out"));
        story5.setEntityGoal(List.of()); // No specific entity for goal
        story5.setBenefit("I can secure my session.");
    }

    @Test
    void testAnalyze_noStories() {
        List<QualityProblem> problems = redundancyAnalyzer.analyze(List.of());
        assertTrue(problems.isEmpty(), "No problems should be found in an empty list.");
    }

    @Test
    void testAnalyze_oneStory() {
        List<QualityProblem> problems = redundancyAnalyzer.analyze(List.of(story1));
        assertTrue(problems.isEmpty(), "No redundancy with only one story.");
    }

    @Test
    void testAnalyze_noRedundancy() {
        List<UserStory> stories = List.of(story1, story3, story5);
        List<QualityProblem> problems = redundancyAnalyzer.analyze(stories);
        assertTrue(problems.isEmpty(), "No redundancy should be detected for these distinct stories.");
    }

    @Test
    void testAnalyze_redundantByGoal() {
        
        List<UserStory> stories = List.of(story1, story2, story5);
        List<QualityProblem> problems = redundancyAnalyzer.analyze(stories);
        assertEquals(1, problems.size(), "Should detect one redundancy problem by goal.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.redundantGoal"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(story1));
        assertTrue(problem.getAffectedStories().contains(story2));
        assertEquals(2, problem.getAffectedStories().size());
    }

    @Test
    void testAnalyze_redundantByBenefit() {
       
        List<UserStory> stories = List.of(story3, story4, story5);
        List<QualityProblem> problems = redundancyAnalyzer.analyze(stories);
        assertEquals(1, problems.size(), "Should detect one redundancy problem by benefit.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.redundantBenefit"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(story3));
        assertTrue(problem.getAffectedStories().contains(story4));
        assertEquals(2, problem.getAffectedStories().size());
    }

    @Test
    void testAnalyze_multipleRedundancies() {
        List<UserStory> stories = List.of(story1, story2, story3, story4);
        List<QualityProblem> problems = redundancyAnalyzer.analyze(stories);
        assertEquals(2, problems.size(), "Should detect two redundancy problems (one by goal, one by benefit).");

        boolean foundGoalRedundancy = false;
        boolean foundBenefitRedundancy = false;

        for (QualityProblem problem : problems) {
            if (problem.getProblemDescription().equals(messages.getString("quality.problem.redundantGoal"))) {
                if (problem.getAffectedStories().contains(story1) && problem.getAffectedStories().contains(story2)) {
                    foundGoalRedundancy = true;
                }
            } else if (problem.getProblemDescription().equals(messages.getString("quality.problem.redundantBenefit"))) {
                if (problem.getAffectedStories().contains(story3) && problem.getAffectedStories().contains(story4)) {
                    foundBenefitRedundancy = true;
                }
            }
        }
        assertTrue(foundGoalRedundancy, "Goal redundancy between story1 and story2 not found or incorrect.");
        assertTrue(foundBenefitRedundancy, "Benefit redundancy between story3 and story4 not found or incorrect.");
    }

    @Test
    void testGetCriterionName() {
        assertEquals("redundancyFree", redundancyAnalyzer.getCriterionName());
    }
}
