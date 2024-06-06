package dbvis.visualsummaries.grouprugs.events;

/**
 * Represents a connect- or disconnect-event between two entities.
 */
public class Event {

    public enum Type {
        CONNECT, DISCONNECT
    }

    private final int entity1; // entity id given by the data set
    private final int entity2;
    private final double time;
    private final Type type;

    /**
     * Constructs an event with the specified type and time.
     * 
     * @param type    The type of the event.
     * @param time    The time at which the event occurs.
     * @param entity1 The first entity involved in the event.
     * @param entity2 The second entity involved in the event.
     */

    public Event(int entity1, int entity2, double time, Type type) {
        this.entity1 = entity1;
        this.entity2 = entity2;
        this.time = time;
        this.type = type;
    }

    public double getTime() {
        return this.time;
    }

    /**
     * Returns the frame corresponding to the time of the event.
     * The frame is the integer part of the time.
     * 
     * @return The frame of the event.
     */
    public int getFrame() {
        return (int) this.time;
    }

    public int getEntity1() {
        return this.entity1;
    }

    public int getEntity2() {
        return this.entity2;
    }

    public Type getType() {
        return this.type;
    }

    public String toString() {
        return String.format("%s entities (%d %d) at time %.2f", this.type, this.entity1, this.entity2, this.time);
    }
}