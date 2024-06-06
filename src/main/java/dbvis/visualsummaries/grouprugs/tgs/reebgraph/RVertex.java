package dbvis.visualsummaries.grouprugs.tgs.reebgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Nested class representing a vertex (entity) in the graph.
 * 
 * @param id The id of the vertex.
 */
public class RVertex {
    private int id;
    private int frame;
    private double time;
    private String type;
    private List<REdge> outEdges;
    private List<REdge> incEdges;
    public static final String START_VERTEX = "start";
    public static final String END_VERTEX = "end";
    public static final String MERGE_VERTEX = "merge";
    public static final String SPLIT_VERTEX = "split";

    public RVertex(int id, double time, String type) {
        this.id = id;
        this.frame = (int) time;
        this.time = time;
        this.type = type;
        this.outEdges = new ArrayList<>();
        this.incEdges = new ArrayList<>();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTime() {
        return this.time;
    }

    public int getFrame() {
        return this.frame;
    }

    public String getType() {
        return this.type;
    }

    public void updateIncEdges(List<REdge> incEdges) {
        this.incEdges = incEdges;
    }

    public void updateOutEdges(List<REdge> outEdges) {
        this.outEdges = outEdges;
    }

    public List<REdge> getInEdges() {
        return this.incEdges;
    }

    public List<REdge> getOutEdges() {
        return this.outEdges;
    }

    public void addIncEdge(REdge e) {
        this.incEdges.add(e);
    }

    public void addOutEdge(REdge e) {
        this.outEdges.add(e);
    }

    public String toString() {
        return "v " + id + " " + frame + " " + type;
    }

    public String toWrite() {
        return "v " + id + " " + frame + " " + type + " " + outEdges + " "
                + incEdges;
    }

    public void setType(String type) {
        if (type.equals(START_VERTEX) || type.equals(END_VERTEX) || type.equals(MERGE_VERTEX)
                || type.equals(SPLIT_VERTEX)) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Invalid vertex type.");
        }
    }
}