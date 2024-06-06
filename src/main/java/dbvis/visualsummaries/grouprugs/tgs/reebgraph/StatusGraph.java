package dbvis.visualsummaries.grouprugs.tgs.reebgraph;

import java.util.List;
import java.util.Set;

import dbvis.visualsummaries.data.*;
import dbvis.visualsummaries.grouprugs.tgs.Utils;

import java.util.ArrayList;
import java.util.HashSet;

public class StatusGraph {
    private ArrayList<StatusGraph.Vertex> vertices;
    double epsilon;

    public StatusGraph(DataPoint[] firstFrame, double epsilon) {
        this.vertices = new ArrayList<>();
        this.epsilon = epsilon;

        // Call initialize function
        initialize(firstFrame, epsilon);
    }

    /**
     * Nested class representing a vertex (entity) in the graph.
     * 
     * @param id The id of the vertex.
     * @param x  The x-coordinate of the vertex.
     * @param y  The y-coordinate of the vertex.
     */
    public class Vertex {
        private int id;
        private double x;
        private double y;
        private int reebId;

        // Adjacency list of the vertex
        private List<Vertex> neighbors;

        public Vertex(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;

            this.reebId = -1;
            this.neighbors = new ArrayList<>();
        }

        public int getId() {
            return this.id;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public int getReebId() {
            return this.reebId;
        }

        public void setReebId(int reebId) {
            this.reebId = reebId;
        }

        public List<Vertex> getNeighbors() {
            return neighbors;
        }

    }

    /**
     * Get the vertex ids of a list of StatusGraph vertices.
     * 
     * @param vertices The list of vertices.
     * @return List of vertex ids.
     */
    public List<Integer> getVertexIds(List<StatusGraph.Vertex> vertices) {
        List<Integer> vertexIds = new ArrayList<>();
        for (StatusGraph.Vertex v : vertices) {
            vertexIds.add(v.getId());
        }
        return vertexIds;
    }

    /**
     * For each connected component in the first frame of the dataset, add a vertex
     * to the graph.
     */
    public void initialize(DataPoint[] firstFrame, double epsilon) {

        // Iterate through all entities in the first frame
        for (int entity = 0; entity < firstFrame.length; entity++) {

            // Get the x and y coordinates of the entity
            double x = firstFrame[entity].getX();
            double y = firstFrame[entity].getY();
            int id = firstFrame[entity].getId();

            // Create a new vertex with the entity id and coordinates
            StatusGraph.Vertex vertex = new StatusGraph.Vertex(id, x, y);

            // Add the vertex to the statusgraph
            addVertex(vertex);
        }

        // For each pair of vertices in the graph
        for (int i = 0; i < vertices.size(); i++) {
            StatusGraph.Vertex v1 = vertices.get(i);

            for (int j = i + 1; j < vertices.size(); j++) {
                StatusGraph.Vertex v2 = vertices.get(j);

                // If the distance between the vertices is less than epsilon
                if (Utils.getEuclideanDistance(v1, v2) <= epsilon) {
                    // Add an edge between the vertices
                    addEdge(v1, v2);
                }
            }
        }

    }

    public void addVertex(StatusGraph.Vertex vertex) {
        vertices.add(vertex);
    }

    /**
     * Add an edge between two vertices.
     * 
     * @param v1 The first vertex.
     * @param v2 The second vertex.
     */
    public void addEdge(StatusGraph.Vertex v1, StatusGraph.Vertex v2) {

        v1.getNeighbors().add(v2);
        v2.getNeighbors().add(v1);

    }

    /**
     * Remove an edge between two vertices.
     * 
     * @param v1 The first vertex.
     * @param v2 The second vertex.
     */
    public void removeEdge(StatusGraph.Vertex v1, StatusGraph.Vertex v2) {

        if (v1.getNeighbors().contains(v2)) {
            v1.getNeighbors().remove(v2);
            v2.getNeighbors().remove(v1);
        }

    }

    public StatusGraph.Vertex getVertex(int id) {
        for (StatusGraph.Vertex vertex : vertices) {
            if (vertex.getId() == id) {
                return vertex;
            }
        }

        return null;
    }

    /**
     * Function checks if a path exists between two vertices.
     * 
     * @param start The start vertex.
     * @param end   The end vertex.
     * @return true if a path exists between the two vertices, false otherwise.
     */
    public boolean pathExists(StatusGraph.Vertex start, StatusGraph.Vertex end) {
        Set<StatusGraph.Vertex> visited = new HashSet<>();

        return path(start, end, visited);
    }

    /**
     * Recursive function to check if a path exists between two vertices.
     * 
     * @param current The current vertex.
     * @param end     The end vertex.
     * @param visited Set of visited vertices.
     * @return true if a path exists between the two vertices, false otherwise.
     */
    private boolean path(StatusGraph.Vertex current, StatusGraph.Vertex end, Set<StatusGraph.Vertex> visited) {

        visited.add(current);

        if (current.equals(end)) {
            return true;
        }

        List<StatusGraph.Vertex> neighbors = current.getNeighbors();

        for (StatusGraph.Vertex neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                if (path(neighbor, end, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the connected component corresponding to a specific reeb node.
     */
    public List<StatusGraph.Vertex> getConnectedComponent(int reebID) {

        List<StatusGraph.Vertex> component = new ArrayList<>();

        for (StatusGraph.Vertex vertex : vertices) {
            if (vertex.getReebId() == reebID) {
                component.add(vertex);
            }
        }

        return component;

    }

    /**
     * Get connected components of the graph.
     *
     * @return List of connected components, where each component is represented by
     *         a list of vertices.
     */
    public List<List<StatusGraph.Vertex>> getConnectedComponents() {
        List<List<StatusGraph.Vertex>> connectedComponents = new ArrayList<>();
        Set<StatusGraph.Vertex> visited = new HashSet<>();

        for (StatusGraph.Vertex vertex : vertices) {
            if (!visited.contains(vertex)) {
                List<StatusGraph.Vertex> component = new ArrayList<>();
                exploreConnectedComponent(vertex, visited, component);
                connectedComponents.add(component);
            }
        }

        return connectedComponents;
    }

    /**
     * Explore a connected component of the graph.
     *
     * @param vertex    The vertex to start the exploration from.
     * @param visited   Set of visited vertices.
     * @param component List of vertices in the connected component.
     */
    private void exploreConnectedComponent(StatusGraph.Vertex vertex, Set<StatusGraph.Vertex> visited,
            List<StatusGraph.Vertex> component) {
        visited.add(vertex);
        component.add(vertex);

        List<StatusGraph.Vertex> neighbors = vertex.getNeighbors();

        for (StatusGraph.Vertex neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                exploreConnectedComponent(neighbor, visited, component);
            }
        }
    }

}
