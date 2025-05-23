package de.uni_marburg.sp25;

import java.util.Objects;

/**
 * Represents a node in an annotation graph.
 */
public class Node {
    /**
     * The type of node in the annotation graph
     */
    public enum Type {
        ROLE,
        GOAL_ACTION,
        GOAL_ENTITY,
        BENEFIT_ACTION,
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
