package dbvis.visualsummaries.grouprugs.linearprogram;

import java.util.ArrayList;
import java.util.List;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;

/**
 * A vertex in the MLCM-TC instance. Note that the vertex is initialized as leaf
 * or internal node, depending on the constructor used. This distinction is used
 * in determining the weight in the objective function of the quadratic program.
 */
public class MLCMVertex {

    String vertexID; // Unique identifier of the vertex. It is the maximal group id + frame.
    int frame;
    MaximalGroup group;
    List<MLCMVertex> adjList;
    String type;
    MLCMVertex parent;

    public MLCMVertex(String vertexID, MaximalGroup group, int frame) {
        this.vertexID = vertexID; // groupId + "_" + frame
        this.frame = frame;
        this.type = "leaf";
        this.parent = null;

        adjList = new ArrayList<MLCMVertex>();
    }

    int layer;

    public MLCMVertex(Integer layer) {
        this.layer = layer;
        this.type = "internal";
        this.parent = null;

        adjList = new ArrayList<MLCMVertex>();
    }

    public String getVertexID() {
        return vertexID;
    }

    public int getFrame() {
        String[] parts = vertexID.split("_");
        return Integer.parseInt(parts[1]);
    }

    public int getGroupId() {
        String[] parts = vertexID.split("_");
        return Integer.parseInt(parts[0]);
    }

    public String getType() {
        return type;
    }

    public MLCMVertex getParent() {
        return parent;
    }

    public void setParent(MLCMVertex parent) {
        this.parent = parent;
    }

    public Integer getLayer() {
        return layer;
    }

    public void addEdge(MLCMVertex v) {
        adjList.add(v);
    }

}
