package de.uni_marburg.sp25;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class UserStoryPojoTest {

    @Test
    void testUserStoryConstructorAndGetters() {
        String pid = "US001";
        String text = "As a User, I want to log in, so that I can access my account.";
        String role = "User";
        // The constructor UserStory(String pid, String text, int i, int j, int k, String role)
        // also initializes actionGoal and benefit based on the text.
        UserStory story = new UserStory(pid, text, 0, 0, 0, role);

        assertEquals(pid, story.getPid());
        assertEquals(text, story.getText());
        assertNotNull(story.getPersona());
        assertEquals(1, story.getPersona().size());
        assertEquals(role, story.getPersona().get(0));

        // Check initialization of actionGoal and benefit based on the constructor's logic
        // The constructor extracts the first part after "I want to " as actionGoal
        // and the part after "so that " as benefit.
        assertEquals(List.of("log in"), story.getActionGoal());
        assertEquals("access my account.", story.getBenefit());
    }

    @Test
    void testSetters() {
        // Use default constructor for Jackson, then set fields
        UserStory story = new UserStory();
        String pid = "US123";
        String text = "As an Admin, I want to manage users, so that I can control access.";
        List<String> persona = List.of("Admin");
        List<String> actionGoal = List.of("manage users");
        String benefit = "control access";
        List<String> entityGoal = List.of("users");
        List<String> actionBenefit = List.of("control action benefit");
        List<String> entityBenefit = List.of("users benefit");
        List<List<String>> triggers = List.of(List.of("trigger1"));
        List<List<String>> targets = List.of(List.of("target1"));
        List<List<String>> contains = List.of(List.of("item1"));

        story.setPid(pid);
        story.setText(text);
        story.setPersona(persona);
        story.setActionGoal(actionGoal);
        story.setBenefit(benefit);
        story.setEntityGoal(entityGoal);
        story.setActionBenefit(actionBenefit);
        story.setEntityBenefit(entityBenefit);
        story.setTriggers(triggers);
        story.setTargets(targets);
        story.setContains(contains);

        assertEquals(pid, story.getPid());
        assertEquals(text, story.getText());
        assertEquals(persona, story.getPersona());
        assertEquals(actionGoal, story.getActionGoal());
        assertEquals(benefit, story.getBenefit());
        assertEquals(entityGoal, story.getEntityGoal());
        assertEquals(actionBenefit, story.getActionBenefit());
        assertEquals(entityBenefit, story.getEntityBenefit());
        assertEquals(triggers, story.getTriggers());
        assertEquals(targets, story.getTargets());
        assertEquals(contains, story.getContains());
    }

    @Test
    void testToString() {
        UserStory story = new UserStory();
        story.setPid("PID001");
        story.setText("Test story text");
        story.setPersona(List.of("Tester"));
        story.setActionGoal(List.of("test action"));
        story.setBenefit("test benefit");
        story.setActionBenefit(List.of("test action benefit"));
        story.setEntityGoal(List.of("test entity goal"));
        story.setEntityBenefit(List.of("test entity benefit"));
        story.setTriggers(List.of(List.of("trigger")));
        story.setTargets(List.of(List.of("target")));
        story.setContains(List.of(List.of("contains")));

        String expectedToString = "{"
                + "\\\"PID\\\": \\\"PID001\\\","
                + "\\\"Text\\\": \\\"Test story text\\\","
                + "\\\"Persona\\\": [Tester],"
                + "\\\"Action.Goal\\\": [test action],"
                + "\\\"Action.Benefit\\\": [test action benefit],"
                + "\\\"Entity.Goal\\\": [test entity goal],"
                + "\\\"Entity.Benefit\\\": [test entity benefit],"
                + "\\\"Benefit\\\": \\\"test benefit\\\","
                + "\\\"Triggers\\\": [[trigger]],"
                + "\\\"Targets\\\": [[target]],"
                + "\\\"Contains\\\": [[contains]]"
                + "}";

        assertEquals(expectedToString, story.toString());
    }

    @Test
    void testFieldBasedEquality() { // UserStory overrides equals/hashCode based on all fields
        UserStory story1 = new UserStory("ID001", "Text", 0,0,0, "Role");
        UserStory story2 = new UserStory("ID001", "Text", 0,0,0, "Role");

        // Assertions based on field values that are set by the constructor
        assertEquals(story1.getPid(), story2.getPid());
        assertEquals(story1.getText(), story2.getText());
        assertEquals(story1.getPersona(), story2.getPersona());
        assertEquals(story1.getBenefit(), story2.getBenefit());
        assertEquals(story1.getActionGoal(), story2.getActionGoal());

        // UserStory overrides equals() to compare all field values, so instances with same data are equal
        assertEquals(story1, story2, "Instances with same field values should be equal.");
        assertEquals(story1.hashCode(), story2.hashCode(), "Instances with same field values should have same hashCode.");
    }
}
