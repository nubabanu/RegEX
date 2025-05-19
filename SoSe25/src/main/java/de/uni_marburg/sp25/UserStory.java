package de.uni_marburg.sp25;

import java.util.List;

public class UserStory {
    private String pid; // Unique identifier
    private String text; // Narrative description
    private List<String> persona; // List of user roles
    private List<String> actionGoal; // Primary intention or goal
    private List<String> actionBenefit; // Action or result benefit
    private List<String> entityGoal; // Information objects related to the goal
    private List<String> entityBenefit; // Additional information as benefit
    private String benefit; // Concrete benefit description
    private List<List<String>> triggers; // Relationships between persona and goal
    private List<List<String>> targets; // Relationships between action and target
    private List<List<String>> contains; // Hierarchical relationships of objects

    // Getters and setters for all fields
    public String getPid() { return pid; }
    public void setPid(String pid) { this.pid = pid; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<String> getPersona() { return persona; }
    public void setPersona(List<String> persona) { this.persona = persona; }
    public List<String> getActionGoal() { return actionGoal; }
    public void setActionGoal(List<String> actionGoal) { this.actionGoal = actionGoal; }
    public List<String> getActionBenefit() { return actionBenefit; }
    public void setActionBenefit(List<String> actionBenefit) { this.actionBenefit = actionBenefit; }
    public List<String> getEntityGoal() { return entityGoal; }
    public void setEntityGoal(List<String> entityGoal) { this.entityGoal = entityGoal; }
    public List<String> getEntityBenefit() { return entityBenefit; }
    public void setEntityBenefit(List<String> entityBenefit) { this.entityBenefit = entityBenefit; }
    public String getBenefit() { return benefit; }
    public void setBenefit(String benefit) { this.benefit = benefit; }
    public List<List<String>> getTriggers() { return triggers; }
    public void setTriggers(List<List<String>> triggers) { this.triggers = triggers; }
    public List<List<String>> getTargets() { return targets; }
    public void setTargets(List<List<String>> targets) { this.targets = targets; }
    public List<List<String>> getContains() { return contains; }
    public void setContains(List<List<String>> contains) { this.contains = contains; }

    @Override
    public String toString() {
        return "{" +
                "\"PID\": \"" + pid + "\"," +
                "\"Text\": \"" + text + "\"," +
                "\"Persona\": " + (persona != null ? persona.toString() : "[]") + "," +
                "\"Action.Goal\": " + (actionGoal != null ? actionGoal.toString() : "[]") + "," +
                "\"Action.Benefit\": " + (actionBenefit != null ? actionBenefit.toString() : "[]") + "," +
                "\"Entity.Goal\": " + (entityGoal != null ? entityGoal.toString() : "[]") + "," +
                "\"Entity.Benefit\": " + (entityBenefit != null ? entityBenefit.toString() : "[]") + "," +
                "\"Benefit\": \"" + benefit + "\"," +
                "\"Triggers\": " + (triggers != null ? triggers.toString() : "[]") + "," +
                "\"Targets\": " + (targets != null ? targets.toString() : "[]") + "," +
                "\"Contains\": " + (contains != null ? contains.toString() : "[]") +
                "}";
    }
}