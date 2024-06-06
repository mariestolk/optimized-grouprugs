package dbvis.visualsummaries.grouprugs.events;

import java.util.Comparator;
import java.util.PriorityQueue;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.grouprugs.tgs.Utils;

public class EventComputer {

    public static final int START_FRAME = 1;

    /**
     * Computes connect and disconnect events in the given dataset, sorted by frame
     * number.
     * 
     * @param datasetName The name of the dataset.
     * @param epsilon     The proximity threshold for events.
     * @return A priority queue of events sorted by time.
     */
    public static PriorityQueue<Event> computeEvents(String datasetName, double epsilon) {
        PriorityQueue<Event> events = new PriorityQueue<Event>(Comparator.comparingDouble(Event::getTime));
        DataSet dataset = SessionData.getInstance().getDataset(datasetName);

        int numFrames = dataset.getBaseData().length;
        int numEntities = dataset.getBaseData()[0].length;

        for (int frame = START_FRAME; frame < numFrames - 1; frame++) {
            processFrame(events, dataset, numEntities, frame, epsilon);
        }

        return events;

    }

    /**
     * Function processes a single frame in the dataset scanning for epsilon events.
     * 
     * @param events      The list of events.
     * @param dataset     The dataset.
     * @param numEntities The number of entities.
     * @param frame       The frame number.
     * @param epsilon     The epsilon value.
     */
    private static void processFrame(
            PriorityQueue<Event> events,
            DataSet dataset,
            int numEntities,
            int frame,
            double epsilon) {

        // for each pair of entities
        for (int entity1 = 0; entity1 < numEntities; entity1++) {
            for (int entity2 = entity1 + 1; entity2 < numEntities; entity2++) {

                processEntityPair(events, dataset, entity1, entity2, frame, epsilon);

            }
        }

    }

    /**
     * Function processes a pair of entities in a single frame, and adds them in a
     * (dis)connect event if they are epsilon apart between the previous and current
     * frame.
     * 
     * @param events
     * @param dataset
     * @param entity1
     * @param entity2
     * @param frame
     * @param epsilon
     */
    private static void processEntityPair(
            PriorityQueue<Event> events,
            DataSet dataset,
            int entity1,
            int entity2,
            int frame,
            double epsilon) {

        DataPoint e1 = dataset.getBaseData()[frame][entity1];
        DataPoint e2 = dataset.getBaseData()[frame][entity2];
        DataPoint e1_prev = dataset.getBaseData()[frame - 1][entity1];
        DataPoint e2_prev = dataset.getBaseData()[frame - 1][entity2];

        double distance = Utils.getEuclideanDistance(e1,
                e2);
        double distancePrev = Utils.getEuclideanDistance(e1_prev,
                e2_prev);

        // Connect Event
        if (distance <= epsilon && distancePrev > epsilon) {
            double time = calculateEventTime(distance, distancePrev, frame, epsilon);
            events.add(new Event(entity1, entity2, time, Event.Type.CONNECT));

            // Disconnect Event
        } else if (distance > epsilon && distancePrev <= epsilon) {
            double time = calculateEventTime(distance, distancePrev, frame, epsilon);
            events.add(new Event(entity1, entity2, time, Event.Type.DISCONNECT));
        }
    }

    /**
     * Function finds the exact time of a connect or disconnect event. This is the
     * exact time that the entities are at the epsilon distance, assuming linear
     * trajectory.
     * 
     * @param distance     The distance between two entities.
     * @param distancePrev The previous distance between two entities.
     * @param frame        The frame number.
     * @param epsilon      The epsilon value.
     * 
     * @return The exact time of the event.
     */
    private static double calculateEventTime(
            double distance,
            double distancePrev,
            int frame,
            double epsilon) {
        double time = 0.0;

        double timeRatio = (distancePrev - epsilon) / (distancePrev - distance);
        time = frame - 1 + timeRatio;

        return time;

    }

}