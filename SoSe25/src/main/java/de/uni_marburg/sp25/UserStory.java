package de.uni_marburg.sp25;

import java.util.List;

public class UserStory {
    private String pid; // Unique identifier
    private String role;
    private String action;
    private String entity;
    private List<String> triggers;
    private List<String> targets;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<String> triggers) {
        this.triggers = triggers;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    @Override
    public String toString() {
        return "{" +
                "\"PID\": \"" + pid + "\"," +
                "\"Role\": \"" + role + "\"," +
                "\"Action\": \"" + action + "\"," +
                "\"Entity\": \"" + entity + "\"," +
                "\"Triggers\": " + (triggers != null ? triggers.toString() : "[]") + "," +
                "\"Targets\": " + (targets != null ? targets.toString() : "[]") +
                "}";
    }
}