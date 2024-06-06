package dbvis.visualsummaries.grouprugs.tgs.reebgraph;

import java.util.ArrayList;
import java.util.List;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;

/**
 * Nested class representing an edge (interaction) in the graph.
 * 
 * @param source The first vertex of the edge.
 * @param dest   The second vertex of the edge.
 */
public class REdge {
    private RVertex source;
    private RVertex dest;
    private int reebId;
    private List<MaximalGroup> maximalGroups;
    private List<Integer> component;

    /**
     * Constructor for the edge.
     * 
     * @param source    The source vertex.
     * @param dest      The destination vertex.
     * @param reebId    The Reeb Id of the edge.
     * @param component The connected component associated with the edge.
     */
    public REdge(RVertex source, RVertex dest, int reebId, List<Integer> component) {
        this.source = source;
        this.dest = dest;

        this.reebId = reebId;
        this.maximalGroups = new ArrayList<MaximalGroup>();
        this.component = component;

        // Add this edge as outgoing edge to the source vertex.
        source.addOutEdge(this);

        if (dest != null) {
            // Add this edge as incoming edge to the destination vertex.
            dest.addIncEdge(this);
        }

    }

    /**
     * Get the source vertex of the edge.
     * 
     * @return The source vertex.
     */
    public RVertex getSource() {
        return this.source;
    }

    /**
     * Sets the source vertex of the edge.
     *
     */
    public void setSource(RVertex source) {
        this.source = source;

    }

    /**
     * Get the destination vertex of the edge.
     * 
     * @return The destination vertex.
     */
    public RVertex getDest() {
        return this.dest;
    }

    /**
     * Sets the destination vertex of the edge. In addition, it adds the edge as
     * incoming edge to the destination vertex.
     * 
     * @param dest The destination vertex.
     */
    public void setDestAddEdge(RVertex dest) {
        this.dest = dest;

        // Add this edge as incoming edge to the destination vertex.
        dest.addIncEdge(this);
    }

    /**
     * Sets the destination vertex of the edge. No additional operations are
     * performed.
     * 
     * @param dest The destination vertex.
     */
    public void setDest(RVertex dest) {
        this.dest = dest;
    }

    /**
     * Sets the Reeb Id of the edge.
     * 
     */
    public int getReebId() {
        return this.reebId;
    }

    /**
     * Sets the Reeb Id of the edge.
     * 
     * @param reebId The Reeb Id of the edge.
     */
    public void setReebId(int reebId) {
        this.reebId = reebId;
    }

    /**
     * Get the connected component associated with this edge.
     * 
     * @return The component of the edge.
     */
    public List<Integer> getComponent() {
        return this.component;
    }

    public String writeComponent() {
        String output = "[";
        for (int i : this.component) {
            output += i + ",";
        }

        // Remove the last comma.
        output = output.substring(0, output.length() - 1);
        output += "]";

        return output;
    }

    public String toWrite() {
        return "e " + source.getId() + " " + dest.getId() + " " + reebId + " " + maximalGroups;
    }

    /*
     * Functions for Maximal Group Computation as specified by Buchin et al. below.
     */

    public void addMaximalGroup(List<Integer> entities, int frame) {
        MaximalGroup maximalGroup = new MaximalGroup(entities, frame);
        this.maximalGroups.add(maximalGroup);
    }

    public void addMaximalGroup(MaximalGroup maximalGroup) {
        this.maximalGroups.add(maximalGroup);
    }

    public void addMaximalGroups(List<MaximalGroup> maximalGroups) {
        for (MaximalGroup maximalGroup : maximalGroups) {
            this.maximalGroups.add(maximalGroup);
        }
    }

    public List<MaximalGroup> getMaximalGroups() {
        return this.maximalGroups;
    }

}
