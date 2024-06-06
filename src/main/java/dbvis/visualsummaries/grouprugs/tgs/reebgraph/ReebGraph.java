package dbvis.visualsummaries.grouprugs.tgs.reebgraph;

// General imports
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * The Reeb Graph class.
 */
public class ReebGraph {

    private ArrayList<RVertex> vertices;
    private ArrayList<REdge> edges;

    /**
     * Constructor for the ReebGraph class.
     * 
     * @param statusGraph The status graph with the state of the dataset at initial
     *                    frame in data.
     */
    public ReebGraph(StatusGraph statusGraph) {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();

        initialize(statusGraph);
    }

    /**
     * Constructor for reading a ReebGraph from a file.
     */
    public ReebGraph() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();

    }

    /**
     * Adds a vertex to the graph.
     * 
     * @param id   The id of the vertex.
     * @param time The time of the vertex.
     * @param type The type of the vertex.
     */
    public void addVertex(int id, double time, String type) {
        RVertex v = new RVertex(id, time, type);
        vertices.add(v);
    }

    /**
     * Returns the vertex with the given id.
     * 
     * @param id The id of the vertex.
     * @return The vertex with the given id.
     */
    public RVertex getVertex(int id) {
        for (RVertex v : vertices) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }

    /**
     * Returns the vertices of the graph.
     * 
     * @return The vertices of the graph.
     */
    public ArrayList<RVertex> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<RVertex> vertices) {
        this.vertices = vertices;
    }

    /**
     * Returns the start vertices of the graph.
     * 
     * @return The start vertices of the graph.
     */
    public List<RVertex> getStartVertices() {
        List<RVertex> startVertices = new ArrayList<>();
        for (RVertex v : vertices) {
            if (v.getType().equals(RVertex.START_VERTEX)) {
                startVertices.add(v);
            }
        }
        return startVertices;
    }

    /**
     * Adds an edge to the graph.
     * 
     * @param id1    The id of the first vertex.
     * @param id2    The id of the second vertex.
     * @param reebId The id of the reeb node.
     */
    public void addEdge(int id1, int id2, int reebId, List<Integer> component) {
        RVertex v1 = getVertex(id1);
        RVertex v2 = getVertex(id2);

        REdge e = new REdge(v1, v2, reebId, component);
        edges.add(e);
    }

    /**
     * Adds an edge to the graph.
     * 
     * @param e The edge to add.
     */
    public void addEdge(REdge e) {
        edges.add(e);
    }

    /**
     * Collapse edge e.
     * 
     * @param e
     */
    public void collapseEdge(REdge e) {

        RVertex src = e.getSource();
        RVertex dest = e.getDest();

        // Add outedges to src
        List<REdge> outEdges = new ArrayList<>(dest.getOutEdges());

        for (REdge e2 : outEdges) {
            REdge newEdge = new REdge(src, e2.getDest(), -1, e2.getComponent());
            addEdge(newEdge);
        }

        // Add inedges to dest, skipping the edge to src
        List<REdge> inEdges = new ArrayList<>(dest.getInEdges());

        for (REdge e2 : inEdges) {
            if (e2.getSource() == src) {
                continue;
            }

            REdge newEdge = new REdge(e2.getSource(), src, -1, e2.getComponent());
            addEdge(newEdge);
        }

        // Remove all edges from dest
        for (REdge e2 : outEdges) {
            removeEdge(e2);
        }

        for (REdge e2 : inEdges) {
            removeEdge(e2);
        }

        // Collapse edge e
        removeEdge(e);
        removeVertex(dest);

    }

    /**
     * Removes a vertex from the graph.
     * 
     * @param v The vertex to remove.
     */
    public void removeVertex(RVertex v) {
        vertices.remove(v);
    }

    /**
     * Removes an edge from the graph.
     * 
     * @param e The edge to remove.
     */
    public void removeEdge(REdge e) {

        e.getSource().getOutEdges().remove(e);
        e.getDest().getInEdges().remove(e);

        edges.remove(e);
    }

    /**
     * Returns the edges of the graph.
     * 
     * @return The edges of the graph.
     */
    public ArrayList<REdge> getEdges() {
        return this.edges;
    }

    /**
     * Sets the edges of the graph.
     * 
     * @param edges The edges of the graph.
     */
    public void setEdges(ArrayList<REdge> edges) {
        this.edges = edges;
    }

    public int getMaxTimestamp() {
        int max = 0;
        for (RVertex v : vertices) {
            if (v.getTime() > max) {
                max = (int) v.getTime();
            }
        }
        return max;
    }

    /**
     * Returns the edges of the graph with the given reebId.
     * 
     * FIXME: Zit nog een foutje in: newFlockModel, epsilon 16, simplePositioning
     * met CubicInterpolation.
     * 
     * Source: https://www.geeksforgeeks.org/topological-sorting/
     * 
     * @param reebId  The reebId of the vertex.
     * @param visited An array of booleans indicating whether a vertex has been
     *                visited.
     * @param stack   The stack of vertices.
     */
    private void topologicalSortUtil(int reebId, boolean visited[], Stack<RVertex> stack) {

        visited[reebId] = true;

        RVertex v = getVertex(reebId);

        for (REdge e : v.getOutEdges()) {
            int destId = e.getDest().getId();
            if (!visited[destId]) {
                topologicalSortUtil(destId, visited, stack);
            }
        }

        stack.push(getVertex(reebId));
    }

    /**
     * Function returns a list of the vertices in topological order.
     * 
     * Source: https://www.geeksforgeeks.org/topological-sorting/
     * 
     * @return A list of the vertices in topological order.
     * 
     *         TODO: Verify if this function is necessary at all. It seems that the
     *         Reeb graph is automatically topologically sorted.
     */
    public List<RVertex> topologicalSort() {

        Stack<RVertex> stack = new Stack<RVertex>();

        boolean visited[] = new boolean[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            visited[i] = false;
        }

        for (int i = 0; i < vertices.size(); i++) {
            if (visited[i] == false) {
                topologicalSortUtil(i, visited, stack);
            }
        }

        List<RVertex> topologicalOrder = new ArrayList<>();
        while (stack.empty() == false) {
            topologicalOrder.add(stack.pop());
        }

        return topologicalOrder;
    }

    /**
     * Initializes the reeb graph with one vertex for each connected component in
     * the status graph.
     * 
     * TODO: Option; pick a representative vertex for each connected component.
     * ReebId = representative vertex. Of: Mapping M (wordt in paper ook gebruikt).
     * 
     * @param statusGraph The status graph.
     */
    public void initialize(StatusGraph statusGraph) {

        int reebId = 0;
        int vertexId = 0;
        int frame = 0;

        // For each connected component in the status graph
        for (List<StatusGraph.Vertex> component : statusGraph.getConnectedComponents()) {

            // Create a new vertex in the reeb graph
            RVertex vertex = new RVertex(vertexId, frame, RVertex.START_VERTEX);
            vertices.add(vertex);

            // Make a list of ids of the vertices in the connected component
            List<Integer> vertexIds = new ArrayList<>();
            for (StatusGraph.Vertex v : component) {
                vertexIds.add(v.getId());
            }

            // Create new edge between vertex and null
            REdge edge = new REdge(vertex, null, reebId, vertexIds);
            edges.add(edge);

            // Set reebId of each vertex in connected component to reebId of reeb node.
            for (StatusGraph.Vertex v : component) {
                v.setReebId(reebId);
            }

            // Set reebId to next value
            reebId++;
            vertexId++;

        }
    }
}