package dbvis.visualsummaries.grouprugs.tgs.maximalgroups;

import java.util.List;

/**
 * A maximal group is a group of entities that are epsilon-close during a
 * certain time interval, based on the chosen Group Selection Strategy. The
 * MaximalGroup is characterized by the list of entities, the start frame and
 * the end frame.
 */
public class MaximalGroup {

    List<Integer> entities;
    int startFrame;
    int endFrame;

    // Constructor for reading from file
    public MaximalGroup(String groupString, int startFrame, int endFrame) {

        String[] groupParts = groupString.split(" ");

        this.entities = new java.util.ArrayList<Integer>();
        String[] entitiesString = groupParts[0].substring(1, groupParts[0].length() - 1).split(",");
        for (String entity : entitiesString) {
            this.entities.add(Integer.parseInt(entity));
        }

        this.startFrame = startFrame;
        this.endFrame = endFrame;

    }

    public MaximalGroup(List<Integer> entities, int startFrame) {
        this.entities = entities;
        this.startFrame = startFrame;
        this.endFrame = -1;
    }

    public List<Integer> getEntities() {
        return this.entities;
    }

    public void setEntities(List<Integer> entities) {
        this.entities = entities;
    }

    public int getStartFrame() {
        return this.startFrame;
    }

    public void setStartFrame(int startFrame) {
        this.startFrame = startFrame;
    }

    public int getEndFrame() {
        return this.endFrame;
    }

    public void setEndFrame(int endFrame) {
        this.endFrame = endFrame;
    }

    /**
     * Returns the list of entity IDs.
     *
     * @return List of entity IDs.
     */
    public List<Integer> getEntityIDs() {
        List<Integer> entityIDs = new java.util.ArrayList<Integer>();
        for (Integer entity : getEntities()) {
            entityIDs.add(entity);
        }
        return entityIDs;
    }

    public int getDuration() {
        return getEndFrame() - getStartFrame() + 1;
    }

    public String toString() {
        return this.entities.toString() + "\nduring interval: [" + this.startFrame + ", " + this.endFrame + "]";
    }

    /**
     * Returns the list of entities as a string for writing to file.
     */
    public String entitiesToString() {

        String entitiesString = "[";

        for (int i = 0; i < this.entities.size() - 1; i++) {
            entitiesString += this.entities.get(i) + ",";
        }

        entitiesString += this.entities.get(this.entities.size() - 1) + "]";

        return entitiesString;
    }

}
