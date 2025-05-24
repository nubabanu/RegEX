package de.uni_marburg.sp25;

import java.util.Objects;

/**
 * Represents a semantic node in an annotation graph containing user story components.
 * 
 * Node objects model the individual components extracted from user story text through
 * natural language processing. Each node represents a distinct semantic element with
 * a specific role in the user story structure:
 * 
 * Node Types and Their Meanings:
 * - ROLE: Represents the persona or user type (e.g., "customer", "administrator")
 * - GOAL_ACTION: Actions that users want to perform (e.g., "login", "purchase")
 * - GOAL_ENTITY: Objects or data that actions target (e.g., "product", "account")
 * - BENEFIT_ACTION: Actions that provide value (e.g., "save time", "increase efficiency")
 * - BENEFIT_ENTITY: Entities that deliver benefits (e.g., "report", "dashboard")
 * 
 * Graph Visualization:
 * Nodes are rendered using the GraphStream library with type-specific styling:
 * - ROLE nodes appear in gold color to highlight personas
 * - GOAL nodes use green colors to represent primary functionality
 * - BENEFIT nodes use orange/salmon colors to emphasize value delivery
 * 
 * Natural Language Processing:
 * Nodes are created through text analysis algorithms that identify and classify
 * semantic components using pattern matching, stop word filtering, and entity extraction.
 * The classification supports quality analysis and relationship discovery.
 * 
 * @see Edge
 * @see AnnotationGraph
 * @see UserStoryManager
 */
public class Node {
    /**
     * Enumeration defining semantic categories for user story components.
     * 
     * These types align with user story template patterns (As a X, I want Y, So that Z)
     * and support both visualization styling and quality analysis algorithms.
     */
    public enum Type {
        /** User personas or roles (the "As a X" component) */
        ROLE,
        /** Actions in the goal clause (verbs in "I want to Y") */
        GOAL_ACTION,
        /** Entities in the goal clause (objects in "I want to Y") */
        GOAL_ENTITY,
        /** Actions in the benefit clause (verbs in "So that Z") */
        BENEFIT_ACTION,
        /** Entities in the benefit clause (objects in "So that Z") */
        BENEFIT_ENTITY
    }

    private String label;
    private Type type;

    /**
     * Default constructor
     */
    public Node() {
    }

    /**
     * Constructor with label and type
     * @param label The label of the node
     * @param type The type of the node
     */
    public Node(String label, Type type) {
        this.label = label;
        this.type = type;
    }

    /**
     * Get the label of the node
     * @return The label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the label of the node
     * @param label The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the type of the node
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Set the type of the node
     * @param type The type
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(label, node.label) && type == node.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, type);
    }

    @Override
    public String toString() {
        return "Node{" +
                "label='" + label + '\'' +
                ", type=" + type +
                '}';
    }
}
