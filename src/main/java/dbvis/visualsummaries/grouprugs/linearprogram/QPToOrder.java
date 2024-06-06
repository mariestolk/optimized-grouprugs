package dbvis.visualsummaries.grouprugs.linearprogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

public class QPToOrder {

    private List<MGOrder> orderingChanges;

    public QPToOrder() {
        this.orderingChanges = new ArrayList<>();
    }

    /**
     * This method computes the order of the maximal groups in the ReebGraph to
     * avoid crossings.
     * 
     * @param decisionVariables The decision variables from the QP.
     * @return A TreeMap containing the ordered maximal groups per layer.
     */
    public void computeOrder(
            Map<Integer, Map<Pair<Integer, Integer>, Boolean>> decisionVariables) {

        List<MGOrder> orderChanges = new ArrayList<>();

        // TreeMap<Integer, List<Integer>> orderedVerticesPerLayer = new TreeMap<>();

        // Extract the order from the decision variables
        for (Map.Entry<Integer, Map<Pair<Integer, Integer>, Boolean>> layerEntry : decisionVariables.entrySet()) {

            int layer = layerEntry.getKey();
            Map<Pair<Integer, Integer>, Boolean> layerDecisionVariables = layerEntry.getValue();

            Map<Integer, List<Integer>> graph = new HashMap<>(); // Construct directed graph per layer
            for (Map.Entry<Pair<Integer, Integer>, Boolean> decisionVariable : layerDecisionVariables.entrySet()) {

                Pair<Integer, Integer> groupPair = decisionVariable.getKey();
                boolean above = decisionVariable.getValue();

                // Add an edge if the decision variable is true
                if (above) {
                    if (!graph.containsKey(groupPair.getKey())) {
                        graph.put(groupPair.getKey(), new ArrayList<>());
                    }

                    if (!graph.containsKey(groupPair.getValue())) {
                        graph.put(groupPair.getValue(), new ArrayList<>());
                    }

                    graph.get(groupPair.getKey()).add(groupPair.getValue());
                }

            }

            List<Integer> orderedVertices = topoSort(graph);
            orderChanges.add(new MGOrder(layer, orderedVertices));
            // orderedVerticesPerLayer.put(layer, orderedVertices);
            // System.out.println("Layer: " + layer + " Ordered Vertices: " +
            // orderedVertices);

        }

        this.orderingChanges = orderChanges;
    }

    public List<MGOrder> getOrderedVertices() {
        return this.orderingChanges;
    }

    private List<Integer> topoSort(Map<Integer, List<Integer>> graph) {
        List<Integer> result = new ArrayList<>();
        Map<Integer, Boolean> visited = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
            int vertex = entry.getKey();
            if (!visited.containsKey(vertex)) {
                dfs(graph, vertex, visited, result);
            }
        }
        return result;
    }

    private void dfs(
            Map<Integer, List<Integer>> graph,
            int vertex,
            Map<Integer, Boolean> visited,
            List<Integer> result) {
        visited.put(vertex, true);
        for (int neighbor : graph.get(vertex)) {
            if (!visited.containsKey(neighbor)) {
                dfs(graph, neighbor, visited, result);
            }
        }
        result.add(vertex);
    }

}
