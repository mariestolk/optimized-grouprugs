package dbvis.visualsummaries.grouprugs.linearprogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.REdge;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;

/**
 * Converts a ReebGraph to an MLCMGraph.
 */
public class ReebToMLCM {

    public static MLCMGraph convert(ReebGraph rg, Set<MaximalGroup> maximalgroups) {

        // Initialize empty timestamps list
        List<Integer> timestamps = new ArrayList<>();

        // Step 1: find all timestamps associated with each edge of @{rg}.
        for (REdge e : rg.getEdges()) {

            // Add the start and end time of the edge to the timestamps list
            int begin = e.getSource().getFrame();
            int end = e.getDest().getFrame() - 1;

            if (!timestamps.contains(begin)) {
                timestamps.add(begin);
            }

            if (!timestamps.contains(end)) {
                timestamps.add(end);
            }

        }

        // Step 2: for each maximal group in @code{maximalgroups}, for each timestamp,
        // create a vertex v_m,r in the MLCM-TC instance. Happens upon creation.
        MLCMGraph mlcmtc = new MLCMGraph(maximalgroups, timestamps);

        // Step 3: for each maximal group in @code{maximalgroups}, for each pair
        // of consecutive timestamps, create an edge between the corresponding vertices
        // in the MLCM-TC instance.
        for (MaximalGroup mg : maximalgroups) {

            for (int i = 0; i < timestamps.size() - 1; i++) {

                int t1 = timestamps.get(i);
                int t2 = timestamps.get(i + 1);

                int groupId = mlcmtc.getGroupToIDMap().get(mg);

                String vertexID1 = groupId + "_" + t1;
                String vertexID2 = groupId + "_" + t2;

                mlcmtc.addEdge(vertexID1, vertexID2);

            }

        }

        // Step 4: for each layer Vr, create a tree Tr in the MLCM-TC instance as
        // follows:
        for (int layer : mlcmtc.getLayers()) {
            // Step 4a: create an internal tree node v_e,r and tree edges {v_e,r, v_m,r} for
            // each maximal group that is present in e.component.

            int internalNodeCount = 0;

            // Find reeb edges that are present in the layer
            for (REdge e : rg.getEdges()) {

                if (e.getSource().getFrame() <= layer
                        && e.getDest().getFrame() - 1 >= layer) {

                    MLCMVertex v_e_r = new MLCMVertex(layer);
                    // add v_e_r to the graph
                    mlcmtc.addTreeNode(v_e_r);
                    internalNodeCount++;

                    // Check which maximal groups are present in the component of the edge.
                    for (MaximalGroup mg : maximalgroups) {
                        if (e.getComponent().containsAll(mg.getEntities())) {

                            int groupId = mlcmtc.getGroupToIDMap().get(mg);
                            String vertexID = groupId + "_" + v_e_r.getLayer();

                            MLCMVertex v_m_r = mlcmtc.getVertexIDToVertexMap().get(vertexID);
                            v_m_r.setParent(v_e_r);
                            mlcmtc.addTreeEdge(v_e_r, v_m_r);

                        }

                    }

                }
            }

            // Step 4b: Unless the above results in a rooted tree with all nodes in Vr as
            // leaves, create a tree root \rho_r and tree edges {\rho_r, v_e,r} for each
            // v_e,r that is not a leaf.
            if (internalNodeCount > 1) {
                MLCMVertex rho_r = new MLCMVertex(layer);
                rho_r.type = "root";
                mlcmtc.addTreeNode(rho_r);
                for (MLCMVertex v : mlcmtc.getTrees()) {
                    if (v.getLayer() == layer && v.getType().equals("internal")) {
                        mlcmtc.addTreeEdge(rho_r, v);
                        v.setParent(rho_r);
                    }
                }
            }

        }

        mlcmtc.setLayerToVerticesMap();
        return mlcmtc;

    }

}