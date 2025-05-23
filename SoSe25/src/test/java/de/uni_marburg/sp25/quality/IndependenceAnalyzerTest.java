package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class IndependenceAnalyzerTest {

    private IndependenceAnalyzer independenceAnalyzer;
    private ResourceBundle messages;
    private UserStory storyCreateAccount;
    private UserStory storyChangePermissions;
    private UserStory storyEditContent;
    private UserStory storyEditName;
    private UserStory storyIndependent1;
    private UserStory storyIndependent2;

    @BeforeEach
    void setUp() {
        messages = ResourceBundle.getBundle("de.uni_marburg.sp25.messages", Locale.ENGLISH);
        independenceAnalyzer = new IndependenceAnalyzer(messages);

        storyCreateAccount = new UserStory();
        storyCreateAccount.setPid("#DEP01A#");
        storyCreateAccount.setText("As an Administrator, I want to create a new user account.");
        storyCreateAccount.setPersona(List.of("Administrator"));
        storyCreateAccount.setActionGoal(List.of("create"));
        storyCreateAccount.setEntityGoal(List.of("new user account"));

        storyChangePermissions = new UserStory();
        storyChangePermissions.setPid("#DEP01B#");
        storyChangePermissions.setText("As an Administrator, I want to change the permission rights of a user account.");
        storyChangePermissions.setPersona(List.of("Administrator"));
        storyChangePermissions.setActionGoal(List.of("change"));
        storyChangePermissions.setEntityGoal(List.of("permission rights of a user account"));
        // This story depends on an account existing.

        storyEditContent = new UserStory();
        storyEditContent.setPid("#DEP02A#");
        storyEditContent.setText("As a App-User, I want to be able to edit the content that I added to a person's profile page.");
        storyEditContent.setPersona(List.of("App-User"));
        storyEditContent.setActionGoal(List.of("edit"));
        storyEditContent.setEntityGoal(List.of("content that I added to a person's profile page"));


        storyEditName = new UserStory();
        storyEditName.setPid("#DEP02B#");
        storyEditName.setText("As a App-User, I want to be able to edit the name that I added to a person's profile page.");
        storyEditName.setPersona(List.of("App-User"));
        storyEditName.setActionGoal(List.of("edit"));
        storyEditName.setEntityGoal(List.of("name that I added to a person's profile page"));
        // Editing name (specific) could be seen as dependent if "content" (general) is also being edited,
        // or if "edit content" is a broader task that includes editing a name.
        // The document states: "Die erste User Story ist von der zweiten abhängig, da das Editieren von Inhalt (Content)
        // das Editieren des Namen umfasst." This implies US1 (edit content) depends on US2 (edit name), which is unusual.
        // Usually, a more specific action (edit name) might be part of a general one (edit content), or a general one
        // might need to be done first.
        // Let's re-read: "Die erste User Story ist von der zweiten abhängig, da das Editieren von Inhalt (Content) das Editieren des Namen umfasst."
        // This means US(edit content) is dependent on US(edit name). This is likely a typo in the document and should be the other way around,
        // or it means that "edit content" cannot be realized if "edit name" (as a part of content) is not realized.
        // For the test, I will assume the example implies a dependency exists.

        storyIndependent1 = new UserStory();
        storyIndependent1.setPid("#IND01#");
        storyIndependent1.setText("As a User, I want to see my dashboard.");
        storyIndependent1.setActionGoal(List.of("see"));
        storyIndependent1.setEntityGoal(List.of("my dashboard"));

        storyIndependent2 = new UserStory();
        storyIndependent2.setPid("#IND02#");
        storyIndependent2.setText("As a User, I want to logout from the system.");
        storyIndependent2.setActionGoal(List.of("logout"));
        storyIndependent2.setEntityGoal(List.of("system"));
    }

    @Test
    void testAnalyze_noStories() {
        List<QualityProblem> problems = independenceAnalyzer.analyze(new ArrayList<>());
        assertTrue(problems.isEmpty(), "No problems should be found in an empty list.");
    }

    @Test
    void testAnalyze_oneStory() {
        List<QualityProblem> problems = independenceAnalyzer.analyze(List.of(storyCreateAccount));
        assertTrue(problems.isEmpty(), "No dependency problems with only one story.");
    }

    @Test
    void testAnalyze_independentStories() {
        List<UserStory> stories = List.of(storyIndependent1, storyIndependent2, storyCreateAccount);
        List<QualityProblem> problems = independenceAnalyzer.analyze(stories);
        assertTrue(problems.isEmpty(), "No dependency problems should be found for these independent stories.");
    }

    @Test
    void testAnalyze_causalDependency() {
        // US(change permissions) depends on US(create account)
        List<UserStory> stories = List.of(storyCreateAccount, storyChangePermissions);
        List<QualityProblem> problems = independenceAnalyzer.analyze(stories);
        // Assuming the analyzer can detect this.
        // The problem description should indicate which story depends on which.
        // For now, we just check if a problem is found.
        assertEquals(1, problems.size(), "Should detect one causal dependency problem.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.dependent"), problem.getProblemDescription());
        assertTrue(problem.getAffectedStories().contains(storyChangePermissions) && problem.getAffectedStories().size() >= 1);
        // Ideally, the problem would list the pair, e.g. storyChangePermissions depends on storyCreateAccount
    }
    
    @Test
    void testAnalyze_entityInclusionDependency() {
        // "As a App-User, I want to be able to edit the content that I added to a person’s profile page.” (US1)
        // "As a App-User, I want to be able to edit the name that I added to a person’s profile page.” (US2)
        // Document: "Die erste User Story ist von der zweiten abhängig, da das Editieren von Inhalt (Content) das Editieren des Namen umfasst."
        // This means storyEditContent depends on storyEditName.
        List<UserStory> stories = List.of(storyEditContent, storyEditName);
        List<QualityProblem> problems = independenceAnalyzer.analyze(stories);
        // This is a complex type of dependency to detect.
        // For now, let's assume if the analyzer is built for this, it finds one.
        assertEquals(1, problems.size(), "Should detect one entity inclusion dependency problem based on example.");
        QualityProblem problem = problems.get(0);
        assertEquals(messages.getString("quality.problem.dependent"), problem.getProblemDescription());
        // Based on the doc, storyEditContent is dependent.
        assertTrue(problem.getAffectedStories().contains(storyEditContent) && problem.getAffectedStories().size() >= 1);
    }


    @Test
    void testGetCriterionName() {
        assertEquals("independence", independenceAnalyzer.getCriterionName());
    }
}
