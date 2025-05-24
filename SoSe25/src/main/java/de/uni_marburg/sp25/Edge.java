package de.uni_marburg.sp25;

import java.util.Objects;

/**
 * Represents a directed relationship (edge) between two nodes in an annotation graph.
 * 
 * Edge objects model the semantic relationships between different components of a user story,
 * such as how personas trigger actions, how actions target entities, or how entities contain
 * other entities. These relationships are essential for:
 * 
 * - Graph visualization using GraphStream library
 * - Quality analysis algorithms that examine story dependencies
 * - Semantic understanding of user story structure
 * - Natural language processing of story relationships
 * 
 * Edge Types:
 * - TRIGGER: Indicates that one component initiates another (e.g., persona -> action)
 * - TARGET: Shows that an action is directed toward an entity (e.g., action -> entity)
 * - CONTAINS: Represents hierarchical containment (e.g., entity -> sub-entity)
 * 
 * Graph Integration:
 * This class integrates with AnnotationGraphManager for graph construction and with
 * GraphStream for interactive visualization. The relationship data supports both
 * static analysis and dynamic graph rendering.
 * 
 * @see AnnotationGraph
 * @see Node
 * @see AnnotationGraphManager
 */
public class Edge {
    /**
     * Enumeration defining the types of semantic relationships between nodes.
     * 
     * These types correspond to different syntactic and semantic patterns
     * found in user story text analysis and natural language processing.
     */
    public enum Type {
        /** Indicates that the source node initiates or triggers the target node */
        TRIGGER,
        /** Shows that the source node is directed toward or acts upon the target node */
        TARGET,
        /** Represents that the source node hierarchically contains the target node */
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
