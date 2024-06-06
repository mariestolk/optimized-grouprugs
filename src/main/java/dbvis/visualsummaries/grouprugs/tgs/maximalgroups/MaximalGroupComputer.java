package dbvis.visualsummaries.grouprugs.tgs.maximalgroups;

import java.util.List;

import dbvis.visualsummaries.grouprugs.tgs.reebgraph.REdge;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.RVertex;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;

import java.util.ArrayList;

/**
 * The MaximalGroupComputer class is responsible for computing the maximal
 * groups for the edges of the Reeb graph. The computation of maximal groups is
 * specified in the paper by Buchin et al. on the Trajectory Grouping Structure
 * algorithm.
 */
public class MaximalGroupComputer {

    public static void compute(ReebGraph reebGraph) {

        List<RVertex> topologicalOrdering = reebGraph.topologicalSort();

        for (RVertex v : topologicalOrdering) {

            switch (v.getType()) {

                case RVertex.START_VERTEX:
                    handleStartVertex(v);
                    break;

                case RVertex.MERGE_VERTEX:
                    handleMergeVertex(v);
                    break;

                case RVertex.SPLIT_VERTEX:
                    handleSplitVertex(v);
                    break;
                case RVertex.END_VERTEX:
                    handleEndVertex(v);
                    break;

                default:
                    break;

            }

        }

    }

    /**
     * Function computes maximal groups for outgoing edges of a start vertex.
     * 
     * @param v The start vertex.
     */
    private static void handleStartVertex(RVertex v) {

        for (REdge e : v.getOutEdges()) {

            List<Integer> entities = e.getComponent();
            int frame = v.getFrame();

            e.addMaximalGroup(entities, frame);

        }

    }

    /**
     * Function computes maximal groups for outgoing edges of a merge vertex.
     * 
     * @param v The merge vertex.
     */
    private static void handleMergeVertex(RVertex v) {

        int frame = v.getFrame();

        // Get the 2 incoming edges and the outgoing edge
        REdge inEdge1 = v.getInEdges().get(0);
        REdge inEdge2 = v.getInEdges().get(1);
        REdge e = v.getOutEdges().get(0);

        // Get the maximal groups of the 2 incoming edges
        List<MaximalGroup> maximalGroups1 = inEdge1.getMaximalGroups();
        List<MaximalGroup> maximalGroups2 = inEdge2.getMaximalGroups();

        // Get the component of inEdge1 and inEdge2
        List<Integer> component1 = inEdge1.getComponent();
        List<Integer> component2 = inEdge2.getComponent();

        // new_component = component1 U component2
        List<Integer> newComponent = union(component1, component2);

        // New maximal group starting at this vertex
        MaximalGroup newMaximalGroup = new MaximalGroup(newComponent, frame);

        // Add new maximal groups to outgoing edge
        e.addMaximalGroup(newMaximalGroup);
        e.addMaximalGroups(maximalGroups1);
        e.addMaximalGroups(maximalGroups2);

    }

    /**
     * Function computes maximal groups for outgoing edges of a split vertex.
     * 
     * @param v The split vertex.
     */
    private static void handleSplitVertex(RVertex v) {

        // Get incoming edge and outgoing edges
        REdge inEdge = v.getInEdges().get(0);

        // Get maximal groups of incoming edge
        List<MaximalGroup> maximalGroups = inEdge.getMaximalGroups();

        for (REdge outEdge : v.getOutEdges()) {

            // Get the component of the outgoing edge
            List<Integer> component = outEdge.getComponent();

            for (MaximalGroup maximalGroup : maximalGroups) {
                handleSplitVertexUtil(component, outEdge, maximalGroup, maximalGroups);
            }

        }

    }

    private static void handleSplitVertexUtil(List<Integer> component, REdge outEdge,
            MaximalGroup maximalGroup,
            List<MaximalGroup> maximalGroups) {

        // Get the intersection of the component of the outgoing edge and the entities
        // of the maximal group
        List<Integer> intersection = intersection(component, maximalGroup.getEntities());

        // Case 1: maximal group is fully contained in component of the outgoing edge
        if (intersection.containsAll(maximalGroup.getEntities())) {
            outEdge.addMaximalGroup(maximalGroup);

            // Case 2: maximal group is partially contained in the component of the outgoing
            // edges
        } else if (intersection.size() > 0) {

            // Set end frame of maximal group to the frame of the split vertex
            RVertex source = outEdge.getSource();
            int frame = source.getFrame();
            maximalGroup.setEndFrame(frame);

            // Create new maximal group
            MaximalGroup newMaximalGroup = new MaximalGroup(intersection, maximalGroup.getStartFrame());

            // TODO: Dit kan beter.
            // Check if outEdge already has a group with the exact same entities
            boolean alreadyExists = false;
            for (MaximalGroup group : maximalGroups) {
                if (group.getEntities().containsAll(newMaximalGroup.getEntities())
                        && newMaximalGroup.getEntities().containsAll(group.getEntities())) {

                    if (group.getStartFrame() > newMaximalGroup.getStartFrame()) {
                        group.setStartFrame(newMaximalGroup.getStartFrame());
                    }

                    alreadyExists = true;
                }
            }

            if (!alreadyExists) {
                outEdge.addMaximalGroup(newMaximalGroup);
            }

        }

    }

    /**
     * Function sets the endframe of maximal groups for incoming edges of an end
     * vertex.
     * 
     * @param v The end vertex.
     */
    private static void handleEndVertex(RVertex v) {

        int frame = v.getFrame();
        REdge inEdge = v.getInEdges().get(0);
        List<MaximalGroup> maximalGroups = inEdge.getMaximalGroups();

        for (MaximalGroup maximalGroup : maximalGroups) {
            maximalGroup.setEndFrame(frame);
        }

    }

    /**
     * Function computes the union of 2 lists.
     * 
     * @param component1 The first list.
     * @param component2 The second list.
     * @return The union of the 2 lists.
     */
    private static List<Integer> union(List<Integer> component1, List<Integer> component2) {

        List<Integer> union = new ArrayList<>();

        for (Integer i : component1) {
            if (!union.contains(i)) {
                union.add(i);
            }
        }

        for (Integer i : component2) {
            if (!union.contains(i)) {
                union.add(i);
            }
        }

        return union;

    }

    /**
     * Function computes the intersection of 2 lists.
     * 
     * @param component1 The first list.
     * @param component2 The second list.
     * @return The intersection of the 2 lists.
     */
    private static List<Integer> intersection(List<Integer> component1, List<Integer> component2) {

        List<Integer> intersection = new ArrayList<>();

        for (Integer i : component1) {
            if (component2.contains(i)) {
                intersection.add(i);
            }
        }

        return intersection;

    }

}
