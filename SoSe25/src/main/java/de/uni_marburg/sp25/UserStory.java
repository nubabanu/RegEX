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

    @JsonProperty("Action")
    private Action action;

    @JsonProperty("Entity")
    private Entity entity;

    @JsonProperty("Benefit")
    private String benefit;

    @JsonProperty("Triggers")
    private List<List<String>> triggers;

    @JsonProperty("Targets")
    private List<List<String>> targets;

    @JsonProperty("Contains")
    private List<List<String>> contains;

    // Nested Action class
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Action {
        @JsonProperty("Goal")
        private List<String> goal;

        @JsonProperty("Benefit")
        private List<String> benefit;

        public List<String> getGoal() { return goal; }
        public void setGoal(List<String> goal) { this.goal = goal; }
        public List<String> getBenefit() { return benefit; }
        public void setBenefit(List<String> benefit) { this.benefit = benefit; }
    }

    // Nested Entity class
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entity {
        @JsonProperty("Goal Entity")
        private List<String> goalEntity;

        @JsonProperty("Benefit Entity")
        private List<String> benefitEntity;

        public List<String> getGoalEntity() { return goalEntity; }
        public void setGoalEntity(List<String> goalEntity) { this.goalEntity = goalEntity; }
        public List<String> getBenefitEntity() { return benefitEntity; }
        public void setBenefitEntity(List<String> benefitEntity) { this.benefitEntity = benefitEntity; }
    }

    public String getPid() { return pid; }
    public void setPid(String pid) { this.pid = pid; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<String> getPersona() { return persona; }
    public void setPersona(List<String> persona) { this.persona = persona; }
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public Entity getEntity() { return entity; }
    public void setEntity(Entity entity) { this.entity = entity; }
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
                "\"Action\": {\"Goal\": " + (action != null && action.getGoal() != null ? action.getGoal().toString() : "[]") +
                ", \"Benefit\": " + (action != null && action.getBenefit() != null ? action.getBenefit().toString() : "[]") + "}," +
                "\"Entity\": {\"Goal Entity\": " + (entity != null && entity.getGoalEntity() != null ? entity.getGoalEntity().toString() : "[]") +
                ", \"Benefit Entity\": " + (entity != null && entity.getBenefitEntity() != null ? entity.getBenefitEntity().toString() : "[]") + "}," +
                "\"Benefit\": \"" + benefit + "\"," +
                "\"Triggers\": " + (triggers != null ? triggers.toString() : "[]") + "," +
                "\"Targets\": " + (targets != null ? targets.toString() : "[]") + "," +
                "\"Contains\": " + (contains != null ? contains.toString() : "[]") +
                "}";
    }
}