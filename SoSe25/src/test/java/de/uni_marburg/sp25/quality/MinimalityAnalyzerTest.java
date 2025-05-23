package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class MinimalityAnalyzerTest {

    private MinimalityAnalyzer minimalityAnalyzer;
    private ResourceBundle messages;
    private UserStory minimalStory;
    private UserStory nonMinimalStoryWithNote;
    private UserStory nonMinimalStoryWithExtraInfo;

    @BeforeEach
    void setUp() {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH);
        minimalityAnalyzer = new MinimalityAnalyzer(messages);

        minimalStory = new UserStory();
        minimalStory.setPid("#MIN01#");
        minimalStory.setText("As a User, I want to login, so that I can access my account.");
        minimalStory.setPersona(List.of("User"));
        minimalStory.setActionGoal(List.of("login"));
        minimalStory.setEntityGoal(List.of("account"));
        minimalStory.setBenefit("I can access my account.");

        nonMinimalStoryWithNote = new UserStory();
        nonMinimalStoryWithNote.setPid("#MIN02#");
        nonMinimalStoryWithNote.setText("As a care professional, I want to see the registered hours of this week (split into products and activities). See: Mockup from Alice NOTE—first create the overview screen—then add validations.");
        nonMinimalStoryWithNote.setPersona(List.of("care professional"));
        nonMinimalStoryWithNote.setActionGoal(List.of("see"));
        nonMinimalStoryWithNote.setEntityGoal(List.of("registered hours of this week"));
        // The benefit is not explicitly stated in the example text, but the structure implies it might be part of the goal.
        // The key is the extra "See: Mockup... NOTE..."

        nonMinimalStoryWithExtraInfo = new UserStory();
        nonMinimalStoryWithExtraInfo.setPid("#MIN03#");
        nonMinimalStoryWithExtraInfo.setText("As a customer, I want to view product details, including price, description, and images, so I can make an informed purchase decision. This feature is high priority.");
        nonMinimalStoryWithExtraInfo.setPersona(List.of("customer"));
        nonMinimalStoryWithExtraInfo.setActionGoal(List.of("view"));
        nonMinimalStoryWithExtraInfo.setEntityGoal(List.of("product details"));
        nonMinimalStoryWithExtraInfo.setBenefit("I can make an informed purchase decision.");
        // "This feature is high priority." is extra information.
    }

    @Test
    void testAnalyze_noStories() {
        List<QualityProblem> problems = minimalityAnalyzer.analyze(List.of());
        assertTrue(problems.isEmpty(), "No problems should be found in an empty list.");
    }

    @Test
    void testAnalyze_minimalStory() {
        List<QualityProblem> problems = minimalityAnalyzer.analyze(List.of(minimalStory));
        assertTrue(problems.isEmpty(), "A minimal story should not have problems.");
    }

    @Test
    void testAnalyze_nonMinimalStory_withNote() {
        // Example from the document
        List<QualityProblem> problems = minimalityAnalyzer.analyze(List.of(nonMinimalStoryWithNote));
        assertEquals(1, problems.size(), "Should detect one minimality problem due to extra note.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notMinimal"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonMinimalStoryWithNote));
    }

    @Test
    void testAnalyze_nonMinimalStory_withExtraInfo() {
        List<QualityProblem> problems = minimalityAnalyzer.analyze(List.of(nonMinimalStoryWithExtraInfo));
        assertEquals(1, problems.size(), "Should detect one minimality problem due to extra priority info.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.notMinimal"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(nonMinimalStoryWithExtraInfo));
    }

    @Test
    void testAnalyze_mixedStories() {
        List<UserStory> stories = List.of(minimalStory, nonMinimalStoryWithNote, nonMinimalStoryWithExtraInfo);
        List<QualityProblem> problems = minimalityAnalyzer.analyze(stories);
        assertEquals(2, problems.size(), "Should detect two minimality problems.");

        boolean foundProblemForNote = false;
        boolean foundProblemForExtraInfo = false;

        for (QualityProblem p : problems) {
            if (p.getAffectedStories().contains(nonMinimalStoryWithNote)) {
                foundProblemForNote = true;
            }
            if (p.getAffectedStories().contains(nonMinimalStoryWithExtraInfo)) {
                foundProblemForExtraInfo = true;
            }
        }
        assertTrue(foundProblemForNote, "Minimality problem for story with note not found.");
        assertTrue(foundProblemForExtraInfo, "Minimality problem for story with extra info not found.");
    }

    @Test
    void testGetCriterionName() {
        assertEquals("minimality", minimalityAnalyzer.getCriterionName());
    }
}
