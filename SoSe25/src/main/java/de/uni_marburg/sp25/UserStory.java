package de.uni_marburg.sp25;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown fields in JSON
public class UserStory {
    @JsonProperty("PID")
    private String pid;

    @JsonProperty("Text")
    private String text;

    @JsonProperty("Persona")
    private List<String> persona;

    // Changed to match the JSON structure described in the Anforderungsbeschreibung
    @JsonProperty("Action.Goal")
    private List<String> actionGoal;

    @JsonProperty("Action.Benefit")
    private List<String> actionBenefit;

    @JsonProperty("Entity.Goal")
    private List<String> entityGoal;

    @JsonProperty("Entity.Benefit")
    private List<String> entityBenefit;

    @JsonProperty("Benefit")
    private String benefit;

    @JsonProperty("Triggers")
    private List<List<String>> triggers;

    @JsonProperty("Targets")
    private List<List<String>> targets;

    @JsonProperty("Contains")
    private List<List<String>> contains;

    // Getters and setters for the new fields
    public List<String> getActionGoal() { return actionGoal; }
    public void setActionGoal(List<String> actionGoal) { this.actionGoal = actionGoal; }
    public List<String> getActionBenefit() { return actionBenefit; }
    public void setActionBenefit(List<String> actionBenefit) { this.actionBenefit = actionBenefit; }
    public List<String> getEntityGoal() { return entityGoal; }
    public void setEntityGoal(List<String> entityGoal) { this.entityGoal = entityGoal; }
    public List<String> getEntityBenefit() { return entityBenefit; }
    public void setEntityBenefit(List<String> entityBenefit) { this.entityBenefit = entityBenefit; }


    // Removed nested Action and Entity classes as the Anforderungsbeschreibung specifies a flat structure for these within the JSON.
    // The fields are now directly part of the UserStory class.

    public String getPid() { return pid; }
    public void setPid(String pid) { this.pid = pid; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<String> getPersona() { return persona; }
    public void setPersona(List<String> persona) { this.persona = persona; }
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
        // Updated toString to reflect the changes in fields and match the specified JSON output format.
        return "{" +
                "\\\"PID\\\": \\\"" + pid + "\\\"," +
                "\\\"Text\\\": \\\"" + text + "\\\"," +
                "\\\"Persona\\\": " + (persona != null ? persona.toString() : "[]") + "," +
                "\\\"Action.Goal\\\": " + (actionGoal != null ? actionGoal.toString() : "[]") + "," +
                "\\\"Action.Benefit\\\": " + (actionBenefit != null ? actionBenefit.toString() : "[]") + "," +
                "\\\"Entity.Goal\\\": " + (entityGoal != null ? entityGoal.toString() : "[]") + "," +
                "\\\"Entity.Benefit\\\": " + (entityBenefit != null ? entityBenefit.toString() : "[]") + "," +
                "\\\"Benefit\\\": \\\"" + benefit + "\\\"," +
                "\\\"Triggers\\\": " + (triggers != null ? triggers.toString() : "[]") + "," +
                "\\\"Targets\\\": " + (targets != null ? targets.toString() : "[]") + "," +
                "\\\"Contains\\\": " + (contains != null ? contains.toString() : "[]") +
                "}";
    }
}