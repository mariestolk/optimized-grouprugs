package dbvis.visualsummaries.grouprugs.visualization.groupselectionstrategies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.REdge;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.RVertex;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;

/**
 * Given a Reeb graph, this class returns the maximal groups that are present
 * from t_0 to t_{tau}. This is used in the MLCM-TC algorithm for computing
 * group ordering.
 */
public class MaximalDurationStrategy {

    public MaximalDurationStrategy() {

    }

    public String getName() {
        return "Maximal Duration Strategy";
    }

    public String getSimpleName() {
        return "maxduration";
    }

    /**
     * Given a Reeb graph with maximal groups on edges, this method returns the
     * set of maximal groups that are maximal in duration.
     * 
     * @param rg Reeb graph
     * 
     * @return List<MaximalGroup> List of maximal groups
     */
    public static Set<MaximalGroup> selectGroups(ReebGraph rg) {

        Set<MaximalGroup> groups = new HashSet<MaximalGroup>();

        // Get topologically sorted Reeb graph
        List<RVertex> sortedVertices = rg.topologicalSort();

        // Find maximal timestamp
        int maxTimestamp = 0;
        for (RVertex v : sortedVertices) {
            if (v.getFrame() > maxTimestamp) {
                maxTimestamp = v.getFrame();
            }
        }

        for (RVertex v : sortedVertices) {
            for (REdge e : v.getOutEdges()) {

                List<MaximalGroup> maximalGroups = e.getMaximalGroups();

                // Add maximal groups that exist from t_0 to t_{tau} to the list
                for (MaximalGroup g : maximalGroups) {
                    if (g.getStartFrame() == 0 && g.getEndFrame() == maxTimestamp) {
                        groups.add(g);
                    }

                }

            }
        }

        return groups;

    }
}