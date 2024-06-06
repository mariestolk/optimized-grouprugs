package dbvis.visualsummaries.grouprugs.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.grouprugs.tgs.Utils;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.MapperUtils;
import javafx.util.Pair;

public class SpatialQuality_SS {

    /**
     * Computes the spatial quality of the group rug.
     * 
     * @param data the data of the group rug
     * @param etp  the EntityToPosition Map for the group rug
     * 
     * @return the spatial quality of the group rug for each frame
     */
    public static double[] computeSS(
            DataPoint[][] data,
            DataPoint[][] orderedDataPoints,
            Double[][] projections,
            boolean ML,
            Integer[][] etp) {

        double[] spatialQuality = new double[data.length];

        if (ML) {

            // Find minmax values in the projections
            Pair<Double, Double> minmax = MapperUtils.getMinMaxProjections(projections);
            for (int frame = 0; frame < data.length; frame++) {
                Double frameQuality = spatialQualityFrameML(data[frame], etp[frame], projections[frame], minmax, ML,
                        frame);
                spatialQuality[frame] = frameQuality;
            }
        } else {

            for (int frame = 0; frame < data.length; frame++) {
                Double frameQuality = spatialQualityFrame(data[frame], etp[frame], frame);
                spatialQuality[frame] = frameQuality;
            }
        }

        System.out.println("Spatial Quality (SS) computed.");
        return spatialQuality;

    }

    /**
     * Computes the spatial quality of the group rug for a single frame.
     * 
     * @param data  the data of the group rug for a single frame
     * @param etp   the EntityToPosition Map for the group rug for a single frame
     * @param frame the frame number
     * 
     * @return the spatial quality of the group rug for a single frame
     */
    private static Double spatialQualityFrame(
            DataPoint[] data,
            Integer[] etp,
            int frame) {

        Double summedNumerator = 0.0;
        Double summedDenominator = 0.0;

        int knn = 10;

        for (int i = 0; i < data.length; i++) {

            DataPoint currentDataPoint = data[i];
            int id = currentDataPoint.getId();

            int embLocation = etp[id];

            Integer[] nearestNeighbors = getKNearestNeighbors(etp, id, knn);

            for (int k = 0; k < knn; k++) {

                DataPoint neighbor = data[nearestNeighbors[k]];
                int id_neighbor = neighbor.getId();

                int embLocation_neighbor = etp[id_neighbor];
                Double distance = Utils.getEuclideanDistance(currentDataPoint, neighbor);

                double embDiff = Math.abs(embLocation - embLocation_neighbor);

                double wij = Math.min(1 / embDiff, 1); // Weight function, cap at max value
                double dij = distance;

                summedNumerator += wij * dij;
                summedDenominator += wij;

            }
        }

        Double quality = summedNumerator / summedDenominator;
        return quality;
    }

    /**
     * Computes the spatial quality of the group rug for a single frame.
     * 
     * @param data  the data of the group rug for a single frame
     * @param etp   the EntityToPosition Map for the group rug for a single frame
     * @param frame the frame number
     * 
     * @return the spatial quality of the group rug for a single frame
     */
    private static Double spatialQualityFrameML(
            DataPoint[] data,
            Integer[] etp,
            Double[] projectionsFrame,
            Pair<Double, Double> minmax,
            boolean ML,
            int frame) {

        double HEIGHT = 3.0 * etp.length;

        Double summedNumerator = 0.0;
        Double summedDenominator = 0.0;

        int knn = 10;

        for (int i = 0; i < data.length; i++) {

            DataPoint currentDataPoint = data[i];
            int id = currentDataPoint.getId();

            double embLocation = ((projectionsFrame[id] - minmax.getKey()) / (minmax.getValue() - minmax.getKey()))
                    * HEIGHT;

            Integer[] nearestNeighbors;

            // Base k nearest neighbors on projected values.
            nearestNeighbors = getKNearestNeighborsML(projectionsFrame, id, knn);

            for (int k = 0; k < knn; k++) {

                DataPoint neighbor = data[nearestNeighbors[k]];
                int id_neighbor = neighbor.getId();

                double embLocation_neighbor = ((projectionsFrame[id_neighbor] - minmax.getKey())
                        / (minmax.getValue() - minmax.getKey())) * HEIGHT;
                Double distance = Utils.getEuclideanDistance(currentDataPoint, neighbor);

                double embDiff = Math.abs(embLocation - embLocation_neighbor);

                if (embDiff == 0) {
                    embDiff = 0.1;
                }

                double wij = Math.min(1 / embDiff, 1); // Weight function, cap at max
                double dij = distance;

                summedNumerator += wij * dij;
                summedDenominator += wij;

            }
        }

        Double quality = summedNumerator / summedDenominator;
        return quality;
    }

    // /**
    // * Return k nearest neighbors of an entity in the embedding space using
    // * orderedDataPoints.
    // */
    // private static Integer[] getKNearestNeighbors(DataPoint[] orderedFrame, int
    // entity_id, int k) {

    // int entity_index = -1;
    // for (int i = 0; i < orderedFrame.length; i++) {
    // if (orderedFrame[i].getId() == entity_id) {
    // entity_index = i;
    // break;
    // }
    // }

    // int counter = 1;
    // Integer[] nearestNeighbors = new Integer[k];
    // while (counter <= k) {
    // if (entity_index - counter >= 0) {
    // nearestNeighbors[counter - 1] = orderedFrame[entity_index - counter].getId();
    // counter++;
    // }
    // if (entity_index + counter < orderedFrame.length) {
    // nearestNeighbors[counter - 1] = orderedFrame[entity_index + counter].getId();
    // counter++;
    // }
    // }

    // return nearestNeighbors;

    // }

    // /**
    // * Returns the k nearest neighbors of an entity in the embedding space.
    // *
    // * @param data The data points.
    // * @param dp The data point.
    // * @param k The number of nearest neighbors to return.
    // * @return The k nearest neighbors of the data point.
    // */
    // private static Integer[] getKNearestNeighbors(Double[] framedata, int
    // entity_id, int k) {

    // // Get position of entity in embedding space
    // double embLocation = framedata[entity_id];
    // List<EntityDistancePair> pq = new ArrayList<EntityDistancePair>();

    // for (int i = 0; i < framedata.length; i++) {

    // if (i != entity_id) {

    // pq.add(new EntityDistancePair(i, Math.abs(embLocation - framedata[i])));
    // }
    // }

    // // Sort on distance
    // Collections.sort(pq, new Comparator<EntityDistancePair>() {
    // @Override
    // public int compare(EntityDistancePair o1, EntityDistancePair o2) {
    // return o1.distance.compareTo(o2.distance);
    // }
    // });

    // Integer[] nearestNeighbors = new Integer[k];

    // for (int i = 0; i < k; i++) {
    // nearestNeighbors[i] = pq.get(i).entity;
    // }

    // return nearestNeighbors;

    // }

    /**
     * Returns the k nearest neighbors of an entity in the embedding space.
     *
     * @param data The data points.
     * @param dp   The data point.
     * @param k    The number of nearest neighbors to return.
     * @return The k nearest neighbors of the data point.
     */
    private static Integer[] getKNearestNeighbors(Integer[] frame, int entity_id,
            int k) {

        // Get position of entity in embedding space
        int embLocation = frame[entity_id];
        List<EntityDistancePair> pq = new ArrayList<EntityDistancePair>();

        for (int i = 0; i < frame.length; i++) {

            if (i != entity_id) {
                pq.add(new EntityDistancePair(i, Math.abs(embLocation - (double) frame[i])));
            }
        }

        // Sort on distance
        Collections.sort(pq, new Comparator<EntityDistancePair>() {
            @Override
            public int compare(EntityDistancePair o1, EntityDistancePair o2) {
                return o1.distance.compareTo(o2.distance);
            }
        });

        Integer[] nearestNeighbors = new Integer[k];

        for (int i = 0; i < k; i++) {
            nearestNeighbors[i] = pq.get(i).entity;
        }

        return nearestNeighbors;

    }

    /**
     * Returns the k nearest neighbors of a data point.
     * 
     * @param data The data points.
     * @param dp   The data point.
     * @param k    The number of nearest neighbors to return.
     * @return The k nearest neighbors of the data point.
     */
    // private static Integer[] getKNearestNeighbors(DataPoint[] data, DataPoint dp,
    // int k) {

    // List<EntityDistancePair> pq = new ArrayList<EntityDistancePair>();

    // for (int i = 0; i < data.length; i++) {

    // if (data[i].getId() != dp.getId()) {

    // pq.add(new EntityDistancePair(data[i].getId(), Utils.getEuclideanDistance(dp,
    // data[i])));

    // }
    // }

    // Sort on distance
    // Collections.sort(pq, new Comparator<EntityDistancePair>() {
    // @Override
    // public int compare(EntityDistancePair o1, EntityDistancePair o2) {
    // return o1.distance.compareTo(o2.distance);
    // }
    // });

    // Integer[] nearestNeighbors = new Integer[k];

    // for (int i = 0; i < k; i++) {
    // nearestNeighbors[i] = pq.get(i).entity;
    // }

    // return nearestNeighbors;

    // }

    /**
     * Returns the k nearest neighbors of a data point. Used for ML projections.
     * 
     * @param data The data points.
     * @param dp   The data point.
     * @param k    The number of nearest neighbors to return.
     * @return The k nearest neighbors of the data point.
     */
    private static Integer[] getKNearestNeighborsML(Double[] projections, int entity,
            int k) {

        int HEIGHT = 3;

        List<EntityDistancePair> pq = new ArrayList<EntityDistancePair>();

        for (int i = 0; i < projections.length; i++) {

            if (i != entity) {

                pq.add(new EntityDistancePair(i, Math.abs(HEIGHT * projections[entity] - HEIGHT * projections[i])));

            }

        }

        // Sort on distance
        Collections.sort(pq, new Comparator<EntityDistancePair>() {
            @Override
            public int compare(EntityDistancePair o1, EntityDistancePair o2) {
                return o1.distance.compareTo(o2.distance);
            }
        });

        Integer[] nearestNeighbors = new Integer[k];

        for (int i = 0; i < k; i++) {
            nearestNeighbors[i] = pq.get(i).entity;
        }

        return nearestNeighbors;

    }

    /**
     * A simple class to represent a pair of an entity and its distance. Used for
     * sorting entities based on their distance in original data space.
     */
    private static class EntityDistancePair {

        public Integer entity;
        public Double distance;

        public EntityDistancePair(Integer entity, Double distance) {
            this.entity = entity;
            this.distance = distance;
        }

    }

}
