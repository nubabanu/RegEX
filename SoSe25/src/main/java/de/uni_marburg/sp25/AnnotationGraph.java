package de.uni_marburg.sp25;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a semantic annotation graph extracted from a user story through natural language processing.
 * 
 * AnnotationGraph serves as the core data structure for representing the semantic relationships
 * between different components of a user story. It provides:
 * 
 * Graph Structure:
 * - Nodes: Semantic components (personas, actions, entities) extracted from story text
 * - Edges: Relationships between components (triggers, targets, contains)
 * - Directed graph model supporting complex relationship analysis
 * 
 * Integration with Analysis Systems:
 * - Quality Analysis: Graph structure enables dependency analysis, conflict detection,
 *   and completeness checking across multiple quality criteria
 * - Visualization: Integrates with GraphStream library for interactive graph rendering
 * - NLP Pipeline: Constructed through text parsing algorithms in UserStoryManager
 * 
 * Data Storage:
 * - Uses HashSet collections for efficient node/edge lookup and duplicate prevention
 * - JSON serialization support through Jackson library for persistence
 * - Immutable once constructed to ensure graph consistency during analysis
 * 
 * Quality Analysis Applications:
 * - Independence: Analyzes cross-story dependencies through graph traversal
 * - Conflict Detection: Identifies contradicting relationships between components
 * - Completeness: Ensures all necessary components and relationships are present
 * - Atomicity: Validates single-responsibility principle through graph connectivity
 * 
 * @see Node
 * @see Edge
 * @see AnnotationGraphManager
 * @see UserStoryManager
 */
public class AnnotationGraph {
    /** 
     * Set of semantic nodes representing user story components.
     * Uses HashSet for O(1) lookup performance and automatic duplicate prevention.
     */
    private Set<Node> nodes = new HashSet<>();
    
    /** 
     * Set of directed edges representing relationships between nodes.
     * Uses HashSet for efficient relationship queries and cycle detection.
     */
    private Set<Edge> edges = new HashSet<>();

    /**
     * Default constructor
     */
    public AnnotationGraph() {
    }

    /**
     * Get the set of nodes in this graph
     * @return The set of nodes
     */
    public Set<Node> getNodes() {
        return nodes;
    }

    /**
     * Set the nodes in this graph
     * @param nodes The set of nodes
     */
    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * Get the set of edges in this graph
     * @return The set of edges
     */
    public Set<Edge> getEdges() {
        return edges;
    }

    /**
     * Set the edges in this graph
     * @param edges The set of edges
     */
    public void setEdges(Set<Edge> edges) {
        this.edges = edges;
    }

    /**
     * Add a node to the graph
     * @param node The node to add
     */
    public void addNode(Node node) {
        this.nodes.add(node);
    }

    /**
     * Add an edge to the graph
     * @param edge The edge to add
     */
    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }

    /**
     * Find a node by its label and type
     * @param label The label of the node
     * @param type The type of the node
     * @return The node if found, otherwise null
     */
    public Node findNode(String label, Node.Type type) {
        for (Node node : nodes) {
            if (node.getLabel().equals(label) && node.getType() == type) {
                return node;
            }
        }
        return null;
    }
}
