package dbvis.visualsummaries.grouprugs.tgs.reebgraph;

import java.util.List;
import java.util.PriorityQueue;

import dbvis.visualsummaries.grouprugs.events.Event;

public class ReebGraphBuilder {

    /**
     * Reeb graph builder. This function builds the Reeb graph from the status
     * graph at frame 0. It updates the status graph and Reeb graph according to the
     * events.
     * 
     * @param statusGraph The status graph.
     * @param reebGraph   The Reeb graph.
     * @param events      The list of events.
     */
    public static ReebGraph build(StatusGraph statusGraph, PriorityQueue<Event> events,
            int frameNum) {

        ReebGraph reebGraph = new ReebGraph(statusGraph);

        for (Event event : events) {
            processEvent(statusGraph, reebGraph, event);
        }

        processFinalEdges(reebGraph, frameNum);

        return reebGraph;

    }

    /**
     * Function to process an event.
     * 
     * @param statusGraph
     * @param reebGraph
     * @param event
     */
    private static void processEvent(StatusGraph statusGraph, ReebGraph reebGraph, Event event) {

        StatusGraph.Vertex v1 = statusGraph.getVertex(event.getEntity1());
        StatusGraph.Vertex v2 = statusGraph.getVertex(event.getEntity2());

        if (event.getType() == Event.Type.CONNECT) {
            handleConnectEvent(statusGraph, reebGraph, event, v1, v2);

        } else if (event.getType() == Event.Type.DISCONNECT) {
            handleDisconnectEvent(statusGraph, reebGraph, event, v1, v2);
        }
    }

    /**
     * Function to handle a connect event. If v1 and v2 are in the same connected
     * component the Reeb graph is not updated. If v1 and v2 are in different
     * connected components, a new Reeb node is created and the Reeb graph is
     * updated accordingly.
     * 
     * @param statusGraph The status graph.
     * @param reebGraph   The Reeb graph.
     * @param event       The event.
     * @param v1          The first vertex.
     * @param v2          The second vertex.
     */
    private static void handleConnectEvent(
            StatusGraph statusGraph,
            ReebGraph reebGraph,
            Event event,
            StatusGraph.Vertex v1,
            StatusGraph.Vertex v2) {

        Boolean sameComponent = statusGraph.pathExists(v1, v2);

        if (sameComponent) {

            handleRegularConnect(statusGraph, v1, v2);

        } else {
            handleMergeEvent(statusGraph, reebGraph, event, v1, v2);
        }
    }

    /**
     * Function to handle a regular connect event. No Reeb node is created, just add
     * an edge to the status graph between v1 and v2.
     * 
     * @param statusGraph
     * @param v1
     * @param v2
     */
    private static void handleRegularConnect(StatusGraph statusGraph, StatusGraph.Vertex v1, StatusGraph.Vertex v2) {
        statusGraph.addEdge(v1, v2);
    }

    /** */
    private static void handleMergeEvent(
            StatusGraph statusGraph,
            ReebGraph reebGraph,
            Event event,
            StatusGraph.Vertex v1,
            StatusGraph.Vertex v2) {

        int prevReebEdgeId1 = statusGraph.getVertex(event.getEntity1()).getReebId();
        int prevReebEdgeId2 = statusGraph.getVertex(event.getEntity2()).getReebId();

        int currentReebVertexId = reebGraph.getVertices().size();
        int currentReebEdgeId = reebGraph.getEdges().size();
        double time = event.getTime();
        reebGraph.addVertex(currentReebVertexId, time, RVertex.MERGE_VERTEX);

        // Add edge between v1 and v2 (becomes 1 connected component)
        statusGraph.addEdge(v1, v2);

        // Update Reeb ID for all vertices in the connected component
        List<StatusGraph.Vertex> component1 = statusGraph.getConnectedComponent(v1.getReebId());
        for (StatusGraph.Vertex v : component1) {
            v.setReebId(currentReebEdgeId);
        }

        List<StatusGraph.Vertex> component2 = statusGraph.getConnectedComponent(v2.getReebId());
        for (StatusGraph.Vertex v : component2) {
            v.setReebId(currentReebEdgeId);
        }

        // new_component = component1 + component2
        List<StatusGraph.Vertex> newComp = component1;
        newComp.addAll(component2);

        // Convert to vertex ids
        List<Integer> newCompInt = statusGraph.getVertexIds(newComp);

        // Find Reeb edge with reebId = prevReebId1
        for (REdge e : reebGraph.getEdges()) {
            if (e.getReebId() == prevReebEdgeId1) {

                // Set this edge to have v2 = currentReebVertexId
                e.setDestAddEdge(reebGraph.getVertex(currentReebVertexId));
                break;
            }
        }

        // Find Reeb edge with reebId = prevReebId2
        for (REdge e : reebGraph.getEdges()) {
            if (e.getReebId() == prevReebEdgeId2) {

                // Set this edge to have v2 = currentReebVertexId
                e.setDestAddEdge(reebGraph.getVertex(currentReebVertexId));
                break;
            }
        }

        // Create new edge between newReebId and null
        reebGraph.addEdge(currentReebVertexId, -1, currentReebEdgeId, newCompInt);
    }

    /**
     * Function to handle a disconnect event. If v1 and v2 are no longer in the same
     * connected component after the disconnect event, then a new Reeb node is
     * created and the Reeb graph is updated.
     * 
     * @param statusGraph The status graph.
     * @param reebGraph   The Reeb graph.
     * @param event       The event.
     * @param v1          The first vertex.
     * @param v2          The second vertex.
     */
    private static void handleDisconnectEvent(
            StatusGraph statusGraph,
            ReebGraph reebGraph,
            Event event,
            StatusGraph.Vertex v1,
            StatusGraph.Vertex v2) {

        int prevVertexReebId = statusGraph.getVertex(event.getEntity1()).getReebId();
        statusGraph.removeEdge(v1, v2);
        Boolean sameComponentAfter = statusGraph.pathExists(v1, v2);

        if (!sameComponentAfter) {
            handleSplitEvent(statusGraph, reebGraph, event, v1, v2, prevVertexReebId);
        }
    }

    private static void handleSplitEvent(
            StatusGraph statusGraph,
            ReebGraph reebGraph,
            Event event,
            StatusGraph.Vertex v1,
            StatusGraph.Vertex v2,
            int prevVertexReebId) {

        // Create 1 new Reeb node
        int currentReebVertexId = reebGraph.getVertices().size();

        // Create 2 new Reeb edges
        int currentReebEdgeId1 = reebGraph.getEdges().size();
        int currentReebEdgeId2 = reebGraph.getEdges().size() + 1;

        double time = event.getTime();

        reebGraph.addVertex(currentReebVertexId, time, RVertex.SPLIT_VERTEX);

        // Find the connected components that contain v1 and v2
        List<List<StatusGraph.Vertex>> components = statusGraph.getConnectedComponents();

        // Find the connected component that contains v1
        List<StatusGraph.Vertex> component1 = null;
        for (List<StatusGraph.Vertex> component : components) {
            if (component.contains(v1)) {
                component1 = component;
                break;
            }
        }

        // Find the connected component that contains v2
        List<StatusGraph.Vertex> component2 = null;
        for (List<StatusGraph.Vertex> component : components) {
            if (component.contains(v2)) {
                component2 = component;
                break;
            }
        }

        // Update Reeb ID for new connected components
        for (StatusGraph.Vertex v : component1) {
            v.setReebId(currentReebEdgeId1);
        }
        for (StatusGraph.Vertex v : component2) {
            v.setReebId(currentReebEdgeId2);
        }

        // Find Reeb edge with v1 = prevReebId and v2 = null and reebId =
        // currentReebEdgeId1
        for (REdge e : reebGraph.getEdges()) {
            if (e.getReebId() == currentReebEdgeId1) {

                // Set this edge to have v2 = currentReebVertexId
                e.setDestAddEdge(reebGraph.getVertex(currentReebVertexId));

                break;
            }
        }

        // Find Reeb edge with reebId = prevReebId
        for (REdge e : reebGraph.getEdges()) {
            if (e.getReebId() == prevVertexReebId) {

                // Set this edge to have v2 = currentReebVertexId
                e.setDestAddEdge(reebGraph.getVertex(currentReebVertexId));

                break;
            }
        }

        List<Integer> component1Int = statusGraph.getVertexIds(component1);
        List<Integer> component2Int = statusGraph.getVertexIds(component2);

        // Create new edges with only start vertex, end vertex is set to -1
        reebGraph.addEdge(currentReebVertexId, -1, currentReebEdgeId1, component1Int);
        reebGraph.addEdge(currentReebVertexId, -1, currentReebEdgeId2, component2Int);
    }

    /**
     * Function to process the final edges in the Reeb graph: add destination
     * vertex.
     * 
     * @param reebGraph The Reeb graph.
     * @param frameNum  The frame number of final frame.
     */
    private static void processFinalEdges(ReebGraph reebGraph, int frameNum) {

        for (REdge e : reebGraph.getEdges()) {

            if (e.getDest() == null) {
                // create end vertex
                int currentReebVertexId = reebGraph.getVertices().size();
                reebGraph.addVertex(currentReebVertexId, frameNum, RVertex.END_VERTEX);

                // set v2 of edge to currentReebVertexId
                e.setDestAddEdge(reebGraph.getVertex(currentReebVertexId));
            }
        }
    }

}
