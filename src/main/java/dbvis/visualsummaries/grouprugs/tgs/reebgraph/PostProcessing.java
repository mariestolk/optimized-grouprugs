package dbvis.visualsummaries.grouprugs.tgs.reebgraph;

import java.util.ArrayList;
import java.util.List;

public class PostProcessing {

    /**
     * Post-processes the ReebGraph by collapsing edges that connect vertices which
     * take place in the same frame. (Edge case which occurs often in discreet time)
     * 
     * @param rg The ReebGraph to be processed.
     * @return The post-processed ReebGraph.
     */
    public static ReebGraph postProcess(ReebGraph rg) {

        // System.out.println(
        // "edges before collapse: " + rg.getEdges().size() + ", vertices before
        // collapse: "
        // + rg.getVertices().size());

        ReebGraph newRg = edgeCollapse(rg);

        // System.out.println(
        // "edges after collapse: " + newRg.getEdges().size() + ", vertices after
        // collapse: "
        // + newRg.getVertices().size());

        return newRg;
    }

    /**
     * Collapses edges that connect vertices u and v which take place in the same
     * frame. The function takes the first vertex u in the topological order, and
     * copies v's outgoing edges to u. Then it removes v and all edges connected to
     * v.
     * 
     * 
     * @param rg The ReebGraph to be processed.
     * @return The ReebGraph with collapsed edges.
     */
    private static ReebGraph edgeCollapse(ReebGraph rg) {

        int edges_collapsed;

        do {
            boolean[] visited = new boolean[rg.getVertices().size()];

            edges_collapsed = 0;

            // Get list of Reeb vertices
            List<RVertex> vertices = new ArrayList<>(rg.getVertices());

            for (RVertex v : vertices) {

                if (visited[v.getId()]) {
                    continue;
                }

                visited[v.getId()] = true;

                List<REdge> outEdges = new ArrayList<>(v.getOutEdges());
                for (REdge e : outEdges) {
                    if (v.getFrame() == e.getDest().getFrame()) {
                        visited[e.getDest().getId()] = true;

                        rg.collapseEdge(e);
                        edges_collapsed++;
                    }
                }

            }

            updateRVertexIds(rg);
            updateREdgeIds(rg);

        } while (edges_collapsed > 0);

        return rg;
    }

    /**
     * Updates the ids of the vertices in the ReebGraph.
     * 
     * @param rg The ReebGraph to be processed.
     */
    private static void updateRVertexIds(ReebGraph rg) {
        int i = 0;
        for (RVertex v : rg.getVertices()) {
            if (v.getId() != i) {
                v.setId(i);

                // Update incoming edges
                for (REdge e : v.getInEdges()) {
                    e.setDest(v);
                }

                // Update outgoing edges
                for (REdge e : v.getOutEdges()) {
                    e.setSource(v);
                }
            }
            i++;
        }

    }

    /**
     * Updates the ids of the edges in the ReebGraph.
     * 
     * @param rg The ReebGraph to be processed.
     */
    private static void updateREdgeIds(ReebGraph rg) {
        int i = 0;
        for (REdge e : rg.getEdges()) {
            e.setReebId(i);
            i++;
        }
    }

}