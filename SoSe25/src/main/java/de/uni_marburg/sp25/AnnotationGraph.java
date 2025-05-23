package de.uni_marburg.sp25;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a graph of annotations extracted from a user story.
 */
public class AnnotationGraph {
    private Set<Node> nodes = new HashSet<>();
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
