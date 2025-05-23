package de.uni_marburg.sp25;

import java.util.Objects;

/**
 * Represents an edge in an annotation graph.
 */
public class Edge {
    /**
     * The type of edge in the annotation graph
     */
    public enum Type {
        TRIGGER,
        TARGET,
        CONTAINS
    }

    private Node source;
    private Node target;
    private Type type;

    /**
     * Default constructor
     */
    public Edge() {
    }

    /**
     * Constructor with source, target, and type
     * @param source The source node
     * @param target The target node
     * @param type The type of the edge
     */
    public Edge(Node source, Node target, Type type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    /**
     * Get the source node
     * @return The source node
     */
    public Node getSource() {
        return source;
    }

    /**
     * Set the source node
     * @param source The source node
     */
    public void setSource(Node source) {
        this.source = source;
    }

    /**
     * Get the target node
     * @return The target node
     */
    public Node getTarget() {
        return target;
    }

    /**
     * Set the target node
     * @param target The target node
     */
    public void setTarget(Node target) {
        this.target = target;
    }

    /**
     * Get the type of the edge
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Set the type of the edge
     * @param type The type
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(source, edge.source) &&
               Objects.equals(target, edge.target) &&
               type == edge.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, type);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source +
                ", target=" + target +
                ", type=" + type +
                '}';
    }
}
