package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class CompletenessAnalyzerTest {

    private CompletenessAnalyzer completenessAnalyzer;
    private ResourceBundle messages;
    private UserStory storyCreate, storyEdit, storyDelete, storyView;

    @BeforeEach
    void setUp() {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH);
        completenessAnalyzer = new CompletenessAnalyzer(messages);

        storyCreate = new UserStory();
        storyCreate.setPid("#C01#");
        storyCreate.setText("As a User, I want to create a profile page.");
        storyCreate.setActionGoal(List.of("create"));
        storyCreate.setEntityGoal(List.of("profile page"));

        storyEdit = new UserStory();
        storyEdit.setPid("#C02#");
        storyEdit.setText("As a User, I want to be able to edit the content of a person's profile page that I created.");
        storyEdit.setActionGoal(List.of("edit"));
        storyEdit.setEntityGoal(List.of("content of a person's profile page")); // More specific entity

        storyDelete = new UserStory();
        storyDelete.setPid("#C03#");
        storyDelete.setText("As a User, I want to delete my profile page.");
        storyDelete.setActionGoal(List.of("delete"));
        storyDelete.setEntityGoal(List.of("my profile page"));

        storyView = new UserStory();
        storyView.setPid("#C04#");
        storyView.setText("As a User, I want to view my profile page.");
        storyView.setActionGoal(List.of("view"));
        storyView.setEntityGoal(List.of("my profile page"));
    }

    @Test
    void testAnalyze_noStories() {
        List<QualityProblem> problems = completenessAnalyzer.analyze(List.of());
        assertTrue(problems.isEmpty(), "No problems should be found in an empty list.");
    }

    @Test
    void testAnalyze_completeSetCRUD() {
        // Create, Read (View), Update (Edit), Delete for "profile page"
        List<UserStory> stories = List.of(storyCreate, storyView, storyEdit, storyDelete);
        List<QualityProblem> problems = completenessAnalyzer.analyze(stories);
        assertTrue(problems.isEmpty(), "A complete CRUD set should not yield completeness problems.");
    }

    @Test
    void testAnalyze_missingCreateStory() {
        // Example from document: "Wenn diese User Story die einzige in der Menge ist, die sich mit Profilseiten von
        // Personen beschäftigt, dann fehlt eine User Story, die Profilseiten hinzufügen kann."
        // Here, storyEdit implies a profile page exists, but no story creates it.
        List<UserStory> stories = List.of(storyEdit);
        List<QualityProblem> problems = completenessAnalyzer.analyze(stories);
        assertEquals(1, problems.size(), "Should detect a missing create story for \'profile page\'.");
        QualityProblem problem = problems.get(0);
        String expectedDescription = messages.getString("quality.problem.incomplete") + ": Missing \'create\' action for entity: \'profile page\'";
        assertEquals(expectedDescription, problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(storyEdit));
    }

    @Test
    void testAnalyze_missingCreateForDifferentEntity() {
        UserStory storyManageSettings = new UserStory();
        storyManageSettings.setPid("#C05#");
        storyManageSettings.setText("As an Admin, I want to manage user settings.");
        storyManageSettings.setActionGoal(List.of("manage")); // manage could imply edit/view
        storyManageSettings.setEntityGoal(List.of("user settings"));

        List<UserStory> stories = List.of(storyManageSettings, storyCreate); // storyCreate is for "profile page"
        List<QualityProblem> problems = completenessAnalyzer.analyze(stories);
        assertEquals(1, problems.size(), "Should detect a missing create story for \'user settings\'.");
        QualityProblem problem = problems.get(0);
        String expectedDescription = messages.getString("quality.problem.incomplete") + ": Missing \'create\' action for entity: \'user settings\'";
        assertEquals(expectedDescription, problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(storyManageSettings));
    }

    @Test
    void testAnalyze_onlyCreateStory() {
        List<UserStory> stories = List.of(storyCreate);
        List<QualityProblem> problems = completenessAnalyzer.analyze(stories);
        // Having only a create story is not necessarily a completeness problem by itself,
        // unless other criteria define it so (e.g., a system must allow viewing what is created).
        // Based on the provided definition focusing on missing create for existing edit/delete/read,
        // this should be fine.
        assertTrue(problems.isEmpty(), "Only a create story should not be a problem by this criterion's definition.");
    }

    @Test
    void testAnalyze_editAndViewWithoutCreate() {
        List<UserStory> stories = List.of(storyEdit, storyView);
        List<QualityProblem> problems = completenessAnalyzer.analyze(stories);
        assertEquals(1, problems.size(), "Should detect one missing create story problem when multiple stories depend on it.");
        QualityProblem problem = problems.get(0);
        String expectedDescription = messages.getString("quality.problem.incomplete") + ": Missing \'create\' action for entity: \'profile page\'";
        assertEquals(expectedDescription, problem.getProblemDescription());
        // Both stories are affected by the missing create story for "profile page" (or similar entity)
        assertTrue(problem.getAffectedStories().contains(storyEdit));
        assertTrue(problem.getAffectedStories().contains(storyView));
        assertEquals(2, problem.getAffectedStories().size());
    }

    @Test
    void testGetCriterionName() {
        assertEquals("completeness", completenessAnalyzer.getCriterionName());
    }
}
