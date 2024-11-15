package dbvis.visualsummaries.grouprugs.linearprogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;

/**
 * The MLCMGraph class.
 * 
 * This class represents the MLCM-TC instance.
 */
public class MLCMGraph {

    private List<MLCMVertex> graph; // List of base vertices
    private List<MLCMVertex> trees; // List of trees per layer, index corresponds to layer.
    private HashMap<Integer, List<MLCMVertex>> layerToVerticesMap;

    private HashMap<String, MLCMVertex> vertexIDToVertexMap; // vertexID -> vertex
    private HashMap<Integer, MaximalGroup> IDToGroupMap; // groupId -> group
    private HashMap<MaximalGroup, Integer> GroupToIDMap; // group -> groupId

    /**
     * Initializes the MLCM-TC instance, where there are as many vertices as there
     * are group lines * timestamps. Timestamps are only those where a Reeb
     * vertex is present (and thus group dynamics change).
     * 
     * @param grouplines Set of group lines.
     * @param timestamps List of timestamps where group dynamics change.
     */
    public MLCMGraph(Set<MaximalGroup> grouplines, List<Integer> timestamps) {

        graph = new ArrayList<MLCMVertex>();
        trees = new ArrayList<MLCMVertex>();

        layerToVerticesMap = new HashMap<Integer, List<MLCMVertex>>();

        vertexIDToVertexMap = new HashMap<String, MLCMVertex>();
        IDToGroupMap = new HashMap<Integer, MaximalGroup>();
        GroupToIDMap = new HashMap<MaximalGroup, Integer>();

        // Create initial vertices
        int groupId = 0;
        for (MaximalGroup mg : grouplines) {

            // Add the group to the group map for easy access
            IDToGroupMap.put(groupId, mg);
            GroupToIDMap.put(mg, groupId);

            for (int t : timestamps) {

                String vertexID = groupId + "_" + t;
                addVertex(vertexID, mg, t);

            }

            groupId++;
        }

    }

    /**
     * Initializes the MLCM-TC instance, where there are as many vertices as there
     * are group lines * timestamps. Timestamps are only those where a Reeb
     * vertex is present (and thus group dynamics change).
     * 
     * @param grouplines Set of group lines.
     * @param timestamps List of timestamps where group dynamics change.
     */
    public void setLayerToVerticesMap() {
        for (MLCMVertex v : graph) {
            int layer = v.getFrame();
            if (!layerToVerticesMap.containsKey(layer)) {
                layerToVerticesMap.put(layer, new ArrayList<MLCMVertex>());
            }
            layerToVerticesMap.get(layer).add(v);
        }
    }

    /**
     * Returns the vertices of the graph for the specified layer.
     * 
     * @param layer The layer.
     * @return The vertices of the graph for the specified layer.
     */
    public List<MLCMVertex> getVerticesForLayer(int layer) {
        return layerToVerticesMap.get(layer);
    }

    /**
     * Returns the layers of the graph, representing timestamps in the Reeb graph.
     * 
     * @return The layers of the graph.
     */
    public List<Integer> getLayers() {
        List<Integer> layers = new ArrayList<Integer>();
        for (MLCMVertex v : graph) {
            if (!layers.contains(v.getFrame())) {
                layers.add(v.getFrame());
            }
        }
        return layers;
    }

    /**
     * Returns the vertex ID to vertex map.
     * 
     * @return The vertex ID to vertex map.
     */
    public HashMap<String, MLCMVertex> getVertexIDToVertexMap() {
        return vertexIDToVertexMap;
    }

    /**
     * Returns the group ID to group map.
     * 
     * @return The group ID to group map.
     */
    public HashMap<Integer, MaximalGroup> getIDToGroupMap() {
        return IDToGroupMap;
    }

    /**
     * Returns the group to group ID map.
     * 
     * @return The group to group ID map.
     */
    public HashMap<MaximalGroup, Integer> getGroupToIDMap() {
        return GroupToIDMap;
    }

    /**
     * Adds a vertex to the graph. The vertex has an ID and is associated with a
     * frame and a maximal group. It is added to the vertex hashmap for easy access.
     * 
     * @param frame The frame of the vertex.
     * @param group The maximal group of the vertex, this is also its unique id.
     */
    public void addVertex(String vertexID, MaximalGroup mg, int frame) {
        MLCMVertex v = new MLCMVertex(vertexID, frame);
        vertexIDToVertexMap.put(vertexID, v);
        graph.add(v);
    }

    /**
     * Adds a tree node to the graph for the given layer.
     * 
     * @param layer The layer of the tree node.
     */
    public void addTreeNode(MLCMVertex v_e_r) {
        trees.add(v_e_r);
    }

    /**
     * Adds a tree edge to the graph between the specified vertices.
     * 
     * @param v_e_r The first vertex.
     * @param v_m_r The second vertex.
     */
    public void addTreeEdge(MLCMVertex v_e_r, MLCMVertex v_m_r) {
        v_e_r.addEdge(v_m_r);
    }

    /**
     * Adds an edge between two vertices.
     * 
     * @param v1 The first vertex.
     * @param v2 The second vertex.
     */
    public void addEdge(String v1, String v2) {
        MLCMVertex vertex1 = vertexIDToVertexMap.get(v1);
        MLCMVertex vertex2 = vertexIDToVertexMap.get(v2);

        vertex1.addEdge(vertex2);
        vertex2.addEdge(vertex1);
    }

    /**
     * Returns the vertex with the specified vertex ID.
     * 
     * @param vertexID The ID of the vertex.
     * @return The vertex.
     */
    public MLCMVertex getVertex(String vertexID) {
        return vertexIDToVertexMap.get(vertexID);
    }

    /**
     * Returns the vertices of the base graph. This is a list of @code{MLCMVertex}
     * objects.
     * 
     * @return The vertices.
     */
    public List<MLCMVertex> getVertices() {
        return graph;
    }

    /**
     * Returns the trees of the MLCM-TC instance. This is a list
     * of @code{MLCMVertex} objects.
     * 
     * @return The trees.
     */
    public List<MLCMVertex> getTrees() {
        return trees;
    }

    /**
     * Main method for testing purposes.
     */
    public static void main(String[] args) {

        // Simple example instance based on 'fishdatamerge' dataset.
        Set<MaximalGroup> maximalGroups = new HashSet<>();
        List<Integer> timestamps = new ArrayList<Integer>();

        List<Integer> entities1 = new ArrayList<Integer>();
        List<Integer> entities2 = new ArrayList<Integer>();

        for (int i = 0; i < 151; i++) {
            if (i < 27) {
                entities1.add(i);
            } else {
                entities2.add(i);
            }

        }

        timestamps.add(0);
        timestamps.add(1029);
        timestamps.add(1030);
        timestamps.add(1497);
        timestamps.add(1498);
        timestamps.add(1999);

        MaximalGroup mg1 = new MaximalGroup(entities1, 0);
        mg1.setEndFrame(1999);
        maximalGroups.add(mg1);

        MaximalGroup mg2 = new MaximalGroup(entities2, 0);
        mg2.setEndFrame(1999);
        maximalGroups.add(mg2);

        MLCMGraph mlcmGraph = new MLCMGraph(maximalGroups, timestamps);

        System.out.println(mlcmGraph.getVertices().size());
    }

}
